package com.auth.module.file.api.enums;

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
	 * 构造文件业务类型枚举
	 * @param code 业务类型编码
	 * @param visibilityRule 可见性规则
	 */
	FileBizType(String code, VisibilityRule visibilityRule) {
		this.code = code;
		this.visibilityRule = visibilityRule;
	}

	/**
	 * 宽松解析业务类型编码
	 * @param code 业务类型编码
	 * @return 解析结果；空值或未知值均返回 {@link Optional#empty()}
	 */
	public static Optional<FileBizType> parse(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}

		String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
		return Optional.ofNullable(CODE_INDEX.get(normalizedCode));
	}

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
		return parse(code).orElseThrow(() -> new IllegalArgumentException("Unsupported file biz type code: " + code));
	}

	/**
	 * 严格解析业务类型可见性
	 * @param code 业务类型编码
	 * @param requestedIsPrivate 客户端请求的可见性
	 * @return 可见性解析结果
	 * @throws IllegalArgumentException 编码为空或编码不存在时抛出
	 */
	public static ResolvedVisibility resolveVisibility(String code, Boolean requestedIsPrivate) {
		FileBizType type = require(code);
		return new ResolvedVisibility(type.getCode(), type.getVisibilityRule().resolve(requestedIsPrivate));
	}

	/**
	 * 文件可见性解析结果。
	 *
	 * @param bizTypeCode 归一化业务类型编码
	 * @param isPrivate 最终是否私有
	 */
	public record ResolvedVisibility(String bizTypeCode, boolean isPrivate) {
	}

}
