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

package com.iqkv.foundation.auditservice.infrastructure.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configures RabbitMQ message serialization/deserialization using Jackson 3.x.
 *
 * <p>Registers a {@link JacksonJsonMessageConverter} backed by the application's shared
 * {@link JsonMapper} bean. Spring Boot auto-configures {@code RabbitTemplate} and
 * {@code RabbitListenerContainerFactory} to use this converter when it is present in the
 * application context, replacing the default {@code SimpleMessageConverter}.
 *
 * <p>This enables automatic JSON deserialization of incoming domain events into
 * {@code Map<String, Object>} payloads consumed by
 * {@link com.iqkv.foundation.auditservice.infrastructure.messaging.AuditEventListener}.
 */
@Configuration
@ConditionalOnProperty(name = "iqkv.messaging.rabbitmq.enabled", havingValue = "true")
class RabbitMQMessageConverterConfig {

  /**
   * Jackson 3.x message converter for RabbitMQ.
   *
   * <p>Reuses the shared {@link JsonMapper} configured in {@link JacksonJsonMapperConfig}
   * to ensure consistent JSON handling across REST APIs and messaging.
   *
   * @param jsonMapper the application's Jackson 3 JsonMapper
   * @return a message converter that serializes/deserializes messages as JSON
   */
  @Bean
  MessageConverter rabbitMessageConverter(final JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }
}
