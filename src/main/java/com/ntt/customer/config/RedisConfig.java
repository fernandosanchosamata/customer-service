package com.ntt.customer.config;

import com.ntt.customer.model.dto.CustomerCacheDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ReactiveRedisOperations<String, CustomerCacheDto> redisOperations(
      ReactiveRedisConnectionFactory factory) {
    Jackson2JsonRedisSerializer<CustomerCacheDto> serializer =
        new Jackson2JsonRedisSerializer<>(CustomerCacheDto.class);

    RedisSerializationContext.RedisSerializationContextBuilder<String, CustomerCacheDto> builder =
        RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

    RedisSerializationContext<String, CustomerCacheDto> context = builder.value(serializer).build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}
