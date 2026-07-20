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

package com.iqkv.foundation.auditservice.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthConfigurationPropertiesTest {

  @Test
  void shouldCreateWithPublicKeyPath() {
    final var jwt = new AuthConfigurationProperties.Jwt(null, "classpath:keys/public.pem");
    final var props = new AuthConfigurationProperties(jwt);

    assertNotNull(props.jwt());
    assertNull(props.jwt().jwksUri());
    assertEquals("classpath:keys/public.pem", props.jwt().publicKeyPath());
  }

  @Test
  void shouldCreateWithJwksUri() {
    final var jwt = new AuthConfigurationProperties.Jwt(
        "http://foundation-iam-service/.well-known/jwks.json", null);
    final var props = new AuthConfigurationProperties(jwt);

    assertNotNull(props.jwt());
    assertEquals("http://foundation-iam-service/.well-known/jwks.json", props.jwt().jwksUri());
    assertNull(props.jwt().publicKeyPath());
  }

  @Test
  void validateShouldPassWithPublicKeyPath() {
    final var props = new AuthConfigurationProperties(
        new AuthConfigurationProperties.Jwt(null, "classpath:keys/public.pem"));
    assertDoesNotThrow(props::validate);
  }

  @Test
  void validateShouldPassWithJwksUri() {
    final var props = new AuthConfigurationProperties(
        new AuthConfigurationProperties.Jwt(
            "http://foundation-iam-service/.well-known/jwks.json", null));
    assertDoesNotThrow(props::validate);
  }

  @Test
  void validateShouldFailWhenBothSet() {
    final var props = new AuthConfigurationProperties(
        new AuthConfigurationProperties.Jwt(
            "http://foundation-iam-service/.well-known/jwks.json",
            "classpath:keys/public.pem"));
    assertThrows(IllegalStateException.class, props::validate);
  }

  @Test
  void validateShouldFailWhenNeitherSet() {
    final var props = new AuthConfigurationProperties(
        new AuthConfigurationProperties.Jwt(null, null));
    assertThrows(IllegalStateException.class, props::validate);
  }

  @Test
  void validateShouldFailWhenBothBlank() {
    final var props = new AuthConfigurationProperties(
        new AuthConfigurationProperties.Jwt("", ""));
    assertThrows(IllegalStateException.class, props::validate);
  }

  @Test
  void shouldTestEquality() {
    final var jwt1 = new AuthConfigurationProperties.Jwt(null, "path1");
    final var jwt2 = new AuthConfigurationProperties.Jwt(null, "path1");
    final var jwt3 = new AuthConfigurationProperties.Jwt(null, "path2");

    assertEquals(jwt1, jwt2);
    assertNotEquals(jwt1, jwt3);

    final var props1 = new AuthConfigurationProperties(jwt1);
    final var props2 = new AuthConfigurationProperties(jwt1);
    final var props3 = new AuthConfigurationProperties(jwt3);

    assertEquals(props1, props2);
    assertNotEquals(props1, props3);
  }
}
