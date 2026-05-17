package com.auth.service.system.common.exception.code;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.auth.common.web.context.SpringContextHolder;

/**
 * 系统服务结果码契约（模块 {@code 10}）
 *
 * <p>
 * 业务代码格式: {@code 10CATSEQ}，{@code getBizCode()} = {@code 100_000 + getCode()}。 各域枚举见
 * {@code code.*} 子包；号段登记见 {@code package-info.java}。
 * </p>
 *
 * @author Bunny
 */
public interface SystemResultCode {

	/**
	 * HTTP 状态码
	 * @return HTTP 状态
	 */
	int getHttpStatus();

	/**
	 * 模块内序号（3 位 catSeq）
	 * @return catSeq
	 */
	int getCode();

	/**
	 * 稳定错误标识（写入 {@code Result.error}）
	 * @return error 字符串
	 */
	String getError();

	/**
	 * i18n 消息键
	 * @return messageKey
	 */
	String getMessageKey();

	/**
	 * 对外业务码
	 * @return {@code 100_000 + getCode()}
	 */
	default int getBizCode() {
		return 100_000 + getCode();
	}

	/**
	 * 按区域渲染消息
	 * @param locale 区域
	 * @param args 占位参数
	 * @return 翻译文案
	 */
	default String getMessage(Locale locale, Object... args) {
		return SpringContextHolder.getMessage(getMessageKey(), args, locale);
	}

	/**
	 * 按当前请求区域渲染消息
	 * @param args 占位参数
	 * @return 翻译文案
	 */
	default String getMessage(Object... args) {
		return getMessage(LocaleContextHolder.getLocale(), args);
	}

	/**
	 * 渲染当前区域消息（无占位参数）
	 * @return 翻译文案
	 */
	@SuppressWarnings("java:S3878")
	default String getMessage() {
		return getMessage(new Object[0]);
	}

}
