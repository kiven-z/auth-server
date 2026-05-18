package com.auth.service.system.schedule.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.mode.bean.BeanInvokeEntryJob;
import org.quartz.Job;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET;

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
	public void validate(SysJobEntity job) {
		if (CharSequenceUtil.isBlank(job.getInvokeTarget())) {
			throw new SystemBusinessException(JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET);
		}
	}

	@Override
	public Class<? extends Job> resolveEntryJobClass(boolean concurrent) {
		return concurrent ? BeanInvokeEntryJob.class : BeanInvokeEntryJob.Disallow.class;
	}

}
