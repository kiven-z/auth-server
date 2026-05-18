package com.auth.service.system.schedule.handler;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

/**
 * 任务处理器注册中心
 *
 * @author Bunny
 */
@Component
public class SysJobTaskHandlerRegistry {

	private final Map<SysJobTaskType, SysJobTaskHandler> handlers;

	public SysJobTaskHandlerRegistry(List<SysJobTaskHandler> handlerList) {
		this.handlers = new EnumMap<>(SysJobTaskType.class);
		for (SysJobTaskHandler handler : handlerList) {
			this.handlers.put(handler.taskType(), handler);
		}
	}

	/**
	 * 解析任务处理器（兼容 taskType 缺失的历史数据）
	 * @param job 任务定义
	 * @return 处理器
	 */
	public SysJobTaskHandler resolve(SysJobEntity job) {
		SysJobTaskType taskType = SysJobTaskType.resolve(job.getTaskType(), job.getInvokeTarget());
		SysJobTaskHandler handler = handlers.get(taskType);
		if (handler == null) {
			throw new SystemBusinessException(JOB_TASK_TYPE_UNSUPPORTED, taskType.name());
		}
		return handler;
	}

}
