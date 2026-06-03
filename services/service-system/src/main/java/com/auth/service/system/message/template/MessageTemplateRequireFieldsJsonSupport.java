package com.auth.service.system.message.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.auth.service.system.message.model.vo.EmailTemplateRequireFieldRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.experimental.UtilityClass;

import java.util.*;

import static com.auth.service.system.common.exception.code.SystemEmailResultCode.*;

/**
 * 消息模板必填变量声明：序列化、解析与发送前完整性校验（多渠道共用）
 *
 * @author Bunny
 */
@UtilityClass
public class MessageTemplateRequireFieldsJsonSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	/**
	 * 将变量列表序列化为 JSON 字符串（写入数据库）
	 * @param rows 变量列表
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
		catch (JsonProcessingException e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_SERIALIZE_FAILED, e.getMessage());
		}
	}

	/**
	 * 解析必填变量声明 JSON，校验结构与 key 唯一性
	 */
	public static List<EmailTemplateRequireFieldRow> parse(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}
		try {
			List<EmailTemplateRequireFieldRow> rows = MAPPER.readValue(json,
					MAPPER.getTypeFactory().constructCollectionType(List.class, EmailTemplateRequireFieldRow.class));
			validate(rows);
			return rows;
		}
		catch (UnrecognizedPropertyException e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_ILLEGAL_PROPERTY, e.getPropertyName());
		}
		catch (Exception e) {
			throw new SystemBusinessException(REQUIRE_FIELDS_PARSE_FAILED, e.getMessage());
		}
	}

	/**
	 * 业务校验：列表非空、key 非空且唯一、exampleValue 非空
	 * @param rows 变量列表
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
	 * 找出 variables 中缺失的必填 key（仅校验 key 是否存在，不校验值是否为空）
	 * @param requireFields 必填变量声明
	 * @param variables 实际变量
	 * @return 缺失的 key 列表
	 */
	public static List<String> findMissingKeys(List<EmailTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables) {
		if (CollUtil.isEmpty(requireFields)) {
			return List.of();
		}
		Map<String, Object> safeVariables = variables != null ? variables : Map.of();
		return requireFields.stream()
			.map(EmailTemplateRequireFieldRow::getKey)
			.filter(key -> !safeVariables.containsKey(key))
			.toList();
	}

	/**
	 * 校验发送变量是否齐全
	 * @param requireFields 已解析的必填变量声明
	 * @param variables 发送变量
	 * @param missingCode 变量缺失时的业务码（邮件/短信/钉钉各自传入）
	 * @param channel 渠道名（写入 i18n 占位 {0}）
	 */
	public static void assertVariablesPresent(List<EmailTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables, SystemResultCode missingCode, String channel) {
		List<String> missingKeys = findMissingKeys(requireFields, variables);
		if (CollUtil.isNotEmpty(missingKeys)) {
			throw new SystemBusinessException(missingCode, channel, String.join(", ", missingKeys));
		}
	}

	/**
	 * 解析声明并校验发送变量是否齐全
	 * @param missingCode 变量缺失时的业务码（各渠道传入）
	 * @param channel 渠道名（i18n 占位 {0}）
	 */
	public static void assertVariablesPresent(String requireFieldsJson, Map<String, Object> variables,
			SystemResultCode missingCode, String channel) {
		assertVariablesPresent(parse(requireFieldsJson), variables, missingCode, channel);
	}

	/**
	 * 用各字段的 exampleValue 组装预览/示例数据模型
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
