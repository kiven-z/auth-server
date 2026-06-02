package com.auth.common.redis.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 序列化/反序列化配置类 注意：如果要禁用可以使用：common.redis.config.enabled，没有设置默认为启用
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnBooleanProperty(prefix = "auth.common.redis.jackson", name = "enabled", matchIfMissing = true)
@Configuration
public class JacksonConfig {

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	/**
	 * 日期格式：yyyy-MM-dd
	 */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
	 * 配置 ObjectMapper 实例
	 * @return 配置好的 ObjectMapper 实例
	 */
	@Bean("redisObjectMapper")
	public ObjectMapper redisObjectMapper() {
		log.info("Registering redisObjectMapper");
		ObjectMapper objectMapper = new ObjectMapper();

		// 添加 JavaTimeModule 模块
		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
		timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
		timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
		timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));

		// 注册JavaTimeModule模块以支持Java 8时间类型
		objectMapper.registerModule(timeModule);

		// 禁用时间戳序列化，使用标准格式输出日期
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 配置反序列化时忽略未知属性，避免解析失败
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// 设置时区为系统默认时区
		objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));

		return objectMapper;
	}

}