package com.example.VaultPay.model.stripe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stripe_webhook_events")
public class StripeWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stripeEventId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean processed = false;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }


}
