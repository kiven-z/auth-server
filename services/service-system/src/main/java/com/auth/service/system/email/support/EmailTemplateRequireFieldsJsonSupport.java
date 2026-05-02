package com.auth.service.system.email.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.email.model.EmailTemplateRequireFieldRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.*;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.FREEMARKER_MODEL_BUILD_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_ELEMENT_NOT_OBJECT;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_ILLEGAL_PROPERTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_NOT_ARRAY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_NULL;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_PARSE_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELDS_SERIALIZE_FAILED;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELD_EXAMPLE_EMPTY;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELD_KEY_DUPLICATE;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.REQUIRE_FIELD_KEY_EMPTY;

/**
 * require_fields JSON 校验与转换
 *
 * @author Bunny
 */
@UtilityClass
public class EmailTemplateRequireFieldsJsonSupport {

	/**
	 * 允许出现在单条 JSON 对象中的字段名（禁止额外字段）
	 */
	private static final Set<String> ALLOWED_PROPERTY_KEYS = Set.of("key", "description", "exampleValue");

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * 将变量列表序列化为 JSON 字符串（写入数据库）
	 * @param rows 变量声明
	 * @return JSON 字符串
	 */
	public static String toJson(List<EmailTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(REQUIRE_FIELDS_NULL);
		}
		validate(rows);
		try {
			return MAPPER.writeValueAsString(rows);
		}
		catch (Exception e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_SERIALIZE_FAILED, e.getMessage());
		}
	}

	/**
	 * 解析数据库中的 JSON，校验结构与 key 唯一性
	 * @param json JSON 字符串
	 * @return 变量列表；空串或 null 返回空列表
	 */
	public static List<EmailTemplateRequireFieldRow> parse(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return new ArrayList<>(0);
		}
		try {
			JsonNode root = MAPPER.readTree(json);
			if (!root.isArray()) {
				throw new SystemBusinessException(REQUIRE_FIELDS_NOT_ARRAY);
			}
			List<EmailTemplateRequireFieldRow> list = new ArrayList<>();
			for (JsonNode node : root) {
				if (!node.isObject()) {
					throw new SystemBusinessException(REQUIRE_FIELDS_ELEMENT_NOT_OBJECT);
				}
				Iterator<String> names = node.fieldNames();
				while (names.hasNext()) {
					String name = names.next();
					if (!ALLOWED_PROPERTY_KEYS.contains(name)) {
						throw new SystemBusinessException(REQUIRE_FIELDS_ILLEGAL_PROPERTY, name);
					}
				}
				EmailTemplateRequireFieldRow row = MAPPER.treeToValue(node, EmailTemplateRequireFieldRow.class);
				list.add(row);
			}
			validate(list);
			return list;
		}
		catch (SystemBusinessException e) {
			throw e;
		}
		catch (Exception e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_PARSE_FAILED, e.getMessage());
		}
	}

	/**
	 * 业务校验：非空列表、key 非空、key 唯一、exampleValue 必填（由 Bean Validation 保障）
	 * @param rows 变量列表
	 */
	public static void validate(List<EmailTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(REQUIRE_FIELDS_NULL);
		}
		if (rows.isEmpty()) {
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
	 * @param rows 变量列表
	 * @return 数据模型
	 */
	public static Map<String, Object> toExampleModel(List<EmailTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(REQUIRE_FIELDS_NULL);
		}
		validate(rows);
		try {
			Map<String, Object> model = new LinkedHashMap<>();
			for (EmailTemplateRequireFieldRow row : rows) {
				model.put(row.getKey(), MAPPER.convertValue(row.getExampleValue(), Object.class));
			}
			return model;
		}
		catch (Exception e) {
			throw new SystemBusinessException(FREEMARKER_MODEL_BUILD_FAILED, e.getMessage());
		}
	}

}
