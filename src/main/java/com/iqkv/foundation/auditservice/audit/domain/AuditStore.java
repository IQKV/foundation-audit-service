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

package com.iqkv.foundation.auditservice.audit.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port for audit log persistence.
 */
public interface AuditStore {

  /**
   * Saves an audit record.
   *
   * @param record the record to save
   */
  void save(AuditRecord record);

  /**
   * Finds a record by its ID.
   *
   * @param id the record identifier
   * @return the record if found
   */
  Optional<AuditRecord> findById(UUID id);

  /**
   * Finds records with pagination and filtering.
   *
   * @param tenantKey optional tenant filter
   * @param action    optional action filter (e.g., "auth.signin.attempt")
   * @param severity  optional severity filter (e.g., "HIGH" or "CRITICAL")
   * @param pageable  pagination info
   * @return page of records
   */
  Page<AuditRecord> findAllByTenantKey(String tenantKey, String action, String severity, Pageable pageable);

  /**
   * Counts records grouped by action.
   *
   * @param tenantKey optional tenant filter
   * @return list of action counts
   */
  List<AuditActionCount> countByAction(String tenantKey);
}
