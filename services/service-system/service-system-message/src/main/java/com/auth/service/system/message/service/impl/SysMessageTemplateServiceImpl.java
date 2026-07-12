package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.MessageTemplateConverter;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.auth.service.system.message.service.SysMessageTemplateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

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

	public SysMessageTemplateServiceImpl(MessageTemplateMapper messageTemplateMapper,
			AuditUserDisplayService auditUserDisplayService) {
		this.messageTemplateMapper = messageTemplateMapper;
		this.baseMapper = messageTemplateMapper;
		this.auditUserDisplayService = auditUserDisplayService;
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
		MessageTemplateEntity entity = messageTemplateMapper.selectById(id);
		if (entity == null || !channel.equals(entity.getChannel())) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

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

}
