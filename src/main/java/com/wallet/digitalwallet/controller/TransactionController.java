package com.wallet.digitalwallet.controller;

import com.wallet.digitalwallet.entity.Transaction;
import com.wallet.digitalwallet.service.PdfService;
import com.wallet.digitalwallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final PdfService pdfService;
    private final TransactionService transactionService;

    // 1. Dekont İndirme Endpoint'i (Önceki Adımdan)
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        byte[] pdfBytes = pdfService.generateTransactionReceipt(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "dekont_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // 2. Sayfalamalı ve Dinamik Filtreli İşlem Geçmişi Arama Endpoint'i (Yeni)
    @GetMapping("/search")
    public ResponseEntity<Page<Transaction>> searchTransactions(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Transaction> transactions = transactionService.getFilteredTransactions(
                iban, minAmount, maxAmount, startDate, endDate, pageable
        );
        return ResponseEntity.ok(transactions);
    }
}