package com.auth.service.system.message.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.po.MessageTemplatePageRowPO;
import com.auth.service.system.message.model.query.MessageTemplateQuery;
import com.auth.service.system.message.model.vo.MessageTemplateDetailVO;
import com.auth.service.system.message.model.vo.MessageTemplatePageResponse;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysMessageTemplateServiceImpl} 分页、详情、删除与启停单元测试
 */
@DisplayName("SysMessageTemplateServiceImpl 消息模板公共服务")
@ExtendWith(MockitoExtension.class)
class SysMessageTemplateServiceImplTest {

	@Mock
	private MessageTemplateMapper messageTemplateMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private SysMessageTemplateServiceImpl sysMessageTemplateService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		// LambdaUpdateWrapper / LambdaQueryWrapper 需要实体表元数据
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				MessageTemplateEntity.class);
	}

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

	@Test
	@DisplayName("详情：渠道匹配时返回 VO 并填充审计用户名")
	void getMessageTemplateById_shouldReturnDetailWhenChannelMatches() {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setId(10L);
		entity.setChannel(MessageChannel.EMAIL.name());
		entity.setSceneCode("login-code");
		entity.setTemplateName("登录邮件");
		entity.setSubject("验证码");
		entity.setBodyContent("<p>${code}</p>");
		entity.setRequireFields("[]");
		entity.setPriority(1);
		entity.setStatus(true);

		when(messageTemplateMapper.selectById(10L)).thenReturn(entity);

		MessageTemplateDetailVO vo = sysMessageTemplateService.getMessageTemplateById(10L, MessageChannel.EMAIL.name());

		assertThat(vo.getTemplateCode()).isEqualTo("login-code");
		assertThat(vo.getChannel()).isEqualTo(MessageChannel.EMAIL.name());
		assertThat(vo.getContent()).isEqualTo("<p>${code}</p>");
		assertThat(vo.getRequireFields()).isEmpty();
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：渠道不匹配时抛出 DATA_NOT_EXIST")
	void getMessageTemplateById_shouldThrowWhenChannelMismatch() {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setId(10L);
		entity.setChannel(MessageChannel.EMAIL.name());
		when(messageTemplateMapper.selectById(10L)).thenReturn(entity);

		assertThatThrownBy(() -> sysMessageTemplateService.getMessageTemplateById(10L, MessageChannel.IN_APP.name()))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("详情：记录不存在时抛出 DATA_NOT_EXIST")
	void getMessageTemplateById_shouldThrowWhenMissing() {
		when(messageTemplateMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysMessageTemplateService.getMessageTemplateById(99L, MessageChannel.EMAIL.name()))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("批量删除：按 ids 与 channel 条件删除")
	void batchDelete_shouldDeleteByIdsAndChannel() {
		// IService.remove 最终仍走 baseMapper.delete，仅删除指定渠道下行
		List<Long> ids = List.of(1L, 2L);
		when(messageTemplateMapper.delete(any())).thenReturn(2);

		sysMessageTemplateService.batchDelete(ids, MessageChannel.EMAIL.name());

		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		verify(messageTemplateMapper).delete(wrapperCaptor.capture());
		assertThat(wrapperCaptor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("批量删除：ids 为空时不调用删除")
	void batchDelete_shouldSkipWhenIdsEmpty() {
		sysMessageTemplateService.batchDelete(Collections.emptyList(), MessageChannel.EMAIL.name());

		verify(messageTemplateMapper, never()).delete(any());
	}

	@Test
	@DisplayName("批量启停：按 ids 与 channel 更新 status")
	void batchUpdateStatus_shouldUpdateByIdsAndChannel() {
		// IService.update 最终仍走 baseMapper.update，仅更新指定渠道下行
		List<Long> ids = List.of(1L, 2L);
		when(messageTemplateMapper.update(any(), any())).thenReturn(2);

		sysMessageTemplateService.batchUpdateStatus(ids, true, MessageChannel.EMAIL.name());

		ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		verify(messageTemplateMapper).update(isNull(), wrapperCaptor.capture());
		assertThat(wrapperCaptor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("批量启停：ids 为空时不调用更新")
	void batchUpdateStatus_shouldSkipWhenIdsEmpty() {
		sysMessageTemplateService.batchUpdateStatus(Collections.emptyList(), false, MessageChannel.EMAIL.name());

		verify(messageTemplateMapper, never()).update(any(), any());
	}

}
