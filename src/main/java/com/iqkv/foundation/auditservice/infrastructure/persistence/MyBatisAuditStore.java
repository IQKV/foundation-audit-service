/*
 * Copyright 2026 iQKV Foundation Team.
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import com.iqkv.foundation.auditservice.audit.domain.AuditRecord;
import com.iqkv.foundation.auditservice.audit.domain.AuditStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * MyBatis implementation of the {@link AuditStore} port.
 */
@Repository
public class MyBatisAuditStore implements AuditStore {

  private final AuditMapper auditMapper;

  public MyBatisAuditStore(final AuditMapper auditMapper) {
    this.auditMapper = auditMapper;
  }

  @Override
  public void save(final AuditRecord record) {
    auditMapper.insert(record);
  }

  @Override
  public Optional<AuditRecord> findById(final UUID id) {
    return auditMapper.findById(id);
  }

  @Override
  public Page<AuditRecord> findAllByTenantKey(final String tenantKey, final String action, final String severity, final Pageable pageable) {
    String sortBy = "occurred_at";
    String sortDir = "DESC";

    if (pageable.getSort().isSorted()) {
      final var order = pageable.getSort().iterator().next();
      sortBy = order.getProperty();
      sortDir = order.getDirection().name();
    }

    final var records = auditMapper.findAll(tenantKey, action, severity, sortBy, sortDir, pageable.getOffset(), pageable.getPageSize());
    final var total = auditMapper.count(tenantKey, action, severity);
    return new PageImpl<>(records, pageable, total);
  }

  @Override
  public List<AuditActionCount> countByAction(final String tenantKey) {
    return auditMapper.countByAction(tenantKey);
  }
}
