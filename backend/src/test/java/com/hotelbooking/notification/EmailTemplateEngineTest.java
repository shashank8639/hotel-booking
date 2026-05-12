package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateEngineTest {

    private PlaceholderEmailTemplateEngine engine;

    @BeforeEach
    void setUp() {
        MailProperties props = new MailProperties();
        props.setDefaultLocale("en");
        engine = new PlaceholderEmailTemplateEngine(props);
    }

    @Test
    void render_shouldIncludeHeaderFooterAndVariables() {
        String html = engine.render("booking-confirmation.html", java.util.Map.ofEntries(
                java.util.Map.entry("hotelName", "Grand Horizon Hotel"),
                java.util.Map.entry("supportEmail", "support@example.com"),
                java.util.Map.entry("guestName", "Asha Patel"),
                java.util.Map.entry("bookingId", "10"),
                java.util.Map.entry("bookingStatus", "CONFIRMED"),
                java.util.Map.entry("checkInDate", "01 Sep 2026"),
                java.util.Map.entry("checkOutDate", "03 Sep 2026"),
                java.util.Map.entry("numberOfNights", "2"),
                java.util.Map.entry("roomsSummary", "101 (STANDARD)"),
                java.util.Map.entry("totalAmount", "INR 5000.00")
        ));

        assertThat(html).contains("Grand Horizon Hotel");
        assertThat(html).contains("Booking Confirmed");
        assertThat(html).contains("Asha Patel");
        assertThat(html).contains("#10");
        assertThat(html).contains("support@example.com");
        assertThat(html).doesNotContain("{{guestName}}");
    }

    @Test
    void render_invoiceTemplate_shouldResolvePlaceholders() {
        String html = engine.render("invoice.html", java.util.Map.ofEntries(
                java.util.Map.entry("hotelName", "Grand Horizon Hotel"),
                java.util.Map.entry("supportEmail", "support@example.com"),
                java.util.Map.entry("guestName", "Asha"),
                java.util.Map.entry("bookingId", "10"),
                java.util.Map.entry("invoiceNumber", "INV-1"),
                java.util.Map.entry("checkInDate", "01 Sep 2026"),
                java.util.Map.entry("checkOutDate", "03 Sep 2026"),
                java.util.Map.entry("roomsSummary", "101"),
                java.util.Map.entry("subtotal", "INR 4200.00"),
                java.util.Map.entry("gstAmount", "INR 800.00"),
                java.util.Map.entry("totalAmount", "INR 5000.00"),
                java.util.Map.entry("razorpayPaymentId", "pay_1")
        ));

        assertThat(html).contains("INV-1");
        assertThat(html).contains("Tax Invoice");
    }

    @Test
    void render_shouldPickHindiCancellationTemplate() {
        String html = engine.render("booking-cancellation.html", java.util.Map.ofEntries(
                java.util.Map.entry("hotelName", "Grand Horizon Hotel"),
                java.util.Map.entry("supportEmail", "support@example.com"),
                java.util.Map.entry("guestName", "आशा"),
                java.util.Map.entry("bookingId", "10"),
                java.util.Map.entry("cancellationDate", "01 Aug 2026"),
                java.util.Map.entry("checkInDate", "01 Sep 2026"),
                java.util.Map.entry("checkOutDate", "03 Sep 2026"),
                java.util.Map.entry("roomsSummary", "101"),
                java.util.Map.entry("totalAmount", "INR 5000.00"),
                java.util.Map.entry("refundStatus", "Pending"),
                java.util.Map.entry("refundAmount", "INR 0.00")
        ), "hi");

        assertThat(html).contains("बुकिंग रद्द");
        assertThat(html).contains("आशा");
    }
}
