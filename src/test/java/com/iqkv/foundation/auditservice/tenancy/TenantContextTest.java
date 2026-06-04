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

package com.iqkv.foundation.auditservice.tenancy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

  @BeforeEach
  void setUp() {
    TenantContext.clear();
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void shouldSetAndGetTenant() {
    TenantContext.setCurrentTenant("test-tenant");

    assertEquals("test-tenant", TenantContext.getCurrentTenant());
  }

  @Test
  void shouldThrowExceptionWhenSettingNullTenant() {
    assertThrows(IllegalArgumentException.class, () -> TenantContext.setCurrentTenant(null));
  }

  @Test
  void shouldThrowExceptionWhenSettingBlankTenant() {
    assertThrows(IllegalArgumentException.class, () -> TenantContext.setCurrentTenant(""));
    assertThrows(IllegalArgumentException.class, () -> TenantContext.setCurrentTenant("   "));
  }

  @Test
  void shouldThrowExceptionWhenGettingTenantBeforeSetting() {
    assertThrows(IllegalStateException.class, TenantContext::getCurrentTenant);
  }

  @Test
  void shouldClearTenantContext() {
    TenantContext.setCurrentTenant("test-tenant");
    TenantContext.clear();

    assertThrows(IllegalStateException.class, TenantContext::getCurrentTenant);
  }
}
