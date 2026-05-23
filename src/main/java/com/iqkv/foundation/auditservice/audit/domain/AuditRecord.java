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

package com.iqkv.foundation.auditservice.audit.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.iqkv.foundation.audit.model.enums.ActivitySeverity;

/**
 * Domain entity representing a persisted audit log entry.
 */
public record AuditRecord(
    UUID id,
    String action,
    String entityType,
    String entityId,
    String actorId,
    String actorType,
    String actorEmail,
    String actorIp,
    String actorUa,
    String impersonatorId,
    String tenantKey,
    ActivitySeverity severity,
    Map<String, Object> details,
    Instant occurredAt,
    String correlationId
) {
  public AuditRecord {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (occurredAt == null) {
      occurredAt = Instant.now();
    }
    if (details == null) {
      details = Map.of();
    }
  }
}
