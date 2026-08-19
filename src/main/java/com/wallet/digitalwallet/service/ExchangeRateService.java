package com.wallet.digitalwallet.service;

import com.wallet.digitalwallet.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestTemplate restTemplate;

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        String url = "https://open.er-api.com/v6/latest/" + fromCurrency.toUpperCase();

        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);

            if (response != null) {
                Map<String, BigDecimal> rates = response.getRates() != null ? response.getRates() : response.getConversionRates();
                if (rates != null && rates.containsKey(toCurrency.toUpperCase())) {
                    return rates.get(toCurrency.toUpperCase());
                }
            }
        } catch (Exception e) {
            System.err.println("Doviz API baglanti hatasi: " + e.getMessage());
        }
        return getFallbackRate(fromCurrency, toCurrency);
    }

    public BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getFallbackRate(String from, String to) {
        String pair = from.toUpperCase() + "_" + to.toUpperCase();
        return switch (pair) {
            case "TRY_USD" -> new BigDecimal("0.030");
            case "USD_TRY" -> new BigDecimal("33.33");
            case "TRY_EUR" -> new BigDecimal("0.028");
            case "EUR_TRY" -> new BigDecimal("35.71");
            case "USD_EUR" -> new BigDecimal("0.92");
            case "EUR_USD" -> new BigDecimal("1.08");
            default -> BigDecimal.ONE;
        };
    }
}