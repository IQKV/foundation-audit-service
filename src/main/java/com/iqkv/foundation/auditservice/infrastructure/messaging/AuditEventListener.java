/*
 * Copyright 2026 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqkv.foundation.auditservice.infrastructure.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.iqkv.foundation.audit.model.enums.ActivitySeverity;
import com.iqkv.foundation.audit.model.event.AuditActor;
import com.iqkv.foundation.audit.model.event.AuditEvent;
import com.iqkv.foundation.audit.spi.AuditLogService;
import com.iqkv.foundation.auditservice.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes events from the shared platform exchange and transforms them into audit logs.
 */
@Component
@ConditionalOnProperty(name = "iqkv.messaging.rabbitmq.enabled", havingValue = "true")
public class AuditEventListener {

  private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

  private final AuditLogService auditLogService;

  public AuditEventListener(final AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
  public void onEvent(final Map<String, Object> payload,
                      @Header("amqp_receivedRoutingKey") final String routingKey) {
    log.debug("Received event for auditing: routingKey={}, payload={}", routingKey, payload);

    try {
      final AuditEvent auditEvent = transform(payload, routingKey);
      auditLogService.log(auditEvent);
    } catch (final Exception e) {
      log.error("Failed to process audit event from routingKey={}", routingKey, e);
      // In production, this would go to DLQ via x-dead-letter-exchange
      throw e;
    }
  }

  /**
   * Transforms a generic domain event payload into a normalized AuditEvent.
   * This logic will grow as more event types are supported.
   */
  @SuppressWarnings("unchecked")
  private AuditEvent transform(final Map<String, Object> payload, final String routingKey) {
    // 1. Check if it's already a standard AuditEvent
    if (routingKey.startsWith("audit.")) {
       // logic for direct AuditEvent if needed
    }

    // 2. Generic transformation logic
    final String action = (String) payload.getOrDefault("eventType", routingKey);
    final String tenantKey = payload.get("tenantKey") != null
        ? (String) payload.get("tenantKey")
        : (String) payload.get("tenantId"); // UserEvent uses "tenantId" instead of "tenantKey"
    final String occurredAtStr = (String) payload.get("occurredAt");
    final Instant occurredAt = occurredAtStr != null ? Instant.parse(occurredAtStr) : Instant.now();

    // Extract actor if present (injected via MessagingService enrichment)
    final Map<String, Object> actorMap = (Map<String, Object>) payload.get("actor");
    AuditActor actor = null;
    if (actorMap != null) {
      actor = new AuditActor(
          (String) actorMap.get("id"),
          (String) actorMap.get("type"),
          (String) actorMap.get("email"),
          (String) actorMap.get("ipAddress"),
          (String) actorMap.get("userAgent"),
          (String) actorMap.get("impersonatorId")
      );
    }

    return new AuditEvent(
        UUID.randomUUID(),
        action,
        inferEntityType(routingKey),
        inferEntityId(payload),
        actor,
        tenantKey,
        inferSeverity(action),
        payload, // Store full payload as details
        occurredAt,
        (String) payload.get("correlationId")
    );
  }

  private String inferEntityType(final String routingKey) {
    if (routingKey.startsWith("user.")) return "USER";
    if (routingKey.startsWith("tenant.")) return "TENANT";
    if (routingKey.startsWith("subscription.")) return "SUBSCRIPTION";
    if (routingKey.startsWith("invoice.")) return "INVOICE";
    if (routingKey.startsWith("payment.")) return "INVOICE";
    if (routingKey.startsWith("refund.")) return "INVOICE";
    if (routingKey.startsWith("auth.")) return "AUTHENTICATION";
    return "UNKNOWN";
  }

  private String inferEntityId(final Map<String, Object> payload) {
    // Billing — subscription events
    if (payload.containsKey("externalSubscriptionId")) return String.valueOf(payload.get("externalSubscriptionId"));
    // Billing — invoice / payment / refund events
    if (payload.containsKey("externalInvoiceId")) return String.valueOf(payload.get("externalInvoiceId"));
    if (payload.containsKey("externalRefundId")) return String.valueOf(payload.get("externalRefundId"));
    // IAM — user events
    if (payload.containsKey("userId")) return String.valueOf(payload.get("userId"));
    if (payload.containsKey("tenantId")) return String.valueOf(payload.get("tenantId"));
    // Signin attempts use email as the identifier
    if (payload.containsKey("email")) return String.valueOf(payload.get("email"));
    if (payload.containsKey("id")) return String.valueOf(payload.get("id"));
    return null;
  }

  private ActivitySeverity inferSeverity(final String action) {
    // Bans are critical security actions
    if (action.contains("BANNED")) {
      return ActivitySeverity.CRITICAL;
    }
    // Payment failures and refunds are high-severity financial events
    if (action.contains("PAYMENT_FAILED") || action.contains("REFUND")) {
      return ActivitySeverity.HIGH;
    }
    // Credential changes — always high
    if (action.contains("PASSWORD_CHANGED") || action.contains("PASSWORD_RESET_COMPLETED")) {
      return ActivitySeverity.HIGH;
    }
    if (action.contains("DELETE") || action.contains("CANCELLED") || action.contains("SUSPENDED")) {
      return ActivitySeverity.HIGH;
    }
    // Password reset initiated, account unlocked, unban — medium
    if (action.contains("PASSWORD_RESET_INITIATED") || action.contains("UNLOCKED") || action.contains("UNBANNED")) {
      return ActivitySeverity.MEDIUM;
    }
    if (action.contains("FAILED")) {
      return ActivitySeverity.MEDIUM;
    }
    if (action.contains("STATUS_CHANGED") || action.contains("UPDATE") || action.contains("UPDATED")) {
      return ActivitySeverity.INFO;
    }
    if (action.contains("CREATED") || action.contains("PAID") || action.contains("FINALIZED")) {
      return ActivitySeverity.INFO;
    }
    // Signin attempts — failed are medium, successful are low
    if (action.contains("SIGNIN") || action.contains("LOGIN")) {
      return ActivitySeverity.MEDIUM;
    }
    return ActivitySeverity.LOW;
  }
}
