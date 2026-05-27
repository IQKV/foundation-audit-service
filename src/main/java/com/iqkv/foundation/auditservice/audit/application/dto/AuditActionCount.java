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

package com.iqkv.foundation.auditservice.audit.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for audit action counts.
 */
@Schema(description = "Count of audit records for a specific action")
public record AuditActionCount(
    @Schema(description = "The audit action type", example = "login")
    String action,
    @Schema(description = "Number of occurrences", example = "42")
    long count
) {}
