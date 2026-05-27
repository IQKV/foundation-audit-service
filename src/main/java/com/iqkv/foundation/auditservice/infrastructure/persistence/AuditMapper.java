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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.iqkv.foundation.auditservice.audit.application.dto.AuditActionCount;
import com.iqkv.foundation.auditservice.audit.domain.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis Mapper for audit logs.
 */
@Mapper
public interface AuditMapper {

  void insert(AuditRecord record);

  Optional<AuditRecord> findById(UUID id);

  List<AuditRecord> findAll(@Param("tenantKey") String tenantKey,
                            @Param("sortBy") String sortBy,
                            @Param("sortDir") String sortDir,
                            @Param("offset") long offset,
                            @Param("limit") int limit);

  long count(@Param("tenantKey") String tenantKey);

  List<AuditActionCount> countByAction(@Param("tenantKey") String tenantKey);
}
