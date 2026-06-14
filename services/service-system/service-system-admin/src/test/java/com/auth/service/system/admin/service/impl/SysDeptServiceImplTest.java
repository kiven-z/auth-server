package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.query.SysDeptListQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link SysDeptServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysDeptServiceImpl 部门管理")
@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private DeptClosureMaintainer deptClosureMaintainer;

	@Mock
	private DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	@Mock
	private SysDeptDetailMapper sysDeptDetailMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	private SysDeptServiceImpl sysDeptService;

	private static SysDeptForm minimalForm(Long id, long parentId, String deptCode, String deptName, boolean status) {
		SysDeptForm form = new SysDeptForm();
		form.setId(id);
		form.setParentId(parentId);
		form.setDeptCode(deptCode);
		form.setDeptName(deptName);
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	private static SysDeptEntity activeDept(Long id, String deptCode) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setParentId(0L);
		entity.setDeptCode(deptCode);
		entity.setDeptName("原部门");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysDeptService = new SysDeptServiceImpl(auditUserDisplayService, deptReferenceChecker, deptClosureMaintainer,
				deptInvalidationTrigger, sysDeptDetailMapper, grantTableMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysDeptService, sysDeptMapper);
	}

	@Test
	@DisplayName("扁平列表：无关键词时查询全量未删除部门")
	void listFlatWithoutKeywordQueriesAllActiveDepts() {
		SysDeptEntity entity = activeDept(1L, "D001");
		when(sysDeptMapper.selectListByQuery(any(SysDeptListQuery.class))).thenReturn(List.of(entity));

		List<SysDeptListVO> result = sysDeptService.listFlat(new SysDeptListQuery());

		assertEquals(1, result.size());
		verify(sysDeptMapper).selectListByQuery(argThat((SysDeptListQuery q) -> q.getKeyword() == null));
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("扁平列表：关键词原样传给 Mapper")
	void listFlatPassesKeywordAsIs() {
		when(sysDeptMapper.selectListByQuery(any(SysDeptListQuery.class))).thenReturn(Collections.emptyList());

		SysDeptListQuery query = new SysDeptListQuery();
		query.setKeyword("  研发  ");
		sysDeptService.listFlat(query);

		verify(sysDeptMapper).selectListByQuery(argThat((SysDeptListQuery q) -> "  研发  ".equals(q.getKeyword())));
	}

	@Test
	@DisplayName("更新元数据：仅改名称时不提交授权失效")
	void updateMetaWithoutAuthImpactSkipsInvalidation() {
		Long deptId = 1L;
		SysDeptEntity existing = activeDept(deptId, "D001");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(deptReferenceChecker.normalizeAndRequireActiveParent(0L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptForm form = minimalForm(deptId, 0L, "D001", "新部门名", true);
		sysDeptService.updateMeta(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> "新部门名".equals(e.getDeptName())));
		verify(deptInvalidationTrigger, never()).submitByDeptIds(any(), any());
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

	@Test
	@DisplayName("更新元数据：状态变更时提交 USER_DEPT 授权失效")
	void updateMetaWithStatusChangeSubmitsInvalidation() {
		Long deptId = 2L;
		SysDeptEntity existing = activeDept(deptId, "D002");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(deptReferenceChecker.normalizeAndRequireActiveParent(0L)).thenReturn(0L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptForm form = minimalForm(deptId, 0L, "D002", "原部门", false);
		sysDeptService.updateMeta(form);

		verify(sysDeptMapper).updateById(argThat((SysDeptEntity e) -> BooleanUtil.isFalse(e.getStatus())));
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(deptId), "update");
		verify(deptClosureMaintainer, never()).moveNode(anyLong(), anyLong());
	}

	@Test
	@DisplayName("更新元数据：父级变更时维护闭包并提交 move 失效")
	void updateMetaWithParentChangeSubmitsMoveInvalidation() {
		Long deptId = 3L;
		SysDeptEntity existing = activeDept(deptId, "D003");
		when(deptReferenceChecker.getExistingActive(deptId)).thenReturn(existing);
		when(deptReferenceChecker.normalizeAndRequireActiveParent(10L)).thenReturn(10L);
		when(sysDeptMapper.updateById(any(SysDeptEntity.class))).thenReturn(1);

		SysDeptForm form = minimalForm(deptId, 10L, "D003", "原部门", true);
		sysDeptService.updateMeta(form);

		verify(deptClosureMaintainer).moveNode(deptId, 10L);
		verify(deptInvalidationTrigger).submitByDeptIds(List.of(deptId), "move");
	}

}
