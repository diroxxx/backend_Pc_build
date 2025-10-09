package org.example.backend_pcbuild.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class rabbitMqConfig {
    @Bean
    public Queue offersQueue() {
        return new Queue("offers", true);
    }
}
