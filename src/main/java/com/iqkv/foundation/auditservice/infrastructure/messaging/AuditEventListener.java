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
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes events from the shared platform exchange and transforms them into audit logs.
 */
@Component
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
    final String tenantKey = (String) payload.get("tenantKey");
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
    if (routingKey.startsWith("billing.")) return "BILLING";
    if (routingKey.startsWith("invoice.")) return "INVOICE";
    if (routingKey.startsWith("auth.")) return "AUTHENTICATION";
    return "UNKNOWN";
  }

  private String inferEntityId(final Map<String, Object> payload) {
    if (payload.containsKey("userId")) return String.valueOf(payload.get("userId"));
    if (payload.containsKey("tenantId")) return String.valueOf(payload.get("tenantId"));
    if (payload.containsKey("email")) return String.valueOf(payload.get("email")); // For signin attempts
    if (payload.containsKey("id")) return String.valueOf(payload.get("id"));
    return null;
  }

  private ActivitySeverity inferSeverity(final String action) {
    if (action.contains("DELETE") || action.contains("FAILED") || action.contains("SUSPENDED")) {
      return ActivitySeverity.HIGH;
    }
    if (action.contains("UPDATE") || action.contains("CREATED")) {
      return ActivitySeverity.INFO;
    }
    // Signin attempts - failed attempts are medium severity, successful are low
    if (action.contains("SIGNIN") || action.contains("LOGIN")) {
      return ActivitySeverity.MEDIUM;
    }
    return ActivitySeverity.LOW;
  }
}
