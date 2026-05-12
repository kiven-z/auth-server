package com.auth.service.system.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostCreateForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysPostServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPostServiceImpl 岗位管理")
@ExtendWith(MockitoExtension.class)
class SysPostServiceImplTest {

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysPostServiceImpl sysPostService;

	@BeforeEach
	void setUp() throws Exception {
		sysPostService = new SysPostServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPostService, sysPostMapper);
	}

	@Test
	@DisplayName("分页查询：Mapper 分页后调用审计用户展示名补全")
	void getPageDelegatesEnrichToAuditUserDisplayService() {
		Page<SysPostEntity> pageParams = new Page<>(1, 10);
		SysPostQuery query = new SysPostQuery();
		Page<SysPostPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(new SysPostPageRowPO()));
		when(sysPostMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(poPage);

		PageResponse<SysPostPageResponse> result = sysPostService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<IPage<SysPostPageResponse>>any());
	}

	@Test
	@DisplayName("新增：部门不可用时抛出 POST_DEPT_NOT_FOUND")
	void createThrowsWhenDeptNotAssignable() {
		when(sysPostMapper.countAssignableDept(99L)).thenReturn(0);
		SysPostCreateForm form = minimalForm(99L, "P1", "岗位一");

		assertThatThrownBy(() -> sysPostService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.POST_DEPT_NOT_FOUND);
	}

	@Test
	@DisplayName("新增：同部门岗位编码重复时抛出 POST_CODE_DUPLICATE_IN_DEPT")
	void createThrowsWhenPostCodeDuplicateInDept() {
		when(sysPostMapper.countAssignableDept(1L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(1L);
		SysPostCreateForm form = minimalForm(1L, "DUP", "重复");

		assertThatThrownBy(() -> sysPostService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.POST_CODE_DUPLICATE_IN_DEPT);
	}

	@Test
	@DisplayName("新增：校验通过后持久化插入")
	void createPersistsWhenValid() {
		when(sysPostMapper.countAssignableDept(2L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.insert(any(SysPostEntity.class))).thenReturn(1);
		SysPostCreateForm form = minimalForm(2L, "NEW_CODE", "新岗位");

		sysPostService.create(form);

		verify(sysPostMapper).insert(argThat(
				(SysPostEntity e) -> Long.valueOf(2L).equals(e.getDeptId()) && "NEW_CODE".equals(e.getPostCode())
						&& Boolean.TRUE.equals(e.getStatus()) && Integer.valueOf(0).equals(e.getOrderNum())));
	}

	private static SysPostCreateForm minimalForm(Long deptId, String postCode, String postName) {
		SysPostCreateForm form = new SysPostCreateForm();
		form.setDeptId(deptId);
		form.setPostCode(postCode);
		form.setPostName(postName);
		form.setStatus(true);
		form.setOrderNum(0);
		return form;
	}

}
