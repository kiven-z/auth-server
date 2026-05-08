package com.auth.service.system.schedule.service.impl;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysJobGroupServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobGroupServiceImpl 定时任务分组")
@ExtendWith(MockitoExtension.class)
class SysJobGroupServiceImplTest {

	@Mock
	private SysJobGroupMapper sysJobGroupMapper;

	@Mock
	private SysJobMapper sysJobMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysJobGroupServiceImpl sysJobGroupService;

	@BeforeEach
	void setUp() throws Exception {
		sysJobGroupService = new SysJobGroupServiceImpl(auditUserDisplayService, sysJobMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobGroupService, sysJobGroupMapper);
	}

	@Test
	@DisplayName("分页查询：补全审计展示名")
	void getPageEnrichesUsernames() {
		Page<SysJobGroupEntity> pageParams = new Page<>(1, 10);
		SysJobGroupQuery query = new SysJobGroupQuery();
		Page<SysJobGroupPageResponse> mpPage = new Page<>(1, 10);
		mpPage.setRecords(List.of(new SysJobGroupPageResponse()));
		when(sysJobGroupMapper.selectListByPage(pageParams, query)).thenReturn(mpPage);

		sysJobGroupService.getPage(pageParams, query);

		verify(auditUserDisplayService).enrichAuditUsernames(mpPage);
	}

	@Test
	@DisplayName("启用下拉：limit 限制在 1～60，keyword trim，并补全展示名")
	void listEnabledOptionsClampsLimitAndTrimsKeyword() {
		List<SysJobGroupPageResponse> rows = List.of(new SysJobGroupPageResponse());
		when(sysJobGroupMapper.selectEnabledOptions("kw", 60)).thenReturn(rows);

		List<SysJobGroupPageResponse> result = sysJobGroupService.listEnabledOptions("  kw  ", 999);

		assertThat(result).isSameAs(rows);
		verify(sysJobGroupMapper).selectEnabledOptions("kw", 60);
		verify(auditUserDisplayService).enrichAuditUsernames(rows);
	}

	@Test
	@DisplayName("启用下拉：limit 小于 1 时按 1 处理")
	void listEnabledOptionsClampsMinimumLimit() {
		when(sysJobGroupMapper.selectEnabledOptions(null, 1)).thenReturn(List.of());

		sysJobGroupService.listEnabledOptions(null, 0);

		verify(sysJobGroupMapper).selectEnabledOptions(null, 1);
	}

	@Test
	@DisplayName("新增：分组编码重复时抛出 JOB_GROUP_CODE_DUPLICATE")
	void createRejectsDuplicateGroupCode() {
		when(sysJobGroupMapper.selectCount(any())).thenReturn(1L);
		SysJobGroupForm form = minimalForm("DUP", "重复");

		assertThatThrownBy(() -> sysJobGroupService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("新增：校验通过则 insert")
	void createInsertsWhenCodeUnique() {
		when(sysJobGroupMapper.selectCount(any())).thenReturn(0L);
		when(sysJobGroupMapper.insert(org.mockito.ArgumentMatchers.<SysJobGroupEntity>any())).thenReturn(1);
		SysJobGroupForm form = minimalForm("UNIQ_GROUP", "唯一分组");

		sysJobGroupService.create(form);

		verify(sysJobGroupMapper).insert(argThat(
				(SysJobGroupEntity e) -> "UNIQ_GROUP".equals(e.getGroupCode()) && Boolean.TRUE.equals(e.getStatus())
						&& Boolean.FALSE.equals(e.getIsSystem()) && Integer.valueOf(0).equals(e.getOrderNum())));
	}

	@Test
	@DisplayName("删除：分组不存在时抛出 DATA_NOT_EXIST")
	void deleteByIdRejectsWhenMissing() {
		when(sysJobGroupMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("删除：系统内置分组不可删")
	void deleteByIdRejectsSystemGroup() {
		SysJobGroupEntity entity = new SysJobGroupEntity();
		entity.setId(1L);
		entity.setGroupCode("SYS");
		entity.setIsSystem(true);
		when(sysJobGroupMapper.selectById(1L)).thenReturn(entity);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(1L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_SYSTEM_PROTECTED);
	}

	@Test
	@DisplayName("删除：存在关联任务时抛出 JOB_GROUP_HAS_ACTIVE_JOBS")
	void deleteByIdRejectsWhenJobsExist() {
		SysJobGroupEntity entity = new SysJobGroupEntity();
		entity.setId(2L);
		entity.setGroupCode("G1");
		entity.setIsSystem(false);
		when(sysJobGroupMapper.selectById(2L)).thenReturn(entity);
		when(sysJobMapper.selectCount(any())).thenReturn(3L);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(2L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_HAS_ACTIVE_JOBS);
	}

	@Test
	@DisplayName("删除：非内置且无关联任务时 removeById")
	void deleteByIdRemovesWhenAllowed() {
		SysJobGroupEntity entity = new SysJobGroupEntity();
		entity.setId(3L);
		entity.setGroupCode("G2");
		entity.setIsSystem(false);
		when(sysJobGroupMapper.selectById(3L)).thenReturn(entity);
		when(sysJobMapper.selectCount(any())).thenReturn(0L);
		when(sysJobGroupMapper.deleteById(3L)).thenReturn(1);

		sysJobGroupService.deleteById(3L);

		verify(sysJobGroupMapper).deleteById(3L);
	}

	/** 最小合法新增表单 */
	private static SysJobGroupForm minimalForm(String groupCode, String groupName) {
		SysJobGroupForm form = new SysJobGroupForm();
		form.setGroupCode(groupCode);
		form.setGroupName(groupName);
		form.setStatus(true);
		return form;
	}

}
