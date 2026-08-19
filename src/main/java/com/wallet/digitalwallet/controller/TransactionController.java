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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final PdfService pdfService;
    private final TransactionService transactionService;

    
    @GetMapping("/my")
    public ResponseEntity<Page<Transaction>> getMyTransactions(
            Principal principal,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Transaction> transactions = transactionService.getMyFilteredTransactions(
                principal.getName(), minAmount, maxAmount, startDate, endDate, pageable
        );
        return ResponseEntity.ok(transactions);
    }

    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getReceipt(
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String action) {

        byte[] pdfBytes = pdfService.generateTransactionReceipt(id);

        boolean isDownload = "download".equalsIgnoreCase(action) || "attachment".equalsIgnoreCase(action);

        ContentDisposition contentDisposition = (isDownload ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename("dekont_" + id + ".pdf")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    
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