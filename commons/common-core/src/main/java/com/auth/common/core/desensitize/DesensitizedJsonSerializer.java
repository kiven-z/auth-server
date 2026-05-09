package com.auth.common.core.desensitize;

import java.io.IOException;

import com.auth.common.core.annotation.Desensitized;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;

/**
 * 将 {@link String} 按 {@link Desensitized} 策略在 JSON 输出时脱敏。
 *
 * @author Bunny
 */
public class DesensitizedJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

	private static final int ID_CARD_PREFIX_KEEP = 6;

	private static final int ID_CARD_SUFFIX_KEEP = 4;

	/** Mask for values that are not valid IPv4 or IPv6 when {@link DesensitizedType#IP_ADDRESS} is used. */
	private static final String UNKNOWN_IP_MASK = "******";

	private Desensitized config;

	/**
	 * 序列化字符串
	 *
	 * @param value 字符串
	 * @param gen  JSON生成器
	 * @param serializers 序列化提供者
	 * @throws IOException 如果发生IO异常
	 */
	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		if (CharSequenceUtil.isBlank(value)) {
			gen.writeString(value);
			return;
		}
		gen.writeString(mask(value));
	}

	/**
	 * 掩码处理
	 *
	 * @param value 字符串
	 * @return 掩码处理后的字符串
	 */
	private String mask(String value) {
		return switch (config.value()) {
			case CHINESE_NAME -> DesensitizedUtil.chineseName(value);
			case ID_CARD -> DesensitizedUtil.idCardNum(value, ID_CARD_PREFIX_KEEP, ID_CARD_SUFFIX_KEEP);
			case MOBILE_PHONE -> DesensitizedUtil.mobilePhone(value);
			case EMAIL -> DesensitizedUtil.email(value);
			case BANK_CARD -> DesensitizedUtil.bankCard(value);
			case IP_ADDRESS -> maskIpAddress(value);
			case CUSTOM -> DesensitizedStrings.custom(value, config.prefixLen(), config.suffixLen(), config.symbol());
		};
	}

	/**
	 * Detect IPv4 / IPv6 via Hutool {@link Validator}; unknown formats return a fixed mask.
	 */
	private static String maskIpAddress(String value) {
		String trimmed = CharSequenceUtil.trim(value);
		if (Validator.isIpv4(trimmed)) {
			return DesensitizedUtil.ipv4(trimmed);
		}
		if (Validator.isIpv6(trimmed)) {
			return DesensitizedUtil.ipv6(trimmed);
		}
		return UNKNOWN_IP_MASK;
	}

	/**
	 * 创建上下文序列化器
	 *
	 * @param prov 序列化提供者
	 * @param property 属性
	 * @return 上下文序列化器
	 * @throws JsonMappingException 如果发生JSON映射异常
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
			throws JsonMappingException {
		if (property == null) {
			return prov.findNullValueSerializer(null);
		}
		Desensitized ann = property.getAnnotation(Desensitized.class);
		if (ann == null) {
			return prov.findValueSerializer(property.getType(), property);
		}
		DesensitizedJsonSerializer ser = new DesensitizedJsonSerializer();
		ser.config = ann;
		return ser;
	}
}
