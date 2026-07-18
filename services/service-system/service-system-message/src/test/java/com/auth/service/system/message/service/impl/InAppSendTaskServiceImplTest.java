package com.auth.service.system.message.service.impl;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.enums.*;
import com.auth.service.system.message.model.form.inapp.InAppComposeForm;
import com.auth.service.system.message.model.vo.InAppComposeResultVO;
import com.auth.service.system.message.support.inapp.InAppComposeDispatchTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.auth.service.system.message.exception.MessageResultCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link InAppSendTaskServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppSendTaskServiceImpl 管理端按范围发送站内信")
@ExtendWith(MockitoExtension.class)
class InAppSendTaskServiceImplTest {

	@Mock
	private InAppMessageMapper inAppMessageMapper;

	@Mock
	private InAppComposeDispatchTrigger dispatchTrigger;

	private InAppSendTaskServiceImpl service;

	/**
	 * 组装可补发校验用的管理端定向写扩散任务
	 */
	private static InAppMessageEntity adminComposePushTask(Long id, InAppMessageStatus status) {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(id);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setDeliveryMode(DeliveryMode.PUSH.name());
		task.setRecipientScopeType(RecipientScopeType.DEPT.name());
		task.setStatus(status.name());
		return task;
	}

	@BeforeEach
	void setUp() {
		// ServiceImpl.baseMapper 由 Spring 字段注入；单测手动挂上
		service = new InAppSendTaskServiceImpl(dispatchTrigger);
		ReflectionTestUtils.setField(service, "baseMapper", inAppMessageMapper);
	}

	@Test
	@DisplayName("定向 PUSH：写 PENDING 任务并 afterCommit 派发")
	void send_pushScope_shouldCreatePendingTaskAndDispatch() {
		// 校验落库字段与派发时机：定向受理即 PENDING，实投由异步回写
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.DEPT.name());
		form.setRecipientScopeIds(List.of(3L));
		form.setIncludeChildren(true);
		form.setTemplateCode("notice");
		form.setTitle("Hello");
		form.setBody("body text");
		form.setContentType("MARKDOWN");
		form.setBizType("NOTICE");
		form.setLinkUrl("/home");

		doAnswer(invocation -> {
			InAppMessageEntity task = invocation.getArgument(0);
			task.setId(9001L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(7L);

			InAppComposeResultVO result = service.send(form);

			assertThat(result.getTaskId()).isEqualTo(9001L);
			assertThat(result.getTotalCount()).isZero();
			assertThat(result.getSuccessCount()).isZero();
			assertThat(result.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getSourceType()).isEqualTo(MessageSendSourceType.ADMIN_COMPOSE.name());
		assertThat(task.getSceneCode()).isEqualTo("notice");
		assertThat(task.getTitle()).isEqualTo("Hello");
		assertThat(task.getContent()).isEqualTo("body text");
		assertThat(task.getContentType()).isEqualTo(InAppContentType.MARKDOWN.name());
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.DEPT.name());
		assertThat(task.getDeliveryMode()).isEqualTo(DeliveryMode.PUSH.name());
		assertThat(task.getRecipientScopeJson()).contains("\"includeChildren\":true");
		assertThat(task.getBizType()).isEqualTo("NOTICE");
		assertThat(task.getLinkUrl()).isEqualTo("/home");
		assertThat(task.getSenderUserId()).isEqualTo(7L);
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		assertThat(task.getTotalCount()).isZero();
		assertThat(task.getSuccessCount()).isZero();

		verify(dispatchTrigger).dispatchAfterCommit(9001L);
	}

	@Test
	@DisplayName("全员 PULL：直接 SUCCESS，不异步派发")
	void send_allScope_shouldPublishWithoutDispatch() {
		// 公开读扩散只落任务，不写收件箱、不走 dispatch
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.ALL.name());
		form.setTitle("Broadcast");
		form.setBody("all users");
		form.setContentType("TEXT");

		doAnswer(invocation -> {
			invocation.getArgument(0, InAppMessageEntity.class).setId(8001L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(1L);

			InAppComposeResultVO result = service.send(form);

			assertThat(result.getTaskId()).isEqualTo(8001L);
			assertThat(result.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		InAppMessageEntity task = taskCaptor.getValue();
		assertThat(task.getRecipientScopeType()).isEqualTo(RecipientScopeType.ALL.name());
		assertThat(task.getDeliveryMode()).isEqualTo(DeliveryMode.PULL.name());
		assertThat(task.getStatus()).isEqualTo(InAppMessageStatus.SUCCESS.name());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("DEPT includeChildren=false 写入范围快照")
	void send_deptWithoutChildren_shouldPersistFlag() {
		// 子部门开关需原样写入 recipient_scope_json
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType(RecipientScopeType.DEPT.name());
		form.setRecipientScopeIds(List.of(5L));
		form.setIncludeChildren(false);
		form.setTitle("t");
		form.setBody("b");
		form.setContentType("TEXT");

		doAnswer(invocation -> {
			invocation.getArgument(0, InAppMessageEntity.class).setId(2L);
			return 1;
		}).when(inAppMessageMapper).insert(any(InAppMessageEntity.class));

		try (MockedStatic<SecurityUserUtils> security = mockStatic(SecurityUserUtils.class)) {
			security.when(SecurityUserUtils::getUserId).thenReturn(1L);
			service.send(form);
		}

		ArgumentCaptor<InAppMessageEntity> taskCaptor = ArgumentCaptor.forClass(InAppMessageEntity.class);
		verify(inAppMessageMapper).insert(taskCaptor.capture());
		assertThat(taskCaptor.getValue().getRecipientScopeJson()).contains("\"includeChildren\":false");
		verify(dispatchTrigger).dispatchAfterCommit(2L);
	}

	@Test
	@DisplayName("非法范围类型：抛范围非法且不落库")
	void send_shouldRejectInvalidScopeType() {
		// 校验失败时不得 insert / 派发
		InAppComposeForm form = new InAppComposeForm();
		form.setRecipientScopeType("UNKNOWN");
		form.setTitle("t");
		form.setBody("b");
		form.setContentType("TEXT");

		assertThatThrownBy(() -> service.send(form)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
		verify(inAppMessageMapper, never()).insert(any(InAppMessageEntity.class));
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：PARTIAL 重置为 PENDING 并派发")
	void retry_shouldResetPartialAndDispatch() {
		// 可恢复状态走 resetForRetry + afterCommit
		InAppMessageEntity task = adminComposePushTask(20L, InAppMessageStatus.PARTIAL);
		when(inAppMessageMapper.selectById(20L)).thenReturn(task);
		when(inAppMessageMapper.resetForRetry(20L)).thenReturn(1);

		InAppComposeResultVO result = service.retry(20L);

		assertThat(result.getTaskId()).isEqualTo(20L);
		assertThat(result.getStatus()).isEqualTo(InAppMessageStatus.PENDING.name());
		verify(inAppMessageMapper).resetForRetry(20L);
		verify(dispatchTrigger).dispatchAfterCommit(20L);
	}

	@Test
	@DisplayName("补发：任务不存在")
	void retry_shouldRejectWhenTaskMissing() {
		when(inAppMessageMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> service.retry(99L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：SUCCESS 拒绝")
	void retry_shouldRejectSuccess() {
		when(inAppMessageMapper.selectById(21L)).thenReturn(adminComposePushTask(21L, InAppMessageStatus.SUCCESS));

		assertThatThrownBy(() -> service.retry(21L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：RECALLED 拒绝")
	void retry_shouldRejectRecalled() {
		when(inAppMessageMapper.selectById(23L)).thenReturn(adminComposePushTask(23L, InAppMessageStatus.RECALLED));

		assertThatThrownBy(() -> service.retry(23L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
	}

	@Test
	@DisplayName("补发：公开 PULL 拒绝")
	void retry_shouldRejectPullDelivery() {
		// 读扩散发布即成功，补发无意义
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(26L);
		task.setSourceType(MessageSendSourceType.ADMIN_COMPOSE.name());
		task.setDeliveryMode(DeliveryMode.PULL.name());
		task.setRecipientScopeType(RecipientScopeType.ALL.name());
		task.setStatus(InAppMessageStatus.SUCCESS.name());
		when(inAppMessageMapper.selectById(26L)).thenReturn(task);

		assertThatThrownBy(() -> service.retry(26L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

	@Test
	@DisplayName("补发：非管理端群发拒绝")
	void retry_shouldRejectNonAdminCompose() {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(22L);
		task.setSourceType("TEMPLATE");
		task.setDeliveryMode(DeliveryMode.PUSH.name());
		task.setStatus(InAppMessageStatus.FAILED.name());
		when(inAppMessageMapper.selectById(22L)).thenReturn(task);

		assertThatThrownBy(() -> service.retry(22L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED);
		verify(inAppMessageMapper, never()).resetForRetry(any());
	}

	@Test
	@DisplayName("补发：reset 影响 0 行时拒绝（状态已变）")
	void retry_shouldRejectWhenResetAffectsZeroRows() {
		// CAS/并发下 reset 失败：用最新状态抛不可补发
		InAppMessageEntity task = adminComposePushTask(25L, InAppMessageStatus.FAILED);
		InAppMessageEntity latest = adminComposePushTask(25L, InAppMessageStatus.SUCCESS);
		when(inAppMessageMapper.selectById(25L)).thenReturn(task, latest);
		when(inAppMessageMapper.resetForRetry(25L)).thenReturn(0);

		assertThatThrownBy(() -> service.retry(25L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_RETRY_NOT_ALLOWED);
		verify(dispatchTrigger, never()).dispatchAfterCommit(any());
	}

}
