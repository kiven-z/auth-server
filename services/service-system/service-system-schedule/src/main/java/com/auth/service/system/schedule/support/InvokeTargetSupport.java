package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_INVOKE_PARAM_NOT_SUPPORTED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_REFLECT_METHOD_NOT_FOUND;
import static com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys.METHOD_NAME;

/**
 * Bean 调用模式 {@code invoke_target} 的共享规则：可调用方法判定、签名与示例格式化。
 *
 * @author Bunny
 */
public final class InvokeTargetSupport {

	private InvokeTargetSupport() {
	}

	/**
	 * 是否可被 {@code invoke_target} 反射调用
	 * @param method 候选方法
	 * @return 是否可调用
	 */
	public static boolean isCallableMethod(@NonNull Method method) {
		int modifiers = method.getModifiers();
		if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || method.isSynthetic()
				|| method.isBridge()) {
			return false;
		}
		if (method.getDeclaringClass() == Object.class) {
			return false;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			return true;
		}
		return parameterTypes.length == 1 && parameterTypes[0] == String.class;
	}

	/**
	 * 返回前端展示用参数签名
	 * @param method 已判定为可调用的方法
	 * @return {@code ()} 或 {@code (String)}
	 */
	public static @NonNull String parameterSignature(@NonNull Method method) {
		return method.getParameterCount() == 0 ? "()" : "(String)";
	}

	/**
	 * 生成 {@code invoke_target} 示例；String 参数留空引号供用户填写
	 * @param beanName Spring Bean 名
	 * @param method 已判定为可调用的方法
	 * @return 示例字符串
	 */
	public static @NonNull String formatInvokeTarget(@NonNull String beanName, @NonNull Method method) {
		String methodName = method.getName();
		if (method.getParameterCount() == 0) {
			return beanName + "." + methodName + "()";
		}
		return beanName + "." + methodName + "('')";
	}

	/**
	 * 组装任务目录方法项
	 * @param beanName Spring Bean 名
	 * @param method 已判定为可调用的方法
	 * @return 方法目录 VO
	 */
	public static @NonNull QuartzTaskMethodVO toMethodCatalog(@NonNull String beanName, @NonNull Method method) {
		QuartzTaskMethodVO methodVO = new QuartzTaskMethodVO();
		methodVO.setMethodName(method.getName());
		methodVO.setParameterSignature(parameterSignature(method));
		methodVO.setReturnType(method.getReturnType().getSimpleName());
		methodVO.setInvokeTargetExample(formatInvokeTarget(beanName, method));
		methodVO.setRequiresStringParam(method.getParameterCount() == 1);
		return methodVO;
	}

	/**
	 * 按参数个数解析 Bean 上的 public 方法
	 * @param clazz Bean 类型
	 * @param methodName 方法名
	 * @param argCount 参数个数（0 或 1）
	 * @return 可访问的方法
	 */
	public static @NonNull Method resolveMethod(@NonNull Class<?> clazz, @NonNull String methodName, int argCount) {
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
			throw new SysJobException(JOB_INVOKE_PARAM_NOT_SUPPORTED);
		}

		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);
		if (method == null) {
			throw new SysJobException(JOB_REFLECT_METHOD_NOT_FOUND, signature, methodName);
		}
		ReflectionUtils.makeAccessible(method);
		return Objects.requireNonNull(method, METHOD_NAME);
	}

}
