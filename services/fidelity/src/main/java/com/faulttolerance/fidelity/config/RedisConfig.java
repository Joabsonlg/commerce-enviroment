package com.faulttolerance.fidelity.config;

import com.faulttolerance.fidelity.model.BonusPoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, BonusPoints> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, BonusPoints> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use Jackson serializer for BonusPoints objects
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(BonusPoints.class));
        template.setKeySerializer(new StringRedisSerializer());
        
        return template;
    }
}
