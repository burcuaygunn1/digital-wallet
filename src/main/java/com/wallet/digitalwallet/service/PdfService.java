package com.wallet.digitalwallet.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.exception.ResourceNotFoundException;
import com.wallet.digitalwallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TransactionRepository transactionRepository;

    public byte[] generateTransactionReceipt(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("İşlem kaydı bulunamadı: " + transactionId));

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Başlık
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("DIGITAL WALLET - TRANSFER DEKONTU", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Çizgi
            Paragraph line = new Paragraph("____________________________________________________");
            line.setSpacingAfter(20);
            document.add(line);

            // Tablo Oluşturma
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);

            addTableRow(table, "Islem No:", String.valueOf(transaction.getId()), labelFont, valueFont);
            addTableRow(table, "Gonderen IBAN:", transaction.getFromIban(), labelFont, valueFont);
            addTableRow(table, "Alici IBAN:", transaction.getToIban(), labelFont, valueFont);
            addTableRow(table, "Tutar:", transaction.getAmount() + " " + transaction.getCurrency(), labelFont, valueFont);
            addTableRow(table, "Islem Tipi:", transaction.getTransactionType(), labelFont, valueFont);
            addTableRow(table, "Durum:", transaction.getStatus(), labelFont, valueFont);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedDate = transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(formatter) : "-";
            addTableRow(table, "Tarih:", formattedDate, labelFont, valueFont);

            document.add(table);

            // Alt Bilgi
            Paragraph footer = new Paragraph("\n\nBu belge Digital Wallet sistemi tarafindan otomatik uretilmistir.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("PDF olusturulurken hata meydana geldi", e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setPadding(8);
        cell1.setBorder(Rectangle.NO_BORDER);

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        cell2.setPadding(8);
        cell2.setBorder(Rectangle.NO_BORDER);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}