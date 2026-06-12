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

package com.iqkv.foundation.auditservice.audit.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.audit.model.event.AuditEvent;
import com.iqkv.foundation.audit.spi.AuditLogService;
import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import com.iqkv.foundation.auditservice.audit.domain.AuditRecord;
import com.iqkv.foundation.auditservice.audit.domain.AuditStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator for audit log operations.
 */
@Service
public class AuditLogOrchestrator implements AuditLogService {

  private static final Logger log = LoggerFactory.getLogger(AuditLogOrchestrator.class);

  private final AuditStore auditStore;

  public AuditLogOrchestrator(final AuditStore auditStore) {
    this.auditStore = auditStore;
  }

  @Override
  @Transactional
  public void log(final AuditEvent event) {
    log.debug("Processing audit event: id={}, action={}", event.id(), event.action());

    final var record = new AuditRecord(
        event.id(),
        event.action(),
        event.entityType(),
        event.entityId(),
        event.actor() != null ? event.actor().id() : null,
        event.actor() != null ? event.actor().type() : null,
        event.actor() != null ? event.actor().email() : null,
        event.actor() != null ? event.actor().ipAddress() : null,
        event.actor() != null ? event.actor().userAgent() : null,
        event.actor() != null ? event.actor().impersonatorId() : null,
        event.tenantKey(),
        event.severity(),
        event.details(),
        event.occurredAt(),
        event.correlationId()
    );

    auditStore.save(record);
  }

  @Transactional(readOnly = true)
  public Optional<AuditRecord> getRecord(final UUID id) {
    return auditStore.findById(id);
  }

  @Transactional(readOnly = true)
  public Page<AuditRecord> searchRecords(final String tenantKey, final String action, final String severity, final Pageable pageable) {
    return auditStore.findAllByTenantKey(tenantKey, action, severity, pageable);
  }

  @Transactional(readOnly = true)
  public List<AuditActionCount> getCountsByAction(final String tenantKey) {
    return auditStore.countByAction(tenantKey);
  }
}
