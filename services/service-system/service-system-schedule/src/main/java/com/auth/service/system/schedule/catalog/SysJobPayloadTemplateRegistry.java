package com.auth.service.system.schedule.catalog;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PAYLOAD_TEMPLATE_UNAVAILABLE;

/**
 * 任务载荷模板注册中心
 *
 * @author Bunny
 */
@Component
public class SysJobPayloadTemplateRegistry {

	private final Map<SysJobTaskType, SysJobPayloadTemplateProvider> providers;

	public SysJobPayloadTemplateRegistry(List<SysJobPayloadTemplateProvider> providerList) {
		this.providers = new EnumMap<>(SysJobTaskType.class);
		for (SysJobPayloadTemplateProvider provider : providerList) {
			this.providers.put(provider.taskType(), provider);
		}
	}

	/**
	 * 按任务类型构建 payload 模板
	 * @param taskType 任务类型
	 * @return 载荷模板
	 */
	public Object buildTemplate(SysJobTaskType taskType) {
		SysJobPayloadTemplateProvider provider = providers.get(taskType);
		if (provider == null) {
			throw new SysJobException(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE, taskType.name());
		}
		return provider.buildTemplatePayload();
	}

}
