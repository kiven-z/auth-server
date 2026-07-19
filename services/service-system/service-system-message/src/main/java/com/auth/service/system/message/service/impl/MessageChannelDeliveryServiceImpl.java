package com.auth.service.system.message.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.MessageChannelDeliveryConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageChannelDeliveryMapper;
import com.auth.service.system.message.model.entity.MessageChannelDeliveryEntity;
import com.auth.service.system.message.model.po.MessageChannelDeliveryPageRowPO;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageResponse;
import com.auth.service.system.message.service.MessageChannelDeliveryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 渠道投递记录查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class MessageChannelDeliveryServiceImpl implements MessageChannelDeliveryService {

	private final MessageChannelDeliveryMapper messageChannelDeliveryMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<MessageChannelDeliveryPageResponse> getChannelDeliveryPage(MessageChannelDeliveryQuery query) {
		Page<MessageChannelDeliveryEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<MessageChannelDeliveryPageRowPO> page = messageChannelDeliveryMapper.selectChannelDeliveryPage(pageParams,
				query);
		IPage<MessageChannelDeliveryPageResponse> voPage = page
			.convert(MessageChannelDeliveryConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public MessageChannelDeliveryDetailVO getChannelDeliveryById(Long id) {
		MessageChannelDeliveryEntity entity = messageChannelDeliveryMapper.selectById(id);
		if (entity == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}
		MessageChannelDeliveryDetailVO vo = MessageChannelDeliveryConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

}
