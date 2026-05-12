package com.auth.service.system.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * {@link SysDeptServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysDeptServiceImpl 部门查询")
@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysDeptServiceImpl sysDeptService;

	@BeforeEach
	void setUp() throws Exception {
		sysDeptService = new SysDeptServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysDeptService, sysDeptMapper);
	}

	@Test
	@DisplayName("扁平列表：Mapper 查询后调用审计用户展示名补全")
	void listFlatDelegatesEnrichToAuditUserDisplayService() {
		SysDeptQuery query = new SysDeptQuery();
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(1L);
		entity.setParentId(0L);
		entity.setDeptName("研发");
		entity.setDeptCode("RD");
		entity.setStatus(true);
		entity.setOrderNum(1);
		when(sysDeptMapper.selectListByQuery(eq(query))).thenReturn(List.of(entity));

		List<SysDeptListVO> result = sysDeptService.listFlat(query);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getDeptName()).isEqualTo("研发");
		verify(auditUserDisplayService).enrichAuditUsernames(result);
	}

	@Test
	@DisplayName("分页查询：Mapper 分页后调用审计用户展示名补全")
	void pageFlatDelegatesEnrichToAuditUserDisplayService() {
		Page<SysDeptEntity> pageParams = new Page<>(1, 10);
		SysDeptQuery query = new SysDeptQuery();
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(2L);
		entity.setDeptName("市场");
		Page<SysDeptEntity> entityPage = new Page<>(1, 10);
		entityPage.setRecords(List.of(entity));
		entityPage.setTotal(1L);
		when(sysDeptMapper.selectPageByQuery(eq(pageParams), eq(query))).thenReturn(entityPage);

		PageResponse<SysDeptListVO> result = sysDeptService.pageFlat(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getDeptName()).isEqualTo("市场");
		verify(auditUserDisplayService).enrichAuditUsernames(org.mockito.ArgumentMatchers.<IPage<SysDeptListVO>>any());
	}

}
