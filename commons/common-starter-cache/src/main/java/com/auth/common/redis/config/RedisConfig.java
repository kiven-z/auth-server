package com.auth.common.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 注意：如果要禁用可以使用：common.redis.config.enabled，没有设置默认为启用
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnBooleanProperty(prefix = "auth.common.redis.config", name = "enabled", matchIfMissing = true)
@Configuration
public class RedisConfig {

	private final ObjectMapper objectMapper;

	public RedisConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 创建RedisTemplate，使用String序列化key，JSON序列化value
	 */
	@Bean("redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(keySerializer());
		redisTemplate.setHashKeySerializer(keySerializer());
		redisTemplate.setValueSerializer(jsonRedisSerializer());
		redisTemplate.setHashValueSerializer(jsonRedisSerializer());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	private RedisSerializer<String> keySerializer() {
		return new StringRedisSerializer();
	}

	public RedisSerializer<Object> jsonRedisSerializer() {
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

}