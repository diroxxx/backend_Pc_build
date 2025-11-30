package org.example.backend_pcbuild.configuration;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class rabbitMqConfig {
    @Bean
    public Queue offersQueue() {
        return new Queue("offers", true);
    }


    @Bean
    public Queue test1() {
        return new Queue("test1", true);
    }

    private static final List<String> SHOPS = List.of("olx", "allegro", "allegroLokalnie", "x-kom");

    @Bean
    public Declarables dynamicShopQueues() {
        List<Declarable> declarables = new ArrayList<>();

        for (String shop : SHOPS) {
            declarables.add(new Queue("scrapingOffers." + shop, true));
            declarables.add(new Queue("checkOffers." + shop, true));
            declarables.add(new Queue("offersAdded." + shop, true));
            declarables.add(new Queue("offersDeleted." + shop, true));
        }

        return new Declarables(declarables);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPort(5672);
        return factory;
    }

    @Bean
    public MessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }



}
