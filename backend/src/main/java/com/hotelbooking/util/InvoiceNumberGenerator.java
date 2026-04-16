package com.hotelbooking.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique invoice numbers without external dependencies.
 */
public final class InvoiceNumberGenerator {

    private static final AtomicInteger SEQ = new AtomicInteger(1);
    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private InvoiceNumberGenerator() {
    }

    public static String next() {
        return "INV-" + LocalDate.now().format(DAY) + "-" + String.format("%04d", SEQ.getAndIncrement());
    }
}
