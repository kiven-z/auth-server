package com.auth.service.system.message.support.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.auth.service.system.message.model.vo.MessageTemplateRequireFieldRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.experimental.UtilityClass;

import java.util.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

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
	public static String toJson(List<MessageTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(DATA_INVALID, "require_fields");
		}

		validate(rows);

		try {
			return MAPPER.writeValueAsString(rows);
		}
		catch (JsonProcessingException e) {
			throw new SystemBusinessException(DATA_INVALID, e.getMessage());
		}
	}

	/**
	 * 解析必填变量声明 JSON，校验结构与 key 唯一性
	 * @param json 必填变量声明 JSON
	 * @return 必填变量声明列表
	 */
	public static List<MessageTemplateRequireFieldRow> parse(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}

		try {
			List<MessageTemplateRequireFieldRow> rows = MAPPER.readValue(json,
					MAPPER.getTypeFactory().constructCollectionType(List.class, MessageTemplateRequireFieldRow.class));
			validate(rows);
			return rows;
		}
		catch (UnrecognizedPropertyException e) {
			throw new SystemBusinessException(DATA_INVALID, e.getPropertyName());
		}
		catch (Exception e) {
			throw new SystemBusinessException(DATA_INVALID, e.getMessage());
		}
	}

	/**
	 * 业务校验：列表非空、key 非空且唯一、exampleValue 非空
	 * @param rows 变量列表
	 * @throws SystemBusinessException 如果变量列表为空、key 为空、exampleValue 为空、key 重复
	 */
	public static void validate(List<MessageTemplateRequireFieldRow> rows) {
		if (CollUtil.isEmpty(rows)) {
			return;
		}

		// 校验变量列表是否为空、key 是否为空、exampleValue 是否为空、key 是否重复
		Set<String> keys = new HashSet<>();
		for (MessageTemplateRequireFieldRow row : rows) {
			if (row == null || CharSequenceUtil.isBlank(row.getKey())) {
				throw new SystemBusinessException(PARAM_REQUIRED, "Variable key");
			}
			if (row.getExampleValue() == null || row.getExampleValue().isNull()) {
				throw new SystemBusinessException(PARAM_REQUIRED, row.getKey() + " exampleValue");
			}
			if (!keys.add(row.getKey())) {
				throw new SystemBusinessException(PARAM_DUPLICATE, "Variable key", row.getKey());
			}
		}
	}

	/**
	 * 找出 variables 中缺失的必填 key（仅校验 key 是否存在，不校验值是否为空）
	 * @param requireFields 必填变量声明
	 * @param variables 实际变量
	 * @return 缺失的 key 列表
	 */
	public static List<String> findMissingKeys(List<MessageTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables) {
		if (CollUtil.isEmpty(requireFields)) {
			return List.of();
		}

		// 如果 variables 为空，则返回空列表
		Map<String, Object> safeVariables = ObjectUtil.defaultIfNull(variables, Map.of());
		return requireFields.stream()
			.map(MessageTemplateRequireFieldRow::getKey)
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
	public static void assertVariablesPresent(List<MessageTemplateRequireFieldRow> requireFields,
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
	 * @param rows 变量列表
	 * @return 预览/示例数据模型
	 */
	public static Map<String, Object> toExampleModel(List<MessageTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new SystemBusinessException(DATA_INVALID, "require_fields");
		}
		validate(rows);
		Map<String, Object> model = new LinkedHashMap<>();
		for (MessageTemplateRequireFieldRow row : rows) {
			model.put(row.getKey(), MAPPER.convertValue(row.getExampleValue(), Object.class));
		}
		return model;
	}

}
