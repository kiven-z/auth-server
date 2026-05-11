package com.auth.service.system.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.email.model.EmailSendDTO;
import com.auth.module.security.autoconfig.web.SecurityUserUtils;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.mapper.UserMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.log.mapper.LogJobMapper;
import com.auth.service.system.log.service.LogJobService;
import com.auth.service.system.message.service.EmailSendService;
import com.auth.service.system.schedule.convert.SysJobLogConvert;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.po.SysJobLogPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.auth.module.email.model.TemplateType.CLEAR_DATABASE_CONFIRM;
import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

/**
 * 定时任务日志服务实现
 *
 * @author Bunny
 */
@Service
public class LogJobServiceImpl extends ServiceImpl<LogJobMapper, JobLogEntity> implements LogJobService {

	private static final String CLEAR_LOG_CODE_KEY = "sys:job:log:clear:code:";

	private final RedisTemplate<String, Object> redisTemplate;

	private final EmailSendService emailSendService;

	private final UserMapper userMapper;

	public LogJobServiceImpl(RedisTemplate<String, Object> redisTemplate, EmailSendService emailSendService,
			UserMapper userMapper) {
		this.redisTemplate = redisTemplate;
		this.emailSendService = emailSendService;
		this.userMapper = userMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobLogPageResponse> getPage(Page<JobLogEntity> pageParams, SysJobLogQuery query) {
		IPage<SysJobLogPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<SysJobLogPageResponse> voPage = page.convert(SysJobLogConvert.INSTANCE::toPageResponse);

		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobLogDetailVO getDetail(Long id) {
		// 查询并验证日志是否存在
		JobLogEntity sysJobLog = getById(id);
		if (sysJobLog == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 复制数据到VO
		SysJobLogDetailVO vo = new SysJobLogDetailVO();
		BeanUtil.copyProperties(sysJobLog, vo);
		return vo;
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
