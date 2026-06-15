package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.catalog.SysJobPayloadTemplateRegistry;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.service.SysJobPayloadTemplateService;
import org.springframework.stereotype.Service;

/**
 * 任务载荷模板服务实现
 *
 * @author Bunny
 */
@Service
public class SysJobPayloadTemplateServiceImpl implements SysJobPayloadTemplateService {

	private final SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry;

	public SysJobPayloadTemplateServiceImpl(SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry) {
		this.sysJobPayloadTemplateRegistry = sysJobPayloadTemplateRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object resolvePayloadTemplate(String taskType) {
		return sysJobPayloadTemplateRegistry.buildTemplate(SysJobTaskType.resolve(taskType));
	}

}
