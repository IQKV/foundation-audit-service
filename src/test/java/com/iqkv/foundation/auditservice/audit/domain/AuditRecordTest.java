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

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.iqkv.foundation.audit.model.enums.ActivitySeverity;
import org.junit.jupiter.api.Test;

class AuditRecordTest {

  @Test
  void shouldCreateAuditRecordWithProvidedValues() {
    final UUID id = UUID.randomUUID();
    final Instant occurredAt = Instant.now().minusSeconds(3600);
    final Map<String, Object> details = Map.of("key", "value");

    final AuditRecord record = new AuditRecord(
        id,
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorUa",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        details,
        occurredAt,
        "correlationId"
    );

    assertEquals(id, record.getId());
    assertEquals("action", record.getAction());
    assertEquals("entityType", record.getEntityType());
    assertEquals("entityId", record.getEntityId());
    assertEquals("actorId", record.getActorId());
    assertEquals("actorType", record.getActorType());
    assertEquals("actorEmail", record.getActorEmail());
    assertEquals("actorIp", record.getActorIp());
    assertEquals("actorUa", record.getActorUa());
    assertEquals("impersonatorId", record.getImpersonatorId());
    assertEquals("tenantKey", record.getTenantKey());
    assertEquals(ActivitySeverity.INFO, record.getSeverity());
    assertEquals(details, record.getDetails());
    assertEquals(occurredAt, record.getOccurredAt());
    assertEquals("correlationId", record.getCorrelationId());
  }

  @Test
  void shouldGenerateIdWhenNull() {
    final AuditRecord record = new AuditRecord(
        null,
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorUa",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        Map.of(),
        Instant.now(),
        "correlationId"
    );

    assertNotNull(record.getId());
  }

  @Test
  void shouldGenerateOccurredAtWhenNull() {
    final AuditRecord record = new AuditRecord(
        UUID.randomUUID(),
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorUa",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        Map.of(),
        null,
        "correlationId"
    );

    assertNotNull(record.getOccurredAt());
  }

  @Test
  void shouldUseEmptyMapWhenDetailsNull() {
    final AuditRecord record = new AuditRecord(
        UUID.randomUUID(),
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorUa",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        null,
        Instant.now(),
        "correlationId"
    );

    assertEquals(Map.of(), record.getDetails());
  }

  @Test
  void shouldTestRecordEquality() {
    final UUID id = UUID.randomUUID();
    final Instant occurredAt = Instant.now();

    final AuditRecord record1 = new AuditRecord(
        id,
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorUa",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        Map.of(),
        occurredAt,
        "correlationId"
    );

    final AuditRecord record2 = new AuditRecord(
        id,
        "action",
        "entityType",
        "entityId",
        "actorId",
        "actorType",
        "actorEmail",
        "actorIp",
        "actorEmail",
        "impersonatorId",
        "tenantKey",
        ActivitySeverity.INFO,
        Map.of(),
        occurredAt,
        "correlationId"
    );

    assertNotEquals(record1.getActorUa(), record2.getActorUa());
  }
}
