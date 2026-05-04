package com.auth.service.system.admin.role.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth.common.core.model.query.SortDirection;
import com.auth.common.core.model.query.SortSpec;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.mapper.SysRoleMapper;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.support.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 角色分页服务单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysRoleServiceImpl 分页与排序")
@SuppressWarnings("unchecked")
class SysRoleServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	private SysRoleServiceImpl sysRoleService;

	@BeforeEach
	void setUp() {
		sysRoleService = new SysRoleServiceImpl();
		ReflectionTestUtils.setField(sysRoleService, "baseMapper", sysRoleMapper);
	}

	@Test
	@DisplayName("sort 为空时 ORDER BY 使用默认片段")
	void getPageUsesDefaultOrderWhenSortEmpty() {
		ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
		when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
			.thenAnswer(invocation -> {
				Page<SysRoleEntity> page = invocation.getArgument(0);
				page.setTotal(0L);
				page.setRecords(Collections.emptyList());
				return page;
			});

		SysRoleQuery query = new SysRoleQuery();
		query.setSort(null);

		PageResponse<SysRolePageResponse> page = sysRoleService.getPage(new Page<>(1, 10), query);

		assertThat(page.getList()).isEmpty();
		assertThat(orderCaptor.getValue()).isEqualTo(SysRolePageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
	}

	@Test
	@DisplayName("非法排序字段被忽略后回退默认 ORDER BY")
	void getPageFallsBackWhenOnlyInvalidSortFields() {
		ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
		when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
			.thenAnswer(invocation -> {
				Page<SysRoleEntity> page = invocation.getArgument(0);
				page.setTotal(0L);
				page.setRecords(Collections.emptyList());
				return page;
			});

		SysRoleQuery query = new SysRoleQuery();
		SortSpec bad = new SortSpec();
		bad.setField("dropTable");
		bad.setDirection(SortDirection.ASC);
		query.setSort(List.of(bad));

		sysRoleService.getPage(new Page<>(1, 10), query);

		assertThat(orderCaptor.getValue()).isEqualTo(SysRolePageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
	}

	@Test
	@DisplayName("白名单字段 orderNum ASC 生成对应列排序")
	void getPageMapsWhitelistFieldToColumn() {
		ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
		when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
			.thenAnswer(invocation -> {
				Page<SysRoleEntity> page = invocation.getArgument(0);
				page.setTotal(0L);
				page.setRecords(Collections.emptyList());
				return page;
			});

		SysRoleQuery query = new SysRoleQuery();
		SortSpec spec = new SortSpec();
		spec.setField("orderNum");
		spec.setDirection(SortDirection.ASC);
		query.setSort(List.of(spec));

		sysRoleService.getPage(new Page<>(1, 10), query);

		assertThat(orderCaptor.getValue()).isEqualTo("r.order_num ASC");
	}

	@Test
	@DisplayName("多字段排序按传入顺序拼接")
	void getPageJoinsMultipleSortSegments() {
		ArgumentCaptor<String> orderCaptor = ArgumentCaptor.forClass(String.class);
		when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), orderCaptor.capture()))
			.thenAnswer(invocation -> {
				Page<SysRoleEntity> page = invocation.getArgument(0);
				page.setTotal(0L);
				page.setRecords(Collections.emptyList());
				return page;
			});

		SysRoleQuery query = new SysRoleQuery();
		SortSpec first = new SortSpec();
		first.setField("orderNum");
		first.setDirection(SortDirection.ASC);
		SortSpec second = new SortSpec();
		second.setField("createTime");
		second.setDirection(SortDirection.DESC);
		query.setSort(List.of(first, second));

		sysRoleService.getPage(new Page<>(1, 10), query);

		assertThat(orderCaptor.getValue()).isEqualTo("r.order_num ASC, r.create_time DESC");
	}

	@Test
	@DisplayName("查询条件传入后原样交给 Mapper")
	void getPagePassesQueryToMapper() {
		when(sysRoleMapper.selectListByPage(any(Page.class), any(SysRoleQuery.class), any()))
			.thenAnswer(invocation -> {
				Page<SysRoleEntity> page = invocation.getArgument(0);
				page.setTotal(0L);
				page.setRecords(Collections.emptyList());
				return page;
			});

		SysRoleQuery query = new SysRoleQuery();
		query.setRoleCode("ADM");
		query.setRoleName("管理");
		query.setStatus(0);
		query.setRoleType(1);

		sysRoleService.getPage(new Page<>(1, 20), query);

		verify(sysRoleMapper).selectListByPage(any(Page.class), eq(query), any());
	}

}
