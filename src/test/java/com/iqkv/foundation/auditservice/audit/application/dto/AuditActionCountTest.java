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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuditActionCountTest {

  @Test
  void shouldCreateAuditActionCountWithValues() {
    final AuditActionCount count = new AuditActionCount("login", 42);

    assertEquals("login", count.action());
    assertEquals(42, count.count());
  }

  @Test
  void shouldTestEquality() {
    final AuditActionCount count1 = new AuditActionCount("login", 42);
    final AuditActionCount count2 = new AuditActionCount("login", 42);
    final AuditActionCount count3 = new AuditActionCount("logout", 10);

    assertEquals(count1, count2);
    assertNotEquals(count1, count3);
  }

  @Test
  void shouldTestHashCode() {
    final AuditActionCount count1 = new AuditActionCount("login", 42);
    final AuditActionCount count2 = new AuditActionCount("login", 42);

    assertEquals(count1.hashCode(), count2.hashCode());
  }
}
