package com.auth.service.system.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.InAppEntityAssembler;
import com.auth.service.system.message.convert.InAppSendTaskConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.*;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.query.InAppSendTaskQuery;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.model.vo.inapp.InAppComposeResultVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageResponse;
import com.auth.service.system.message.service.InAppSendTaskService;
import com.auth.service.system.message.support.inapp.InAppComposeDispatchTrigger;
import com.auth.service.system.message.support.inapp.InAppComposeFormValidation;
import com.auth.service.system.message.support.recipient.RecipientScopeJsonSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_NOT_FOUND;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED;

/**
 * 管理端站内信按范围发送
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppSendTaskServiceImpl extends ServiceImpl<InAppMessageMapper, InAppMessageEntity>
		implements InAppSendTaskService {

	private final InAppComposeDispatchTrigger dispatchTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * 是否读扩散任务
	 * @param task 任务实体
	 * @return 是否读扩散任务
	 */
	private static boolean isPullDelivery(InAppMessageEntity task) {
		DeliveryMode mode = DeliveryMode.from(task.getDeliveryMode());
		if (mode != null) {
			return mode.isPull();
		}
		RecipientScopeType scopeType = RecipientScopeType.from(task.getRecipientScopeType());
		return scopeType != null && scopeType.deliveryMode().isPull();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<InAppSendTaskPageResponse> getSendTaskPage(InAppSendTaskQuery query) {
		Page<InAppMessageEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<InAppSendTaskPageRowPO> page = baseMapper.selectSendTaskPage(pageParams, query);
		IPage<InAppSendTaskPageResponse> voPage = page.convert(InAppSendTaskConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public InAppSendTaskDetailVO getSendTaskById(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppSendTaskDetailVO vo = InAppSendTaskConverter.INSTANCE.toDetailVo(task);
		String scopeJson = task.getRecipientScopeJson();
		vo.setRecipientScopeIds(RecipientScopeJsonSupport.parseIds(scopeJson));
		vo.setIncludeChildren(RecipientScopeJsonSupport.parseIncludeChildren(scopeJson));
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public InAppComposeResultVO send(InAppComposeForm form) {
		RecipientScopeType type = InAppComposeFormValidation.validate(form);
		RecipientScope scope = RecipientScope.builder()
			.type(type)
			.ids(form.getRecipientScopeIds())
			.includeChildren(form.getIncludeChildren())
			.build();
		InAppContentType contentType = InAppContentType.from(form.getContentType());

		Long senderUserId = SecurityUserUtils.getUserId();
		InAppMessageEntity task = InAppEntityAssembler.toAdminComposeTask(form, scope, contentType, senderUserId);
		super.save(task);

		// 公开直接做读扩散，不写用户收件表
		if (type.deliveryMode().isPull()) {
			return InAppComposeResultVO.builder()
				.taskId(task.getId())
				.totalCount(0)
				.successCount(0)
				.status(InAppMessageStatus.SUCCESS.name())
				.build();
		}

		// 非公开写入用户收件表
		dispatchTrigger.dispatchAfterCommit(task.getId());
		return InAppComposeResultVO.builder()
			.taskId(task.getId())
			.totalCount(0)
			.successCount(0)
			.status(InAppMessageStatus.PENDING.name())
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void retry(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
		boolean retryAllowed = MessageSendSourceType.ADMIN_COMPOSE.name().equals(task.getSourceType())
				&& !isPullDelivery(task) && status != null && status.isRetryable();
		if (!retryAllowed) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		if (baseMapper.resetForRetry(taskId) <= 0) {
			// CAS 失败：并发下状态已变，避免双发
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		dispatchTrigger.dispatchAfterCommit(taskId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recall(Long taskId) {
		InAppMessageEntity task = super.getById(taskId);
		if (task == null) {
			throw new MessageException(IN_APP_SEND_TASK_NOT_FOUND, taskId);
		}

		InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
		if (status == null || !status.isRecallable()) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		Long recallUserId = SecurityUserUtils.getUserId();
		if (baseMapper.recallTask(taskId, LocalDateTime.now(), recallUserId) <= 0) {
			// CAS 失败：并发下状态已变
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<InAppMessageEntity> tasks = baseMapper.selectByIds(ids);
		boolean blocked = tasks.stream().anyMatch(task -> {
			InAppMessageStatus status = InAppMessageStatus.from(task.getStatus());
			return status == null || !status.isDeletable();
		});
		if (blocked) {
			throw new MessageException(IN_APP_SEND_TASK_OPERATION_NOT_ALLOWED);
		}

		baseMapper.deleteByIds(ids);
	}

}
