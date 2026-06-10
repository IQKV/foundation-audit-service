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
 *
 * <p>Implemented as a regular JavaBean (not a record) so that MyBatis can
 * populate it via setter injection. MyBatis's {@code <result>} mapping requires
 * setters; Java records have none, which causes {@code ReflectionException} at
 * query time.
 */
public class AuditRecord {

  private UUID id;
  private String action;
  private String entityType;
  private String entityId;
  private String actorId;
  private String actorType;
  private String actorEmail;
  private String actorIp;
  private String actorUa;
  private String impersonatorId;
  private String tenantKey;
  private ActivitySeverity severity;
  private Map<String, Object> details;
  private Instant occurredAt;
  private String correlationId;

  /**
   * No-arg constructor required by MyBatis.
   */
  public AuditRecord() {
  }

  /**
   * Full constructor for programmatic creation (e.g. from incoming audit events).
   */
  public AuditRecord(
      final UUID id,
      final String action,
      final String entityType,
      final String entityId,
      final String actorId,
      final String actorType,
      final String actorEmail,
      final String actorIp,
      final String actorUa,
      final String impersonatorId,
      final String tenantKey,
      final ActivitySeverity severity,
      final Map<String, Object> details,
      final Instant occurredAt,
      final String correlationId) {
    this.id = id != null ? id : UUID.randomUUID();
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.actorId = actorId;
    this.actorType = actorType;
    this.actorEmail = actorEmail;
    this.actorIp = actorIp;
    this.actorUa = actorUa;
    this.impersonatorId = impersonatorId;
    this.tenantKey = tenantKey;
    this.severity = severity;
    this.details = details != null ? details : Map.of();
    this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    this.correlationId = correlationId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getAction() {
    return action;
  }

  public void setAction(final String action) {
    this.action = action;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(final String entityType) {
    this.entityType = entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(final String entityId) {
    this.entityId = entityId;
  }

  public String getActorId() {
    return actorId;
  }

  public void setActorId(final String actorId) {
    this.actorId = actorId;
  }

  public String getActorType() {
    return actorType;
  }

  public void setActorType(final String actorType) {
    this.actorType = actorType;
  }

  public String getActorEmail() {
    return actorEmail;
  }

  public void setActorEmail(final String actorEmail) {
    this.actorEmail = actorEmail;
  }

  public String getActorIp() {
    return actorIp;
  }

  public void setActorIp(final String actorIp) {
    this.actorIp = actorIp;
  }

  public String getActorUa() {
    return actorUa;
  }

  public void setActorUa(final String actorUa) {
    this.actorUa = actorUa;
  }

  public String getImpersonatorId() {
    return impersonatorId;
  }

  public void setImpersonatorId(final String impersonatorId) {
    this.impersonatorId = impersonatorId;
  }

  public String getTenantKey() {
    return tenantKey;
  }

  public void setTenantKey(final String tenantKey) {
    this.tenantKey = tenantKey;
  }

  public ActivitySeverity getSeverity() {
    return severity;
  }

  public void setSeverity(final ActivitySeverity severity) {
    this.severity = severity;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(final Map<String, Object> details) {
    this.details = details != null ? details : Map.of();
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(final Instant occurredAt) {
    this.occurredAt = occurredAt;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(final String correlationId) {
    this.correlationId = correlationId;
  }
}
