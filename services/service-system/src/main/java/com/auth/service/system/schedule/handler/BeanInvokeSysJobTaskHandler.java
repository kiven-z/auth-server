package com.auth.service.system.schedule.handler;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.mode.bean.BeanInvokeEntryJob;
import org.quartz.Job;
import org.springframework.stereotype.Component;

/**
 * Bean 调用模式处理器
 *
 * @author Bunny
 */
@Component
public class BeanInvokeSysJobTaskHandler implements SysJobTaskHandler {

	@Override
	public String handlerCode() {
		return "beanInvokeHandler";
	}

	@Override
	public SysJobTaskType taskType() {
		return SysJobTaskType.BEAN_INVOKE;
	}

	@Override
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? BeanInvokeEntryJob.class : BeanInvokeEntryJob.Disallow.class;
	}

}
