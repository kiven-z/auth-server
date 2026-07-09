package com.auth.module.file.api.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件业务类型枚举
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum FileBizType {

	/**
	 * 用户头像
	 */
	AVATAR("avatar", VisibilityRule.FORCE_PUBLIC),

	/**
	 * 通用附件
	 */
	ATTACHMENT("attachment", VisibilityRule.DEFAULT_PRIVATE),;

	/**
	 * 已注册业务类型索引
	 */
	private static final Map<String, FileBizType> CODE_INDEX = Arrays.stream(FileBizType.values())
		.collect(Collectors.toUnmodifiableMap(FileBizType::getCode, Function.identity()));

	/**
	 * 业务类型编码
	 */
	private final String code;

	/**
	 * 可见性规则
	 */
	private final VisibilityRule visibilityRule;

	/**
	 * 严格解析业务类型编码
	 * @param code 业务类型编码
	 * @return 业务类型枚举
	 * @throws IllegalArgumentException 编码为空或编码不存在时抛出
	 */
	public static FileBizType require(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("File biz type code must not be blank");
		}
		return Optional.ofNullable(CODE_INDEX.get(code.trim().toLowerCase(Locale.ROOT)))
			.orElseThrow(() -> new IllegalArgumentException("Unsupported file biz type code: " + code));
	}

}
