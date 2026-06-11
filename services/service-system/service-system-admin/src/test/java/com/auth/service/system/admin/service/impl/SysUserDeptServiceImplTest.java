package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.DeptReferenceMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserDeptServiceImpl} 单元测试
 */
@DisplayName("SysUserDeptServiceImpl 用户部门关联")
@ExtendWith(MockitoExtension.class)
class SysUserDeptServiceImplTest {

	private static final Long USER_ID = 10L;

	private static final Long RELATION_ID = 100L;

	private static final Long DEPT_ID = 20L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private DeptReferenceMapper deptReferenceMapper;

	@Mock
	private UserOperationChecker userOperationChecker;

	@Mock
	private UserDeptMapper userDeptMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Captor
	private ArgumentCaptor<UserDeptEntity> entityCaptor;

	private SysUserDeptServiceImpl sysUserDeptService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserDeptServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	private static SysDeptEntity activeDept() {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(DEPT_ID);
		entity.setDeptName("研发部");
		entity.setDeptCode("RD");
		return entity;
	}

	private static UserDeptAssignForm createForm(Integer status, Boolean isPrimary, String remark) {
		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setDeptId(DEPT_ID);
		form.setStatus(status);
		form.setIsPrimary(isPrimary);
		form.setRemark(remark);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		DeptReferenceChecker deptReferenceChecker = new DeptReferenceChecker(sysDeptMapper, deptReferenceMapper);
		sysUserDeptService = new SysUserDeptServiceImpl(userReferenceChecker, userOperationChecker,
				deptReferenceChecker, userAuthorizationInvalidationTrigger, auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserDeptService, userDeptMapper);
	}

	@Test
	@DisplayName("分页查询：映射业务字段并补充审计用户名")
	void getPageMapsRowsAndEnrichesAuditUsernames() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptPageQuery query = new UserDeptPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		LocalDateTime now = LocalDateTime.of(2026, 6, 11, 10, 0, 0);
		UserDeptPageRowPO row = new UserDeptPageRowPO();
		row.setId(RELATION_ID);
		row.setUserId(USER_ID);
		row.setDeptId(DEPT_ID);
		row.setDeptName("研发部");
		row.setDeptCode("RD");
		row.setIsPrimary(true);
		row.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());
		row.setRemark("负责人");
		row.setCreateTime(now);
		row.setUpdateTime(now);
		row.setCreateUser(1L);
		row.setUpdateUser(1L);
		Page<UserDeptPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(row));
		poPage.setTotal(1);
		when(userDeptMapper.selectListByPage(any(Page.class), eq(USER_ID), eq(query))).thenReturn(poPage);

		PageResponse<UserDeptPageVO> result = sysUserDeptService.getPage(USER_ID, query);

		assertThat(result.getList()).hasSize(1);
		UserDeptPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(RELATION_ID);
		assertThat(vo.getUserId()).isEqualTo(USER_ID);
		assertThat(vo.getDeptId()).isEqualTo(DEPT_ID);
		assertThat(vo.getDeptName()).isEqualTo("研发部");
		assertThat(vo.getDeptCode()).isEqualTo("RD");
		assertThat(vo.getIsPrimary()).isTrue();
		assertThat(vo.getStatus()).isEqualTo(UserOrgRelationStatusEnum.NORMAL.getCode());
		assertThat(vo.getRemark()).isEqualTo("负责人");
		assertThat(vo.getCreateTime()).isEqualTo(now);
		assertThat(vo.getUpdateTime()).isEqualTo(now);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

	@Test
	@DisplayName("创建关联：非主部门时不降级其他关联")
	void createNonPrimarySkipsDemote() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.insert(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.create(USER_ID, createForm(UserOrgRelationStatusEnum.NORMAL.getCode(), false, "备注"));

		verify(userDeptMapper, never()).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).insert(entityCaptor.capture());
		UserDeptEntity saved = entityCaptor.getValue();
		assertThat(saved.getUserId()).isEqualTo(USER_ID);
		assertThat(saved.getDeptId()).isEqualTo(DEPT_ID);
		assertThat(saved.getIsPrimary()).isFalse();
		assertThat(saved.getStatus()).isEqualTo(UserOrgRelationStatusEnum.NORMAL.getCode());
		assertThat(saved.getRemark()).isEqualTo("备注");
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
	}

	@Test
	@DisplayName("创建关联：主部门时降级其他有效关联")
	void createPrimaryDemotesExistingPrimary() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.insert(any(UserDeptEntity.class))).thenReturn(1);

		sysUserDeptService.create(USER_ID, createForm(UserOrgRelationStatusEnum.NORMAL.getCode(), true, null));

		verify(userDeptMapper).demotePrimaryByUserId(USER_ID);
		verify(userDeptMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getIsPrimary()).isTrue();
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
	}

	@Test
	@DisplayName("创建关联：重复部门时拒绝保存")
	void createRejectsDuplicateDeptRelation() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(1);

		assertThatThrownBy(() -> sysUserDeptService.create(USER_ID,
				createForm(UserOrgRelationStatusEnum.NORMAL.getCode(), false, null)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_DUPLICATE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	@Test
	@DisplayName("创建关联：非正常状态不可设为主部门")
	void createRejectsPrimaryWhenStatusNotNormal() {
		stubCreatePrerequisites();
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);

		assertThatThrownBy(() -> sysUserDeptService.create(USER_ID,
				createForm(UserOrgRelationStatusEnum.TRANSFERRED.getCode(), true, null)))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	private void stubCreatePrerequisites() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(activeDept());
	}

}
