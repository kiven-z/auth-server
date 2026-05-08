package com.auth.service.system.message.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.model.EmailTemplateRequireFieldRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.experimental.UtilityClass;

import java.util.*;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * require_fields JSON 校验与转换
 *
 * @author Bunny
 */
@UtilityClass
public class EmailTemplateRequireFieldsJsonSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	/**
	 * 将变量列表序列化为 JSON 字符串（写入数据库）
	 */
	public static String toJson(List<EmailTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(REQUIRE_FIELDS_NULL);
		}
		validate(rows);
		try {
			return MAPPER.writeValueAsString(rows);
		}
		catch (JsonProcessingException e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_SERIALIZE_FAILED, e.getMessage());
		}
	}

	/**
	 * 解析数据库中的 JSON，校验结构与 key 唯一性
	 */
	public static List<EmailTemplateRequireFieldRow> parse(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}
		try {
			// 直接反序列化，FAILL_ON_UNKNOWN_PROPERTIES 会自动拒绝未知字段
			List<EmailTemplateRequireFieldRow> rows = MAPPER.readValue(json,
					MAPPER.getTypeFactory().constructCollectionType(List.class, EmailTemplateRequireFieldRow.class));
			validate(rows);
			return rows;
		}
		catch (UnrecognizedPropertyException e) {
			// 保留原有的非法属性名错误信息
			throw new SystemBusinessException(REQUIRE_FIELDS_ILLEGAL_PROPERTY, e.getPropertyName());
		}
		catch (Exception e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_PARSE_FAILED, e.getMessage());
		}
	}

	/**
	 * 业务校验：列表非空、key 非空且唯一、exampleValue 非空
	 */
	public static void validate(List<EmailTemplateRequireFieldRow> rows) {
		if (CollUtil.isEmpty(rows)) {
			return;
		}

		Set<String> keys = new HashSet<>();
		for (EmailTemplateRequireFieldRow row : rows) {
			if (row == null || CharSequenceUtil.isBlank(row.getKey())) {
				throw new SystemBusinessException(REQUIRE_FIELD_KEY_EMPTY);
			}
			if (row.getExampleValue() == null || row.getExampleValue().isNull()) {
				throw new SystemBusinessException(REQUIRE_FIELD_EXAMPLE_EMPTY, row.getKey());
			}
			if (!keys.add(row.getKey())) {
				throw new SystemBusinessException(REQUIRE_FIELD_KEY_DUPLICATE, row.getKey());
			}
		}
	}

	/**
	 * 将变量列表转为 FreeMarker 数据模型（使用每条记录的 exampleValue）
	 */
	public static Map<String, Object> toExampleModel(List<EmailTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(REQUIRE_FIELDS_NULL);
		}
		validate(rows);
		Map<String, Object> model = new LinkedHashMap<>();
		for (EmailTemplateRequireFieldRow row : rows) {
			model.put(row.getKey(), MAPPER.convertValue(row.getExampleValue(), Object.class));
		}
		return model;
	}

}