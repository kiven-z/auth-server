package com.auth.service.system.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.mapper.SysUserQueryMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.convert.SysJobLogConverter;
import com.auth.service.system.log.mapper.LogJobMapper;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.query.LogJobQuery;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import com.auth.service.system.log.service.LogJobService;
import com.auth.service.system.message.service.MessageDispatchService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.auth.service.system.admin.model.enums.TemplateType.CLEAR_DATABASE_CONFIRM;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 定时任务日志服务实现
 *
 * @author Bunny
 */
@Service
public class LogJobServiceImpl extends ServiceImpl<LogJobMapper, JobLogEntity> implements LogJobService {

	private static final String CLEAR_LOG_CODE_KEY = "sys:job:log:clear:code:";

	private final AuditUserDisplayService auditUserDisplayService;

	private final RedisTemplate<String, Object> redisTemplate;

	private final MessageDispatchService messageDispatchService;

	private final SysUserQueryMapper sysUserMapper;

	public LogJobServiceImpl(AuditUserDisplayService auditUserDisplayService,
			RedisTemplate<String, Object> redisTemplate, MessageDispatchService messageDispatchService,
			SysUserQueryMapper sysUserMapper) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.redisTemplate = redisTemplate;
		this.messageDispatchService = messageDispatchService;
		this.sysUserMapper = sysUserMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<LogJobPageResponse> getPage(Page<JobLogEntity> pageParams, LogJobQuery query) {
		IPage<SysJobLogPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<LogJobPageResponse> voPage = page.convert(SysJobLogConverter.INSTANCE::toPageResponse);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogJobDetailVO getDetail(Long id) {
		// 查询并验证日志是否存在
		JobLogEntity sysJobLog = getById(id);
		if (sysJobLog == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 复制数据到VO
		return BeanUtil.copyProperties(sysJobLog, LogJobDetailVO.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendClearLogVerificationCode() {
		// 获取当前用户ID
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(ILLEGAL_USER_ID_NOT_NULL);
		}

		// 查询并验证用户是否存在
		UserEntity user = sysUserMapper.selectById(userId);
		if (user == null || CharSequenceUtil.isBlank(user.getEmail())) {
			throw new SystemBusinessException(USER_EMAIL_NOT_BIND);
		}

		// 生成验证码
		String code = RandomUtil.randomNumbers(6);
		Duration expire = Duration.ofMinutes(10);
		redisTemplate.opsForValue().set(CLEAR_LOG_CODE_KEY + userId, code, expire);

		// 设置邮件变量
		Map<String, Object> variables = new HashMap<>(3);
		variables.put("username", user.getUsername());
		variables.put("verificationCode", code);
		variables.put("expire", expire.toMinutes());

		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode(CLEAR_DATABASE_CONFIRM.getTemplateCode())
			.targets(Collections.singletonList(user.getEmail()))
			.variables(variables)
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();
		messageDispatchService.sendByTemplate(command);
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
