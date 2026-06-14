package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.catalog.MessageDispatchPayloadTemplateProvider;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.service.SysJobTaskCatalogService;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;

/**
 * 任务目录服务实现
 *
 * @author Bunny
 */
@Service
public class SysJobTaskCatalogServiceImpl implements SysJobTaskCatalogService {

	private final QuartzTaskRegistry quartzTaskRegistry;

	private final ApplicationContext applicationContext;

	private final Optional<MessageDispatchPayloadTemplateProvider> messageDispatchPayloadTemplateProvider;

	public SysJobTaskCatalogServiceImpl(QuartzTaskRegistry quartzTaskRegistry, ApplicationContext applicationContext,
			Optional<MessageDispatchPayloadTemplateProvider> messageDispatchPayloadTemplateProvider) {
		this.quartzTaskRegistry = quartzTaskRegistry;
		this.applicationContext = applicationContext;
		this.messageDispatchPayloadTemplateProvider = messageDispatchPayloadTemplateProvider;
	}

	@NotNull
	private static QuartzTaskMethodVO getQuartzTaskMethodVO(Method method, String beanName) {
		String methodName = method.getName();
		boolean noArg = method.getParameterCount() == 0;
		QuartzTaskMethodVO methodVO = new QuartzTaskMethodVO();
		methodVO.setMethodName(methodName);
		methodVO.setParameterSignature(noArg ? "()" : "(String)");
		methodVO.setReturnType(method.getReturnType().getSimpleName());
		methodVO.setInvokeTargetExample(
				noArg ? beanName + "." + methodName + "()" : beanName + "." + methodName + "('demo')");
		return methodVO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QuartzTaskClassVO> listQuartzClasses() {
		return quartzTaskRegistry.listAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QuartzTaskMethodVO> listCallableMethods(String className) {
		if (CharSequenceUtil.isBlank(className)) {
			throw new SysJobException(PARAM_REQUIRED, "job_class");
		}
		if (!quartzTaskRegistry.isAllowed(className)) {
			throw new SysJobException(JOB_CLASS_NOT_WHITELISTED);
		}
		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		}
		catch (ClassNotFoundException exception) {
			throw new SysJobException(JOB_CLASS_LOAD_FAILED, exception.getMessage());
		}

		// 解析 bean 名称
		String[] beanNames = applicationContext.getBeanNamesForType(targetClass);
		String beanName = beanNames.length > 0 ? beanNames[0] : Introspector.decapitalize(targetClass.getSimpleName());
		List<QuartzTaskMethodVO> methodList = new ArrayList<>();
		for (Method method : targetClass.getMethods()) {
			if (!isCallableMethod(method)) {
				continue;
			}
			QuartzTaskMethodVO methodVO = getQuartzTaskMethodVO(method, beanName);
			methodList.add(methodVO);
		}
		return methodList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object messageDispatchPayloadTemplate() {
		return messageDispatchPayloadTemplateProvider.map(MessageDispatchPayloadTemplateProvider::buildTemplatePayload)
			.orElseThrow(() -> new SysJobException(JOB_MESSAGE_DISPATCH_UNAVAILABLE));
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

}
