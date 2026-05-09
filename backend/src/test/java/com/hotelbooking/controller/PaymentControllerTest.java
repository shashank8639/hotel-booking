package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.CreatePaymentOrderResponse;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.dto.PaymentResponse;
import com.hotelbooking.dto.RefundPaymentRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.InvalidPaymentSignatureException;
import com.hotelbooking.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new ByteArrayHttpMessageConverter(),
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createOrder_shouldReturn200() throws Exception {
        when(paymentService.createOrder(any())).thenReturn(
                CreatePaymentOrderResponse.builder()
                        .paymentId(1L)
                        .bookingId(10L)
                        .razorpayOrderId("order_1")
                        .amount(new BigDecimal("5000.00"))
                        .currency("INR")
                        .status("PENDING")
                        .build()
        );

        mockMvc.perform(post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CreatePaymentOrderRequest.builder().bookingId(10L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razorpayOrderId").value("order_1"));
    }

    @Test
    void verify_shouldReturn400OnBadSignature() throws Exception {
        when(paymentService.verifyPayment(any())).thenThrow(new InvalidPaymentSignatureException());

        mockMvc.perform(post("/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VerifyPaymentRequest.builder()
                                .razorpayOrderId("o")
                                .razorpayPaymentId("p")
                                .razorpaySignature("s")
                                .build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void history_shouldReturnPage() throws Exception {
        when(paymentService.getPaymentHistory(isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(
                        List.of(PaymentResponse.builder().id(1L).status(PaymentStatus.SUCCESS).build()),
                        PageRequest.of(0, 10),
                        1
                ));

        mockMvc.perform(get("/payments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void refund_shouldReturn200() throws Exception {
        when(paymentService.refund(any())).thenReturn(
                PaymentResponse.builder().id(1L).status(PaymentStatus.REFUNDED).build()
        );

        mockMvc.perform(post("/payments/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                RefundPaymentRequest.builder().paymentId(1L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void webhook_shouldReturn200() throws Exception {
        doNothing().when(paymentService).handleWebhook(anyString(), anyString());

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Razorpay-Signature", "sig")
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void webhook_shouldReturn400OnBadSignature() throws Exception {
        doThrow(new InvalidPaymentSignatureException())
                .when(paymentService).handleWebhook(anyString(), anyString());

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Razorpay-Signature", "bad")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invoicePdf_shouldReturnPdfBytes() throws Exception {
        when(paymentService.getInvoicePdf(10L)).thenReturn("%PDF-1.4 mock".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/payments/invoice/pdf/10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void invoice_shouldReturnJson() throws Exception {
        when(paymentService.getInvoice(10L)).thenReturn(
                InvoiceResponse.builder().invoiceNumber("INV-1").bookingId(10L).build()
        );

        mockMvc.perform(get("/payments/invoice/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-1"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(paymentService.getPaymentById(5L)).thenReturn(
                PaymentResponse.builder().id(5L).status(PaymentStatus.PENDING).build()
        );

        mockMvc.perform(get("/payments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
