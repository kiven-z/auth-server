package com.auth.service.system.message.service.impl;

import java.util.Collections;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysMessageTemplateServiceImpl} 分页查询单元测试
 */
@DisplayName("SysMessageTemplateServiceImpl 消息模板分页")
@ExtendWith(MockitoExtension.class)
class SysMessageTemplateServiceImplTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private SysMessageTemplateServiceImpl sysMessageTemplateService;

	@Test
	@DisplayName("按传入 channel 分页并填充审计用户名")
	void getMessageTemplatePage_shouldQueryByChannelAndEnrichAudit() {
		// 准备 EMAIL 渠道查询与 Mapper 返回行
		MessageTemplateQuery query = new MessageTemplateQuery();
		query.setChannel(MessageChannel.EMAIL.name());
		query.setPageIndex(1);
		query.setPageSize(10);

		MessageTemplatePageRowPO row = new MessageTemplatePageRowPO();
		row.setId(1L);
		row.setChannel(MessageChannel.EMAIL.name());
		row.setTemplateCode("login-code");
		row.setTemplateName("登录邮件");

		Page<MessageTemplatePageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(List.of(row));
		mapperPage.setTotal(1);

		when(messageTemplateMapper.selectMessageTemplatePage(any(Page.class), eq(query))).thenReturn(mapperPage);

		Page<MessageTemplateEntity> pageParams = new Page<>(1, 10);
		PageResponse<MessageTemplatePageResponse> result = sysMessageTemplateService.getMessageTemplatePage(pageParams,
				query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getChannel()).isEqualTo(MessageChannel.EMAIL.name());
		assertThat(result.getList().get(0).getTemplateCode()).isEqualTo("login-code");
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());

		ArgumentCaptor<MessageTemplateQuery> queryCaptor = ArgumentCaptor.forClass(MessageTemplateQuery.class);
		verify(messageTemplateMapper).selectMessageTemplatePage(any(Page.class), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getChannel()).isEqualTo(MessageChannel.EMAIL.name());
	}

	@Test
	@DisplayName("channel 无匹配数据：原样下传并返回空列表")
	void getMessageTemplatePage_shouldPassThroughChannelAndReturnEmpty() {
		// 库中不存在该渠道时，不在 Service 层拦截，交由 SQL 过滤为空
		MessageTemplateQuery query = new MessageTemplateQuery();
		query.setChannel("FAX");

		Page<MessageTemplatePageRowPO> mapperPage = new Page<>(1, 10);
		mapperPage.setRecords(Collections.emptyList());
		mapperPage.setTotal(0);
		when(messageTemplateMapper.selectMessageTemplatePage(any(Page.class), eq(query))).thenReturn(mapperPage);

		PageResponse<MessageTemplatePageResponse> result = sysMessageTemplateService
			.getMessageTemplatePage(new Page<>(1, 10), query);

		assertThat(result.getList()).isEmpty();
		assertThat(result.getTotal()).isZero();

		ArgumentCaptor<MessageTemplateQuery> queryCaptor = ArgumentCaptor.forClass(MessageTemplateQuery.class);
		verify(messageTemplateMapper).selectMessageTemplatePage(any(Page.class), queryCaptor.capture());
		assertThat(queryCaptor.getValue().getChannel()).isEqualTo("FAX");
	}

}
