package com.auth.common.web.valid.sensitive;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.auth.common.web.model.enums.RegexConstantsEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 敏感信息序列化器
 *
 * @author Bunny
 */
public class SensitiveSerialize extends JsonSerializer<String> implements ContextualSerializer {

	private static final String IPV4_REGEX = RegexConstantsEnum.IPV4.getPattern();

	private static final String IPV6_REGEX = RegexConstantsEnum.IPV6.getPattern();

	private Sensitive sensitive;

	@Override
	public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
		// 为空检查
		if (CharSequenceUtil.isBlank(value)) {
			jsonGenerator.writeString(value);
			return;
		}

		switch (sensitive.type()) {
			// 中文名
			case CHINESE_NAME:
				String chineseName = DesensitizedUtil.chineseName(value);
				jsonGenerator.writeString(chineseName);
				break;
			// 身份证
			case ID_CARD:
				String idCardNum = DesensitizedUtil.idCardNum(value, sensitive.prefixLen(), sensitive.suffixLen());
				jsonGenerator.writeString(idCardNum);
				break;
			// 电话
			case PHONE_NUMBER:
				String mobilePhone = DesensitizedUtil.mobilePhone(value);
				jsonGenerator.writeString(mobilePhone);
				break;
			// 邮箱
			case EMAIL:
				String email = DesensitizedUtil.email(value);
				jsonGenerator.writeString(email);
				break;
			// IP地址
			case IP_ADDRESS:
				// IPv4
				if (value.matches(IPV4_REGEX)) {
					String ipv4 = DesensitizedUtil.ipv4(value);
					jsonGenerator.writeString(ipv4);
					break;
				}
				// IPV6
				else if (value.matches(IPV6_REGEX)) {
					String ipv6 = DesensitizedUtil.ipv6(value);
					jsonGenerator.writeString(ipv6);
					break;
				}
				jsonGenerator.writeString("******");
				break;
			// 银行卡
			case BANK_CARD:
				String bankCard = DesensitizedUtil.bankCard(value);
				jsonGenerator.writeString(bankCard);
				break;
			// 自定义
			case CUSTOM:
				String desensitizeCustom = SensitiveSerializeUtil.desensitizeCustom(value, sensitive.prefixLen(),
						sensitive.suffixLen(), sensitive.symbol());
				jsonGenerator.writeString(desensitizeCustom);
				break;
			default:
				jsonGenerator.writeString(value);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
			throws JsonMappingException {
		Sensitive annotation = property.getAnnotation(Sensitive.class);
		if (annotation != null) {
			SensitiveSerialize serializer = new SensitiveSerialize();
			serializer.sensitive = annotation;
			return serializer;
		}
		return prov.findValueSerializer(property.getType(), property);
	}

}
