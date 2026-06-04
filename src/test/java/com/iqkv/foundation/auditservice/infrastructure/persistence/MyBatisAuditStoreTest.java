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

package com.iqkv.foundation.auditservice.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.audit.model.enums.ActivitySeverity;
import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import com.iqkv.foundation.auditservice.audit.domain.AuditRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class MyBatisAuditStoreTest {

  @Mock
  private AuditMapper auditMapper;

  private MyBatisAuditStore myBatisAuditStore;

  @BeforeEach
  void setUp() {
    myBatisAuditStore = new MyBatisAuditStore(auditMapper);
  }

  @Test
  void shouldSaveAuditRecord() {
    final AuditRecord record = createTestRecord();
    myBatisAuditStore.save(record);
    verify(auditMapper).insert(record);
  }

  @Test
  void shouldFindById() {
    final UUID id = UUID.randomUUID();
    final AuditRecord record = createTestRecord();
    when(auditMapper.findById(id)).thenReturn(Optional.of(record));

    final Optional<AuditRecord> found = myBatisAuditStore.findById(id);

    assertTrue(found.isPresent());
    assertEquals(record, found.get());
  }

  @Test
  void shouldFindAllByTenantKey() {
    final String tenantKey = "test-tenant";
    final String action = "login";
    final PageRequest pageable = PageRequest.of(0, 10);
    final AuditRecord record = createTestRecord();

    when(auditMapper.findAll(tenantKey, action, "occurred_at", "DESC", 0, 10)).thenReturn(List.of(record));
    when(auditMapper.count(tenantKey, action)).thenReturn(1L);

    final var result = myBatisAuditStore.findAllByTenantKey(tenantKey, action, pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(record, result.getContent().get(0));
  }

  @Test
  void shouldFindAllByTenantKeyWithSort() {
    final String tenantKey = "test-tenant";
    final String action = "login";
    final PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "actor_id"));
    final AuditRecord record = createTestRecord();

    when(auditMapper.findAll(tenantKey, action, "actor_id", "ASC", 0, 10)).thenReturn(List.of(record));
    when(auditMapper.count(tenantKey, action)).thenReturn(1L);

    final var result = myBatisAuditStore.findAllByTenantKey(tenantKey, action, pageable);

    assertEquals(1, result.getTotalElements());
  }

  @Test
  void shouldCountByAction() {
    final String tenantKey = "test-tenant";
    final List<AuditActionCount> counts = List.of(new AuditActionCount("login", 42));
    when(auditMapper.countByAction(tenantKey)).thenReturn(counts);

    final var result = myBatisAuditStore.countByAction(tenantKey);

    assertEquals(counts, result);
  }

  private AuditRecord createTestRecord() {
    return new AuditRecord(
        UUID.randomUUID(),
        "login",
        "user",
        "123",
        "user123",
        "user",
        "user@example.com",
        "127.0.0.1",
        "Mozilla",
        null,
        "test-tenant",
        ActivitySeverity.INFO,
        Map.of(),
        Instant.now(),
        "corr-123"
    );
  }
}
