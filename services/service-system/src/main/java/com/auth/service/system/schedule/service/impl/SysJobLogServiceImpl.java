package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.email.model.EmailSendDTO;
import com.auth.module.security.autoconfig.web.SecurityUserUtils;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.mapper.UserMapper;
import com.auth.service.system.email.service.EmailSendService;
import com.auth.service.system.schedule.mapper.SysJobLogMapper;
import com.auth.service.system.schedule.model.entity.SysJobLogEntity;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.auth.service.system.schedule.service.SysJobLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.auth.module.email.model.TemplateType.CLEAR_DATABASE_CONFIRM;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 任务执行日志实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLogEntity> implements SysJobLogService {

	private static final String CLEAR_LOG_CODE_KEY = "sys:job:log:clear:code:";

	private final RedisTemplate<String, Object> redisTemplate;

	private final EmailSendService emailSendService;

	private final UserMapper userMapper;

	public SysJobLogServiceImpl(RedisTemplate<String, Object> redisTemplate, EmailSendService emailSendService,
			UserMapper userMapper) {
		this.redisTemplate = redisTemplate;
		this.emailSendService = emailSendService;
		this.userMapper = userMapper;
	}

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	@Override
	public PageResponse<SysJobLogPageResponse> getPage(Page<SysJobLogEntity> pageParams, SysJobLogQuery query) {
		IPage<SysJobLogPageResponse> page = baseMapper.selectListByPage(pageParams, query);

		return PageResponse.of(page);
	}

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	@Override
	public SysJobLogDetailVO getDetail(Long id) {
		// 查询并验证日志是否存在
		SysJobLogEntity sysJobLog = getById(id);
		if (sysJobLog == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 复制数据到VO
		SysJobLogDetailVO vo = new SysJobLogDetailVO();
		BeanUtil.copyProperties(sysJobLog, vo);
		return vo;
	}

	/**
	 * 记录成功日志
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param message 消息
	 */
	@Override
	public void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			String message) {
		SysJobLogEntity sysJobLog = new SysJobLogEntity();
		sysJobLog.setJobName(jobName);
		sysJobLog.setJobGroup(jobGroup);
		sysJobLog.setInvokeTarget(invokeTarget);
		sysJobLog.setJobMessage(message);
		sysJobLog.setStatus(false);
		sysJobLog.setElapsedTime(elapsedMs);
		sysJobLog.setCreateTime(LocalDateTime.now());
		save(sysJobLog);
	}

	/**
	 * 记录失败日志
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param ex 异常
	 */
	@Override
	public void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			JobExecutionException ex) {
		SysJobLogEntity jobLog = new SysJobLogEntity();
		jobLog.setJobName(jobName);
		jobLog.setJobGroup(jobGroup);
		jobLog.setInvokeTarget(invokeTarget);
		jobLog.setJobMessage("FAILED");
		jobLog.setStatus(true);
		jobLog.setElapsedTime(elapsedMs);
		jobLog.setCreateTime(LocalDateTime.now());

		// 如果异常不为空，则设置异常信息
		if (ex != null) {
			jobLog.setExceptionInfo(ExceptionUtil.stacktraceToString(ex, 8000));
		}

		save(jobLog);
	}

	/**
	 * 批量删除
	 * @param ids 主键
	 */
	@Override
	public void removeBatch(List<Long> ids) {
		removeBatchByIds(ids);
	}

	/**
	 * 发送清空日志验证码
	 */
	@Override
	public void sendClearLogVerificationCode() {
		// 获取当前用户ID
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(USER_NOT_EXIST);
		}

		// 查询并验证用户是否存在
		UserEntity user = userMapper.selectById(userId);
		if (user == null || CharSequenceUtil.isBlank(user.getEmail())) {
			throw new SystemBusinessException(USER_EMAIL_NOT_BIND);
		}

		// 生成验证码
		String code = RandomUtil.randomNumbers(6);
		Duration expire = Duration.ofMinutes(10);
		redisTemplate.opsForValue().set(CLEAR_LOG_CODE_KEY + userId, code, expire);

		// 发送邮件
		EmailSendDTO dto = new EmailSendDTO();
		dto.setTemplateCode(CLEAR_DATABASE_CONFIRM.getTemplateCode());
		dto.setTo(Collections.singletonList(user.getEmail()));

		// 设置邮件变量
		Map<String, Object> variables = new HashMap<>(2);
		variables.put("username", user.getUsername());
		variables.put("verificationCode", code);
		variables.put("expire", expire.toMinutes());
		dto.setVariables(variables);

		emailSendService.sendByTemplate(dto);
	}

	/**
	 * 清空全部日志
	 * @param verificationCode 验证码
	 */
	@Override
	public void clearAll(String verificationCode) {
		// 验证验证码是否为空
		if (CharSequenceUtil.isBlank(verificationCode)) {
			throw new SystemBusinessException(VERIFY_CODE_ERROR);
		}

		// 获取当前用户ID
		Long userId = SecurityUserUtils.getUserId();
		String cachedCode = (String) redisTemplate.opsForValue().get(CLEAR_LOG_CODE_KEY + userId);

		// 验证验证码是否正确
		if (cachedCode == null || !cachedCode.equals(verificationCode)) {
			throw new SystemBusinessException(VERIFY_CODE_ERROR);
		}

		// 删除验证码
		redisTemplate.delete(CLEAR_LOG_CODE_KEY + userId);
		baseMapper.deleteAll();
	}

}
