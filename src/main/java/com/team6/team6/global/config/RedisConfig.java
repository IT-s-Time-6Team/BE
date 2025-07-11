package com.team6.team6.global.config;

import com.team6.team6.keyword.dto.AnalysisResults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 이벤트 리스너를 등록하기 위한 RedisMessageListenerContainer 빈을 생성합니다.
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    // AnalysisResults 객체를 Redis에 저장하기 위한 RedisTemplate을 생성합니다.
    @Bean
    public RedisTemplate<String, AnalysisResults> analysisResultsRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, AnalysisResults> template = new RedisTemplate<>();
        Jackson2JsonRedisSerializer<AnalysisResults> serializer =
                new Jackson2JsonRedisSerializer<>(AnalysisResults.class);
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        return template;
    }
}
