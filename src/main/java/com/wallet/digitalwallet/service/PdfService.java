package com.wallet.digitalwallet.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.entity.Wallet;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.TransactionRepository;
import com.wallet.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    /**
     * İşlem ID'sine göre kullanıcıya özel dekont PDF'i üretir.
     * @Transactional(readOnly = true) anotasyonu Lazy Initialization hatalarını önler.
     */
    @Transactional(readOnly = true)
    public byte[] generateTransactionReceipt(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("İşlem kaydı bulunamadı: " + transactionId));

        // 1. Sistem kaynaklı işlemler (örn: SYSTEM_DEPOSIT) için alıcı IBAN'ı baz al
        String searchIban = (transaction.getFromIban() != null && transaction.getFromIban().startsWith("SYSTEM"))
                ? transaction.getToIban()
                : transaction.getFromIban();

        Wallet wallet = walletRepository.findByIban(searchIban)
                .orElseThrow(() -> new ResourceNotFoundException("Cüzdan bulunamadı: " + searchIban));

        // 2. Kullanıcının sahip olduğu tüm IBAN'ları lazy relation üzerinden çek
        List<String> userIbans = wallet.getUser().getWallets().stream()
                .map(Wallet::getIban)
                .toList();

        // 3. Kullanıcı bazlı kronolojik sıra numarasını hesapla
        long sequenceNumber = transactionRepository.countUserTransactionsUntil(
                userIbans,
                transaction.getCreatedAt(),
                transaction.getId()
        );

        // --- PDF Oluşturma ---
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fontlar
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font successFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 150, 0));

            // Başlık Alanı
            Paragraph title = new Paragraph("DIGITAL WALLET", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("TRANSFER DEKONTU", FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.GRAY));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(30);
            document.add(subTitle);

            // Bilgi Tablosu
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            addTableRow(table, "Islem Sira No:", String.valueOf(sequenceNumber), labelFont, valueFont);            addTableRow(table, "REFERANS KODU", "TX-" + (transaction.getId() + 10000), labelFont, valueFont);
            addTableRow(table, "ISLEM TIPI", transaction.getTransactionType(), labelFont, valueFont);
            addTableRow(table, "GONDEREN IBAN", transaction.getFromIban(), labelFont, valueFont);
            addTableRow(table, "ALICI IBAN", transaction.getToIban(), labelFont, valueFont);
            addTableRow(table, "TUTAR", transaction.getAmount() + " " + transaction.getCurrency(), labelFont, valueFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String formattedDate = transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(formatter) : "-";
            addTableRow(table, "TARIH", formattedDate, labelFont, valueFont);

            String statusText = transaction.getStatus() != null ? transaction.getStatus().toUpperCase() : "BASARILI";
            addTableRow(table, "DURUM", statusText, labelFont, successFont);

            document.add(table);

            // Alt Bilgi
            Paragraph footer = new Paragraph("\n\nBu belge sistem tarafından otomatik üretilmiştir.",
                    FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.LIGHT_GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("PDF oluşturma hatası: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setBorder(Rectangle.BOTTOM);
        cell1.setPadding(10);
        cell1.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        cell2.setBorder(Rectangle.BOTTOM);
        cell2.setPadding(10);
        cell2.setBorderColor(BaseColor.LIGHT_GRAY);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}