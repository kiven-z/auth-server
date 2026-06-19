package com.auth.gateway.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.common.core.model.response.Result;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 构建网关响应体负载（i18n + ext），MessageSource 解析与 system/auth 侧两段式英文兜底一致
 *
 * @author Bunny
 */
@UtilityClass
public class GatewayResultFactory {

	/**
	 * 构建网关响应体负载
	 * @param messageSource 消息源
	 * @param rc 网关结果代码枚举
	 * @param args 参数
	 * @return 网关响应体负载
	 */
	public static Result<Object> build(MessageSource messageSource, GatewayResultCodeEnum rc, Object... args) {
		return build(messageSource, Locale.ENGLISH, rc, args);
	}

	/**
	 * 基于
	 * {@link GatewayBusinessException}（{@link com.auth.common.core.i18n.I18nMessageProvider}）构建响应体；无
	 * {@link ServerWebExchange} 时使用英语环境
	 * @param messageSource 消息源
	 * @param ex 网关业务异常
	 * @return 网关响应体负载
	 */
	public static Result<Object> build(MessageSource messageSource, GatewayBusinessException ex) {
		Objects.requireNonNull(ex, "ex");
		return build(messageSource, Locale.ENGLISH, ex.getResultCode(), ex.getMessageArgs());
	}

	/**
	 * 基于 {@link GatewayBusinessException} 构建响应体，区域取自请求头 {@code Accept-Language}
	 * @param exchange 交换机
	 * @param messageSource 消息源
	 * @param ex 网关业务异常
	 * @return 网关响应体负载
	 */
	public static Result<Object> build(ServerWebExchange exchange, MessageSource messageSource,
			GatewayBusinessException ex) {
		Objects.requireNonNull(ex, "ex");
		Locale locale = resolveLocale(exchange);
		return build(messageSource, locale, ex.getResultCode(), ex.getMessageArgs());
	}

	/**
	 * 构建网关响应体负载
	 * @param exchange 交换机
	 * @param messageSource 消息源
	 * @param rc 网关结果代码枚举
	 * @param args 参数
	 * @return 网关响应体负载
	 */
	public static Result<Object> build(ServerWebExchange exchange, MessageSource messageSource,
			GatewayResultCodeEnum rc, Object... args) {
		Locale locale = resolveLocale(exchange);
		return build(messageSource, locale, rc, args);
	}

	/**
	 * 构建网关响应体负载
	 * @param messageSource 消息源
	 * @param locale 区域
	 * @param rc 网关结果代码枚举
	 * @param args 参数
	 * @return 网关响应体负载
	 */
	public static Result<Object> build(MessageSource messageSource, Locale locale, GatewayResultCodeEnum rc,
			Object... args) {
		Objects.requireNonNull(rc, "rc");
		Objects.requireNonNull(messageSource, "messageSource");

		Locale finalLocale = ObjectUtil.defaultIfNull(locale, Locale.ENGLISH);
		Object[] safeArgs = ObjectUtil.defaultIfNull(args, new Object[0]);
		String i18nKey = rc.getI18nKey();
		String defaultEnglish = messageSource.getMessage(i18nKey, safeArgs, rc.getDefaultMessageEn(), Locale.ENGLISH);
		String message = messageSource.getMessage(i18nKey, safeArgs, defaultEnglish, finalLocale);

		Result<Object> body = Result.error(rc.fullBizCode(), rc.getError(), message);
		body.setExt(rc.ext(safeArgs));
		return body;
	}

	/**
	 * 将枚举上的 HTTP 状态解析为 {@link HttpStatus}；无法解析时回退为 500（网关历史行为）
	 * @param resultCode 结果码
	 * @return HTTP 状态
	 */
	public static HttpStatus resolveHttpStatus(GatewayResultCodeEnum resultCode) {
		Objects.requireNonNull(resultCode, "resultCode");
		HttpStatus resolved = HttpStatus.resolve(resultCode.getHttpStatus());
		if (resolved != null) {
			return resolved;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	/**
	 * 解析区域
	 * @param exchange 交换机
	 * @return 区域
	 */
	private static Locale resolveLocale(ServerWebExchange exchange) {
		if (exchange == null) {
			return Locale.ENGLISH;
		}
		List<Locale> locales = exchange.getRequest().getHeaders().getAcceptLanguageAsLocales();
		if (CollUtil.isNotEmpty(locales)) {
			return locales.get(0);
		}
		return Locale.ENGLISH;
	}

}
