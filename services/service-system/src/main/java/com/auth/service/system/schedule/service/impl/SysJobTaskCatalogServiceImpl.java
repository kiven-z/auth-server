package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.payload.MessageDispatchPayload;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.service.SysJobTaskCatalogService;
import com.auth.service.system.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemScheduleResultCode.*;

/**
 * 任务目录服务实现
 *
 * @author Bunny
 */
@Service
public class SysJobTaskCatalogServiceImpl implements SysJobTaskCatalogService {

	private final QuartzTaskRegistry quartzTaskRegistry;

	private final ApplicationContext applicationContext;

	private final MessageDispatchCommandParser messageDispatchCommandParser;

	public SysJobTaskCatalogServiceImpl(QuartzTaskRegistry quartzTaskRegistry, ApplicationContext applicationContext,
			MessageDispatchCommandParser messageDispatchCommandParser) {
		this.quartzTaskRegistry = quartzTaskRegistry;
		this.applicationContext = applicationContext;
		this.messageDispatchCommandParser = messageDispatchCommandParser;
	}

	@Override
	public List<QuartzTaskClassVO> listQuartzClasses() {
		return quartzTaskRegistry.listAll();
	}

	@Override
	public List<QuartzTaskMethodVO> listCallableMethods(String className) {
		if (CharSequenceUtil.isBlank(className)) {
			throw new SystemBusinessException(JOB_CLASS_EMPTY);
		}
		if (!quartzTaskRegistry.isAllowed(className)) {
			throw new SystemBusinessException(JOB_CLASS_NOT_WHITELISTED);
		}
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		}
		catch (ClassNotFoundException exception) {
			throw new SystemBusinessException(JOB_CLASS_LOAD_FAILED, exception.getMessage());
		}

		String beanName = resolveBeanName(targetClass);
		List<QuartzTaskMethodVO> methodList = new ArrayList<>();
		for (Method method : targetClass.getMethods()) {
			if (!isCallableMethod(method)) {
				continue;
			}
			QuartzTaskMethodVO methodVO = new QuartzTaskMethodVO();
			methodVO.setMethodName(method.getName());
			methodVO.setParameterSignature(buildParameterSignature(method));
			methodVO.setReturnType(method.getReturnType().getSimpleName());
			methodVO.setInvokeTargetExample(buildInvokeTarget(beanName, method));
			methodList.add(methodVO);
		}
		return methodList;
	}

	@Override
	public MessageDispatchPayload messageDispatchPayloadTemplate() {
		return messageDispatchCommandParser.buildTemplatePayload();
	}

	/**
	 * 是否可被 invoke_target 调用
	 * @param method 方法
	 * @return 是否可调用
	 */
	private boolean isCallableMethod(Method method) {
		if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic() || method.isBridge()) {
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
	 * 构建参数签名文本
	 * @param method 方法
	 * @return 签名
	 */
	private String buildParameterSignature(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length == 0) {
			return "()";
		}
		String joined = Arrays.stream(parameterTypes)
			.map(Class::getSimpleName)
			.reduce((a, b) -> a + ", " + b)
			.orElse("");
		return "(" + joined + ")";
	}

	/**
	 * 构建 invoke_target 示例
	 * @param beanName bean 名称
	 * @param method 方法
	 * @return invoke_target 示例
	 */
	private String buildInvokeTarget(String beanName, Method method) {
		if (method.getParameterCount() == 0) {
			return beanName + "." + method.getName() + "()";
		}
		return beanName + "." + method.getName() + "('demo')";
	}

	/**
	 * 解析 bean 名称
	 * @param type 任务类
	 * @return bean 名称
	 */
	private String resolveBeanName(Class<?> type) {
		String[] beanNames = applicationContext.getBeanNamesForType(type);
		if (beanNames.length > 0) {
			return beanNames[0];
		}
		return Introspector.decapitalize(type.getSimpleName());
	}

}
