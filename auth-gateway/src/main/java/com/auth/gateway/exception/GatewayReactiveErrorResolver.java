package com.auth.gateway.exception;

import lombok.experimental.UtilityClass;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * 将 Reactive链路上的解析为网关错误枚举及可选 i18n 参数 优先识别错误信息上 cause 链上的包装），其余按框架/上游异常映射
 *
 * @author Bunny
 */
@UtilityClass
public class GatewayReactiveErrorResolver {

	private static final int MAX_CAUSE_DEPTH = 20;

	/**
	 * 解析错误：若为GatewayBusinessException则使用其枚举与参数；否则按基础设施异常映射
	 * @param ex 当前错误
	 * @return 解析结果（不可为 null）
	 */
	public static GatewayReactiveErrorResolution resolve(Throwable ex) {
		GatewayBusinessException businessException = findGatewayBusinessException(ex);
		if (businessException != null) {
			Object[] args = businessException.getMessageArgs();
			List<Object> argList = args.length == 0 ? List.of() : Arrays.stream(args).toList();
			return new GatewayReactiveErrorResolution(businessException.getResultCode(), argList);
		}
		return new GatewayReactiveErrorResolution(resolveFramework(ex), List.of());
	}

	/**
	 * 在自身与 cause 链上查找 {@link GatewayBusinessException}
	 * @param ex 当前错误
	 * @return 第一个匹配的网关业务异常，若无则 null
	 */
	static GatewayBusinessException findGatewayBusinessException(Throwable ex) {
		Throwable current = ex;
		int depth = 0;
		while (current != null && depth++ < MAX_CAUSE_DEPTH) {
			if (current instanceof GatewayBusinessException gbe) {
				return gbe;
			}
			current = current.getCause();
		}
		return null;
	}

	private static GatewayResultCodeEnum resolveFramework(Throwable ex) {
		if (ex instanceof NotFoundException) {
			return GatewayResultCodeEnum.ROUTE_NOT_FOUND;
		}
		if (ex instanceof ResponseStatusException rse) {
			HttpStatus status = HttpStatus.resolve(rse.getStatusCode().value());
			if (status == HttpStatus.NOT_FOUND) {
				return GatewayResultCodeEnum.ROUTE_NOT_FOUND;
			}
		}
		if (containsCause(ex, TimeoutException.class)) {
			return GatewayResultCodeEnum.UPSTREAM_TIMEOUT;
		}
		if (containsCause(ex, ConnectException.class)) {
			return GatewayResultCodeEnum.UPSTREAM_CONNECT_FAILED;
		}
		return GatewayResultCodeEnum.INTERNAL_ERROR;
	}

	private static boolean containsCause(Throwable ex, Class<? extends Throwable> type) {
		Throwable current = ex;
		int depth = 0;
		while (current != null && depth++ < MAX_CAUSE_DEPTH) {
			if (type.isInstance(current)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	/**
	 * Reactive 错误解析结果：枚举 + i18n 参数（不可变列表，便于 record 值语义）
	 *
	 * @param resultCode 网关结果码
	 * @param i18nArgs 国际化参数
	 */
	public record GatewayReactiveErrorResolution(GatewayResultCodeEnum resultCode, List<Object> i18nArgs) {

		public GatewayReactiveErrorResolution {
			Objects.requireNonNull(resultCode, "resultCode");
			i18nArgs = i18nArgs != null ? List.copyOf(i18nArgs) : List.of();
		}
	}

}
