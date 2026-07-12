package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.MessageTemplateConverter;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.MessageTemplateRequireFieldsForm;
import com.auth.service.system.message.model.form.MessageTemplateTestSendForm;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.auth.service.system.message.model.vo.MessageTemplateRequireFieldRow;
import com.auth.service.system.message.service.MessageDispatchService;
import com.auth.service.system.message.service.SysMessageTemplateService;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_INVALID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.message.exception.MessageResultCode.MESSAGE_CHANNEL_UNSUPPORTED;

/**
 * 消息模板公共服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(readOnly = true)
public class SysMessageTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysMessageTemplateService {

	private final MessageTemplateMapper messageTemplateMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	private final MessageDispatchService messageDispatchService;

	public SysMessageTemplateServiceImpl(MessageTemplateMapper messageTemplateMapper,
			AuditUserDisplayService auditUserDisplayService, MessageDispatchService messageDispatchService) {
		this.messageTemplateMapper = messageTemplateMapper;
		this.baseMapper = messageTemplateMapper;
		this.auditUserDisplayService = auditUserDisplayService;
		this.messageDispatchService = messageDispatchService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<MessageTemplatePageResponse> getMessageTemplatePage(Page<MessageTemplateEntity> pageParams,
			MessageTemplateQuery query) {
		IPage<MessageTemplatePageRowPO> page = messageTemplateMapper.selectMessageTemplatePage(pageParams, query);
		IPage<MessageTemplatePageResponse> voPage = page.convert(MessageTemplateConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageTemplateDetailVO getMessageTemplateById(Long id, String channel) {
		MessageTemplateEntity entity = requireTemplate(id, channel);
		MessageTemplateDetailVO vo = MessageTemplateConverter.INSTANCE.toDetailVo(entity);

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids, String channel) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		super.remove(Wrappers.<MessageTemplateEntity>lambdaQuery()
			.in(MessageTemplateEntity::getId, ids)
			.eq(MessageTemplateEntity::getChannel, channel));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(List<Long> ids, Boolean status, String channel) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		super.update(Wrappers.<MessageTemplateEntity>lambdaUpdate()
			.in(MessageTemplateEntity::getId, ids)
			.eq(MessageTemplateEntity::getChannel, channel)
			.set(MessageTemplateEntity::getStatus, status));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateRequireFields(Long id, String channel, MessageTemplateRequireFieldsForm form) {
		MessageTemplateEntity entity = requireTemplate(id, channel);
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(form.getRequireFields()));
		super.updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void testSend(MessageTemplateTestSendForm form) {
		// 解析渠道枚举；非法名称视为不支持
		String formChannel = form.getChannel();
		MessageChannel channel;
		try {
			channel = MessageChannel.valueOf(formChannel);
		}
		catch (IllegalArgumentException ex) {
			throw new SystemBusinessException(MESSAGE_CHANNEL_UNSUPPORTED, formChannel);
		}

		// 按渠道校验接收目标格式
		String target = form.getTarget();
		boolean valid = switch (channel) {
			case EMAIL -> Validator.isEmail(target);
			case SMS -> Validator.isMobile(target);
			case DING_TALK, IN_APP -> true;
		};
		if (!valid) {
			throw new SystemBusinessException(DATA_INVALID, "test-send target");
		}

		MessageTemplateEntity entity = requireTemplate(form.getId(), channel.name());
		List<MessageTemplateRequireFieldRow> rows = MessageTemplateRequireFieldsJsonSupport
			.parse(entity.getRequireFields());
		Map<String, Object> variables = MessageTemplateRequireFieldsJsonSupport.toExampleModel(rows);

		// 组装渠道默认扩展参数
		EmailChannelOptions options = channel == MessageChannel.EMAIL
				? EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build() : null;
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(channel)
			.templateCode(entity.getSceneCode())
			.targets(List.of(target))
			.variables(variables)
			.options(options)
			.build();
		messageDispatchService.sendByTemplate(command);
	}

	/**
	 * 按主键加载模板，并校验渠道一致
	 * @param id 模板主键
	 * @param channel 期望渠道
	 * @return 模板实体
	 */
	private MessageTemplateEntity requireTemplate(Long id, String channel) {
		MessageTemplateEntity entity = messageTemplateMapper.selectById(id);
		if (entity == null || !channel.equals(entity.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return entity;
	}

}
