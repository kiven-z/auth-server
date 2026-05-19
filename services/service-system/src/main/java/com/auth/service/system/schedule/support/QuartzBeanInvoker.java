package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

import static com.auth.service.system.schedule.exception.SystemScheduleResultCode.*;
import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.BEAN_NAME;
import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.METHOD_NAME;

/**
 * 解析 {@code invoke_target}（首个 {@code .} 前为 Spring Bean 名，后为方法及可选参数）并反射调用
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class QuartzBeanInvoker {

	private final ApplicationContext applicationContext;

	private static @NonNull InvocationSpec parseSpec(@Nullable String invokeTarget) {
		if (CharSequenceUtil.isBlank(invokeTarget)) {
			throw new SystemBusinessException(JOB_INVOKE_TARGET_EMPTY);
		}

		String nonNullInvokeTarget = Objects.requireNonNull(invokeTarget, "invokeTarget");
		int dot = nonNullInvokeTarget.indexOf('.');
		if (dot <= 0 || dot >= invokeTarget.length() - 1) {
			throw new SystemBusinessException(JOB_INVOKE_TARGET_FORMAT);
		}

		String beanName = nonNullInvokeTarget.substring(0, dot).trim();
		String tail = nonNullInvokeTarget.substring(dot + 1).trim();
		if (tail.isEmpty()) {
			throw new SystemBusinessException(JOB_INVOKE_TARGET_FORMAT);
		}

		String nonNullBeanName = Objects.requireNonNull(beanName, BEAN_NAME);
		int lp = tail.indexOf('(');
		if (lp < 0) {
			String nonNullMethodName = Objects.requireNonNull(tail, METHOD_NAME);
			return new InvocationSpec(nonNullBeanName, nonNullMethodName, null);
		}

		String suffix = ")";
		if (!tail.endsWith(suffix)) {
			throw new SystemBusinessException(JOB_INVOKE_TARGET_PAREN_MISMATCH);
		}

		String methodName = tail.substring(0, lp).trim();
		String argsInner = tail.substring(lp + 1, tail.length() - 1).trim();
		String nonNullMethodName = Objects.requireNonNull(methodName, METHOD_NAME);
		return new InvocationSpec(nonNullBeanName, nonNullMethodName, argsInner);
	}

	private static Object[] parseArgs(@Nullable String inner) {
		// 1. 前置检查与去空格
		if (CharSequenceUtil.isBlank(inner)) {
			return new Object[0];
		}
		String t = CharSequenceUtil.trim(inner);

		// 2. 尝试移除引号（支持双引号或单引号）
		String unwrapped = CharSequenceUtil.unWrap(t, '"');
		// 如果没变，说明不是双引号包裹，尝试单引号
		if (unwrapped.equals(t)) {
			unwrapped = CharSequenceUtil.unWrap(t, '\'');
		}

		// 3. 判断是否成功移除并返回
		if (unwrapped.length() < t.length()) {
			return new Object[] { unwrapped };
		}
		throw new SystemBusinessException(JOB_INVOKE_PARAM_NOT_SUPPORTED);
	}

	private static @NonNull Method resolveMethod(@NonNull Class<?> clazz, @NonNull String methodName, int argCount) {
		Class<?>[] paramTypes;
		String signature;
		if (argCount == 0) {
			paramTypes = new Class<?>[0];
			signature = "()";
		}
		else if (argCount == 1) {
			paramTypes = new Class<?>[] { String.class };
			signature = "(String)";
		}
		else {
			throw new SystemBusinessException(JOB_INVOKE_PARAM_NOT_SUPPORTED);
		}

		Class<?> nonNullClazz = Objects.requireNonNull(clazz, "clazz");
		String nonNullMethodName = Objects.requireNonNull(methodName, METHOD_NAME);
		Method m = ReflectionUtils.findMethod(nonNullClazz, nonNullMethodName, paramTypes);
		if (m == null) {
			throw new SystemBusinessException(JOB_REFLECT_METHOD_NOT_FOUND, signature, nonNullMethodName);
		}
		ReflectionUtils.makeAccessible(m);
		return m;
	}

	/**
	 * 执行调用目标字符串
	 * @param invokeTarget 如 {@code demoQuartzTask.run()} 或
	 * {@code demoQuartzTask.run('x')}
	 * @return 方法返回值转字符串（无返回则为空串）
	 */
	public String invoke(@Nullable String invokeTarget) {
		InvocationSpec spec = parseSpec(invokeTarget);

		String beanName = Objects.requireNonNull(spec.beanName, BEAN_NAME);
		String methodName = Objects.requireNonNull(spec.methodName, METHOD_NAME);

		Object bean = applicationContext.getBean(beanName);
		Object[] args = parseArgs(spec.argsInner);
		Method method = resolveMethod(bean.getClass(), methodName, args.length);
		Object result = ReflectionUtils.invokeMethod(method, bean, args);
		return result == null ? "" : String.valueOf(result);
	}

	private record InvocationSpec(@NonNull String beanName, @NonNull String methodName, @Nullable String argsInner) {
		private InvocationSpec(@NonNull String beanName, @NonNull String methodName, @Nullable String argsInner) {
			this.beanName = Objects.requireNonNull(beanName, BEAN_NAME);
			this.methodName = Objects.requireNonNull(methodName, METHOD_NAME);
			this.argsInner = argsInner;
		}
	}

}
