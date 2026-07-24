package com.wallet.digitalwallet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("rates")
    private Map<String, BigDecimal> rates;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;
}