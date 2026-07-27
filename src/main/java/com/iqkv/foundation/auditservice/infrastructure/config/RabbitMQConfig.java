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

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Audit Service.
 * Declares the audit queue and binds it to the shared platform events exchange.
 */
@Configuration
@ConditionalOnProperty(name = "iqkv.messaging.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {

  public static final String EVENTS_EXCHANGE = "iqkv.events";
  public static final String AUDIT_QUEUE = "iqkv.audit.service.events";
  public static final String TENANT_PROVISIONING_QUEUE = "iqkv.audit.tenant.provisioning";
  // Aligned with the platform-wide DLX declared by iam-service (was "iqkv.events.dlx")
  public static final String DLX_EXCHANGE = "iqkv.dlx";
  public static final String AUDIT_DLQ = "iqkv.audit.service.events.dlq";

  public static final String ROUTING_TENANT_CREATED = "tenant.created";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EVENTS_EXCHANGE);
  }

  @Bean
  public Queue tenantProvisioningQueue() {
    return new Queue(TENANT_PROVISIONING_QUEUE, true, false, false, Map.of(
        "x-dead-letter-exchange", DLX_EXCHANGE,
        "x-dead-letter-routing-key", AUDIT_DLQ
    ));
  }

  @Bean
  public Binding tenantProvisioningBinding(final Queue tenantProvisioningQueue, final TopicExchange eventsExchange) {
    return BindingBuilder.bind(tenantProvisioningQueue).to(eventsExchange).with(ROUTING_TENANT_CREATED);
  }

  @Bean
  public Queue auditQueue() {
    return new Queue(AUDIT_QUEUE, true, false, false, Map.of(
        "x-dead-letter-exchange", DLX_EXCHANGE,
        "x-dead-letter-routing-key", AUDIT_DLQ
    ));
  }

  /**
   * Bind to all events in the platform for passive auditing.
   */
  @Bean
  public Binding auditBinding(final Queue auditQueue, final TopicExchange eventsExchange) {
    return BindingBuilder.bind(auditQueue).to(eventsExchange).with("#");
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_EXCHANGE);
  }

  @Bean
  public Queue auditDeadLetterQueue() {
    return new Queue(AUDIT_DLQ);
  }

  @Bean
  public Binding auditDlxBinding(final Queue auditDeadLetterQueue, final TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(auditDeadLetterQueue).to(deadLetterExchange).with(AUDIT_DLQ);
  }
}
