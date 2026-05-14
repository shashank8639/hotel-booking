package com.hotelbooking.service;

import com.hotelbooking.config.PaymentProperties;
import com.hotelbooking.exception.PaymentValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Resolves FX rates and converts amounts into the hotel base currency (snapshot at order time).
 */
@Service
@RequiredArgsConstructor
public class FxRateService {

    private final PaymentProperties paymentProperties;

    public BigDecimal rateToBase(String currency) {
        String code = normalize(currency);
        BigDecimal rate = paymentProperties.getFxRatesToBase().get(code);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Unsupported currency for FX: " + code);
        }
        return rate;
    }

    public BigDecimal toBase(BigDecimal amountInCurrency, String currency) {
        return amountInCurrency.multiply(rateToBase(currency)).setScale(2, RoundingMode.HALF_UP);
    }

    public String baseCurrency() {
        return normalize(paymentProperties.getBaseCurrency());
    }

    private static String normalize(String currency) {
        if (currency == null || currency.isBlank()) {
            return "INR";
        }
        return currency.trim().toUpperCase();
    }
}
