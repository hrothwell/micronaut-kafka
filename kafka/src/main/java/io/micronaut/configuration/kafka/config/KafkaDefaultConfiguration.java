/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.kafka.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.DefaultConversionService;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The default Kafka configuration to apply to both the consumer and the producer, but can be overridden by either.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@ConfigurationProperties(AbstractKafkaConfiguration.PREFIX)
@Requires(AbstractKafkaConfiguration.PREFIX)
public class KafkaDefaultConfiguration extends AbstractKafkaConfiguration {

    /**
     * The default health timeout value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_HEALTHTIMEOUT = 10;

    private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();
    private Duration healthTimeout = Duration.ofSeconds(DEFAULT_HEALTHTIMEOUT);

    /**
     * Constructs the default Kafka configuration.
     *
     * @param environment The environment
     */
    public KafkaDefaultConfiguration(Environment environment) {
        super(resolveDefaultConfiguration(environment));
        getConfig().putIfAbsent(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                AbstractKafkaConfiguration.DEFAULT_BOOTSTRAP_SERVERS
        );
    }

    /**
     * The health check timeout.
     *
     * @return The duration
     */
    public Duration getHealthTimeout() {
        return healthTimeout;
    }

    /**
     * The health check timeout. Default value ({@value #DEFAULT_HEALTHTIMEOUT} seconds).
     *
     * @param healthTimeout The duration
     */
    public void setHealthTimeout(Duration healthTimeout) {
        if (healthTimeout != null) {
            this.healthTimeout = healthTimeout;
        }
    }

    private static Properties resolveDefaultConfiguration(Environment environment) {
        Map<String, Object> values = environment.getProperties(PREFIX);
        Properties properties = new Properties();
        values.entrySet().stream().filter(entry -> {
            String key = entry.getKey().toString();
            return !Stream.of("embedded", "consumers", "producers", "streams").anyMatch(key::startsWith);
        }).forEach(entry -> {
            Object value = entry.getValue();
            if (CONVERSION_SERVICE.canConvert(entry.getValue().getClass(), String.class)) {
                Optional<Object> converted = CONVERSION_SERVICE.convert(entry.getValue(), String.class);
                if (converted.isPresent()){
                    value = converted.get();
                }
            }
            properties.setProperty(entry.getKey(), value.toString());

        });
        return properties;
    }
}
