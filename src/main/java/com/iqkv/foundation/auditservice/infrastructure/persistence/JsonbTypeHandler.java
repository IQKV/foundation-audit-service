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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

/**
 * MyBatis TypeHandler for PostgreSQL JSONB fields.
 * Uses Jackson for serialization/deserialization.
 */
@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

  private final JsonMapper jsonMapper;

  public JsonbTypeHandler() {
    this(new JsonMapper());
  }

  public JsonbTypeHandler(final JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void setNonNullParameter(final PreparedStatement ps, final int i,
                                  final Map<String, Object> parameter, final JdbcType jdbcType)
      throws SQLException {
    final var jsonObject = new PGobject();
    jsonObject.setType("jsonb");
    try {
      jsonObject.setValue(jsonMapper.writeValueAsString(parameter));
    } catch (final JacksonException e) {
      throw new SQLException("Error mapping Map to JSONB", e);
    }
    ps.setObject(i, jsonObject);
  }

  @Override
  public Map<String, Object> getNullableResult(final ResultSet rs, final String columnName)
      throws SQLException {
    return parseJson(rs.getString(columnName));
  }

  @Override
  public Map<String, Object> getNullableResult(final ResultSet rs, final int columnIndex)
      throws SQLException {
    return parseJson(rs.getString(columnIndex));
  }

  @Override
  public Map<String, Object> getNullableResult(final CallableStatement cs, final int columnIndex)
      throws SQLException {
    return parseJson(cs.getString(columnIndex));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseJson(final String json) throws SQLException {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return jsonMapper.readValue(json, Map.class);
    } catch (final JacksonException e) {
      throw new SQLException("Error mapping JSONB to Map", e);
    }
  }
}
