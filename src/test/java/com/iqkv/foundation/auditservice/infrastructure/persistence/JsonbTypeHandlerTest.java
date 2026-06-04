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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class JsonbTypeHandlerTest {

  @Mock
  private PreparedStatement preparedStatement;

  @Mock
  private ResultSet resultSet;

  @Mock
  private CallableStatement callableStatement;

  private JsonbTypeHandler jsonbTypeHandler;

  @BeforeEach
  void setUp() {
    jsonbTypeHandler = new JsonbTypeHandler(new JsonMapper());
  }

  @Test
  void shouldSetNonNullParameter() throws SQLException {
    final Map<String, Object> parameter = Map.of("key", "value");
    jsonbTypeHandler.setNonNullParameter(preparedStatement, 1, parameter, null);
    verify(preparedStatement).setObject(eq(1), any());
  }

  @Test
  void shouldGetNullableResultFromResultSetColumnName() throws SQLException {
    when(resultSet.getString("column")).thenReturn("{\"key\":\"value\"}");
    final var result = jsonbTypeHandler.getNullableResult(resultSet, "column");
    assertEquals(Map.of("key", "value"), result);
  }

  @Test
  void shouldGetNullableResultFromResultSetColumnIndex() throws SQLException {
    when(resultSet.getString(1)).thenReturn("{\"key\":\"value\"}");
    final var result = jsonbTypeHandler.getNullableResult(resultSet, 1);
    assertEquals(Map.of("key", "value"), result);
  }

  @Test
  void shouldGetNullableResultFromCallableStatement() throws SQLException {
    when(callableStatement.getString(1)).thenReturn("{\"key\":\"value\"}");
    final var result = jsonbTypeHandler.getNullableResult(callableStatement, 1);
    assertEquals(Map.of("key", "value"), result);
  }

  @Test
  void shouldReturnEmptyMapWhenJsonIsNull() throws SQLException {
    when(resultSet.getString("column")).thenReturn(null);
    final var result = jsonbTypeHandler.getNullableResult(resultSet, "column");
    assertEquals(Map.of(), result);
  }

  @Test
  void shouldReturnEmptyMapWhenJsonIsBlank() throws SQLException {
    when(resultSet.getString("column")).thenReturn("   ");
    final var result = jsonbTypeHandler.getNullableResult(resultSet, "column");
    assertEquals(Map.of(), result);
  }
}
