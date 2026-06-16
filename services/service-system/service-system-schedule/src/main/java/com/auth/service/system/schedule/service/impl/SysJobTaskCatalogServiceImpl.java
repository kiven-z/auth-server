package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.catalog.QuartzTaskRegistry;
import com.auth.service.system.schedule.catalog.SysJobPayloadTemplateRegistry;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.service.SysJobTaskCatalogService;
import com.auth.service.system.schedule.support.task.InvokeTargetSupport;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_LOAD_FAILED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_NOT_WHITELISTED;

/**
 * 任务目录服务实现
 *
 * @author Bunny
 */
@Service
public class SysJobTaskCatalogServiceImpl implements SysJobTaskCatalogService {

	private final QuartzTaskRegistry quartzTaskRegistry;

	private final SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry;

	public SysJobTaskCatalogServiceImpl(QuartzTaskRegistry quartzTaskRegistry,
			SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry) {
		this.quartzTaskRegistry = quartzTaskRegistry;
		this.sysJobPayloadTemplateRegistry = sysJobPayloadTemplateRegistry;
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
		String beanName = quartzTaskRegistry.resolveBeanName(className);
		if (CharSequenceUtil.isBlank(beanName)) {
			throw new SysJobException(JOB_CLASS_NOT_WHITELISTED);
		}

		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		}
		catch (ClassNotFoundException exception) {
			throw new SysJobException(JOB_CLASS_LOAD_FAILED, exception.getMessage());
		}

		List<QuartzTaskMethodVO> methodList = new ArrayList<>();
		for (Method method : targetClass.getDeclaredMethods()) {
			if (!InvokeTargetSupport.isCallableMethod(method)) {
				continue;
			}
			methodList.add(InvokeTargetSupport.toMethodCatalog(beanName, method));
		}
		return methodList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object resolvePayloadTemplate(String taskType) {
		SysJobTaskType resolve = SysJobTaskType.resolve(taskType);
		return sysJobPayloadTemplateRegistry.buildTemplate(resolve);
	}

}
