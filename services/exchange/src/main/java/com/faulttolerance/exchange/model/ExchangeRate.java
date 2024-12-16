package com.faulttolerance.exchange.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "exchange_rates")
public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDateTime timestamp;

    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, LocalDateTime timestamp) {
        this.id = fromCurrency + "-" + toCurrency;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
