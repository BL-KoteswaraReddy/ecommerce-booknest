package com.ecommerce.client;

import com.ecommerce.exception.OrderServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletServiceClient {

    private final WebClient webClient;

    @Value("${services.wallet.url}")
    private String walletServiceUrl;


    @Value("${app.internal.secret}")   // ← Add this
    private String internalSecret;


    public boolean processWalletPayment(Long userId, Double amount, String orderNumber, String userEmail) {
        try {
            WalletPaymentRequest request = new WalletPaymentRequest(userId, amount, orderNumber, userEmail);

            WalletPaymentResponse response = webClient.post()
                    .uri(walletServiceUrl + "/api/wallet/pay")
                    .header("X-Internal-Secret", internalSecret)  //
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletPaymentResponse.class)
                    .block();

            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("Error processing wallet payment: {}", e.getMessage());
            throw new OrderServiceException("Failed to process wallet payment");
        }
    }

    // Inner classes moved to class level (outside the method)
    @Data
    @AllArgsConstructor
    public static class WalletPaymentRequest {
        private Long userId;
        private Double amount;
        private String orderNumber;
        private String userEmail;
    }

    @Data
    public static class WalletPaymentResponse {
        private boolean success;
        private String message;
    }
}
