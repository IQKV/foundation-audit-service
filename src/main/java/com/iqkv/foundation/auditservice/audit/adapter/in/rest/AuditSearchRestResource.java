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

package com.iqkv.foundation.auditservice.audit.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.iqkv.foundation.auditservice.audit.application.AuditLogOrchestrator;
import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import com.iqkv.foundation.auditservice.audit.domain.AuditRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for searching and viewing audit logs.
 * Restricted to PLATFORM_ADMIN authority.
 */
@RestController
@RequestMapping("/api/v1/audit/admin/logs")
@Tag(name = "Audit Logs", description = "Administrative API for reviewing platform-wide activity logs")
public class AuditSearchRestResource {

  private final AuditLogOrchestrator orchestrator;

  public AuditSearchRestResource(final AuditLogOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
  @Operation(summary = "Search audit logs", description = "Returns a paginated list of audit records, optionally filtered by tenant, action type, and severity.")
  public ResponseEntity<Page<AuditRecord>> search(
      @Parameter(description = "Optional tenant key filter")
      @RequestParam(required = false) final String tenantKey,
      @Parameter(description = "Optional action filter (e.g., 'auth.signin.attempt' for signin attempts)")
      @RequestParam(required = false) final String action,
      @Parameter(description = "Optional severity filter: LOW | MEDIUM | HIGH | CRITICAL")
      @RequestParam(required = false) final String severity,
      final Pageable pageable) {
    return ResponseEntity.ok(orchestrator.searchRecords(tenantKey, action, severity, pageable));
  }

  @GetMapping("/stats/actions")
  @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
  @Operation(summary = "Get audit record counts by action", description = "Returns counts of audit records grouped by their action type.")
  public ResponseEntity<List<AuditActionCount>> getActionStats(
      @Parameter(description = "Optional tenant key filter")
      @RequestParam(required = false) final String tenantKey) {
    return ResponseEntity.ok(orchestrator.getCountsByAction(tenantKey));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
  @Operation(summary = "Get audit record details", description = "Returns the full details of a specific audit log entry by its UUID.")
  public ResponseEntity<AuditRecord> getDetails(
      @Parameter(description = "Audit record UUID")
      @PathVariable final UUID id) {
    return orchestrator.getRecord(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
