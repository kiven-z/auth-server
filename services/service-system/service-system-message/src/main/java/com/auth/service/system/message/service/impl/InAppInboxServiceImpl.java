package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.message.convert.InAppInboxConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppInboxMapper;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.mapper.InAppMessageUserStatusMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageUserStatusEntity;
import com.auth.service.system.message.model.enums.DeliveryMode;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageResponse;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;
import com.auth.service.system.message.service.InAppInboxService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.JWT_INVALID;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_INBOX_DELIVERY_MODE_INVALID;

/**
 * 用户侧站内信收件箱
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppInboxServiceImpl implements InAppInboxService {

	private final InAppInboxMapper inAppInboxMapper;

	private final InAppMessageMapper inAppMessageMapper;

	private final InAppMessageRecipientMapper inAppMessageRecipientMapper;

	private final InAppMessageUserStatusMapper inAppMessageUserStatusMapper;

	/**
	 * 解析当前登录用户 ID；未登录时拒绝
	 * @return 用户 ID
	 */
	private static Long requireCurrentUserId() {
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(JWT_INVALID);
		}
		return userId;
	}

	/**
	 * 组装公开消息已读状态行（懒写入）
	 * @param userId 当前用户
	 * @param messageId 公开消息 ID
	 * @param readTime 已读时间
	 * @return 待 upsert 实体
	 */
	private static InAppMessageUserStatusEntity toPullReadRow(Long userId, Long messageId, LocalDateTime readTime) {
		InAppMessageUserStatusEntity row = new InAppMessageUserStatusEntity();
		row.setId(IdWorker.getId());
		row.setMessageId(messageId);
		row.setUserId(userId);
		row.setIsRead(Boolean.TRUE);
		row.setReadTime(readTime);
		row.setIsDeleted(Boolean.FALSE);
		row.setCreateUser(userId);
		row.setUpdateUser(userId);
		row.setVersion(0L);
		return row;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<InAppInboxPageResponse> getInboxPage(InAppInboxQuery query) {
		Long userId = requireCurrentUserId();

		DeliveryMode deliveryMode = DeliveryMode.from(query.getDeliveryMode());
		if (deliveryMode == null) {
			throw new MessageException(IN_APP_INBOX_DELIVERY_MODE_INVALID, query.getDeliveryMode());
		}

		Page<InAppInboxPageRowPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<InAppInboxPageRowPO> page = switch (deliveryMode) {
			case PUSH -> inAppInboxMapper.selectPushInboxPage(pageParams, userId, query);
			case PULL -> inAppInboxMapper.selectPullInboxPage(pageParams, userId, query);
			default -> throw new MessageException(IN_APP_INBOX_DELIVERY_MODE_INVALID, query.getDeliveryMode());
		};

		IPage<InAppInboxPageResponse> voPage = page.convert(InAppInboxConverter.INSTANCE::toPageResponse);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public InAppInboxUnreadCountVO getUnreadCount() {
		Long userId = requireCurrentUserId();

		long pullUnreadCount = inAppInboxMapper.countPullUnread(userId);
		long pushUnreadCount = inAppInboxMapper.countPushUnread(userId);
		return InAppInboxUnreadCountVO.builder()
			.pullUnreadCount(pullUnreadCount)
			.pushUnreadCount(pushUnreadCount)
			.totalUnreadCount(pullUnreadCount + pushUnreadCount)
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markRead(List<Long> messageIds) {
		Long userId = requireCurrentUserId();
		if (CollUtil.isEmpty(messageIds)) {
			return;
		}

		List<Long> ids = messageIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<InAppMessageEntity> messages = inAppMessageMapper.selectList(Wrappers.<InAppMessageEntity>lambdaQuery()
			.select(InAppMessageEntity::getId, InAppMessageEntity::getDeliveryMode, InAppMessageEntity::getStatus)
			.in(InAppMessageEntity::getId, ids));
		if (CollUtil.isEmpty(messages)) {
			return;
		}

		List<Long> pushIds = messages.stream()
			.filter(entity -> DeliveryMode.from(entity.getDeliveryMode()) == DeliveryMode.PUSH)
			.filter(entity -> {
				InAppMessageStatus status = InAppMessageStatus.from(entity.getStatus());
				return status == InAppMessageStatus.SUCCESS || status == InAppMessageStatus.PARTIAL;
			})
			.map(InAppMessageEntity::getId)
			.toList();

		List<Long> pullIds = messages.stream()
			.filter(entity -> DeliveryMode.from(entity.getDeliveryMode()) == DeliveryMode.PULL)
			.filter(entity -> InAppMessageStatus.from(entity.getStatus()) == InAppMessageStatus.SUCCESS)
			.map(InAppMessageEntity::getId)
			.toList();

		LocalDateTime readTime = LocalDateTime.now();
		// PUSH：收件行已存在，只改已读状态
		if (CollUtil.isNotEmpty(pushIds)) {
			inAppMessageRecipientMapper.markRead(userId, pushIds, readTime);
		}
		// PULL：公开读扩散，用户状态懒写入
		if (CollUtil.isNotEmpty(pullIds)) {
			List<InAppMessageUserStatusEntity> rows = pullIds.stream()
				.map(messageId -> toPullReadRow(userId, messageId, readTime))
				.toList();
			inAppMessageUserStatusMapper.upsertReadBatch(rows);
		}
	}

}
