package com.auth.service.system.admin.service.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.form.userdept.UserDeptUpdateForm;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.admin.support.userdept.UserDeptPrimaryCoordinator;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
	private UserOperationChecker userOperationChecker;

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private UserDeptMapper userDeptMapper;

	@Mock
	private UserDeptPrimaryCoordinator userDeptPrimaryCoordinator;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	@Mock
	private DeptAuthorizationInvalidationTrigger deptAuthorizationInvalidationTrigger;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysUserDeptServiceImpl sysUserDeptService;

	@BeforeEach
	void setUp() {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserDeptService = new SysUserDeptServiceImpl(userReferenceChecker, userOperationChecker,
				deptReferenceChecker, userDeptMapper, userDeptPrimaryCoordinator, userAuthorizationInvalidationTrigger,
				deptAuthorizationInvalidationTrigger, auditUserDisplayService);
	}

	@Test
	@DisplayName("新增：写入关联并触发授权失效")
	void createInsertsRelationAndSubmitsInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.selectActiveByUserId(USER_ID)).thenReturn(List.of());
		when(deptReferenceChecker.requireGrantEligible(DEPT_ID)).thenReturn(activeDept(DEPT_ID));

		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setDeptId(DEPT_ID);
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());

		sysUserDeptService.create(USER_ID, form);

		verify(userOperationChecker).requireOperable(List.of(USER_ID));
		ArgumentCaptor<UserDeptEntity> captor = ArgumentCaptor.forClass(UserDeptEntity.class);
		verify(userDeptMapper).insert(captor.capture());
		assertThat(captor.getValue().getDeptId()).isEqualTo(DEPT_ID);
		assertThat(captor.getValue().getIsPrimary()).isTrue();
		verify(userDeptPrimaryCoordinator).ensurePrimaryInvariant(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
		verify(deptAuthorizationInvalidationTrigger).submitByDeptIds(List.of(DEPT_ID), "create-dept");
	}

	@Test
	@DisplayName("新增：重复部门抛出 USER_DEPT_DUPLICATE")
	void createThrowsWhenDuplicateDept() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(1);
		when(deptReferenceChecker.requireGrantEligible(DEPT_ID)).thenReturn(activeDept(DEPT_ID));

		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setDeptId(DEPT_ID);
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());

		assertThatThrownBy(() -> sysUserDeptService.create(USER_ID, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_DUPLICATE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
	}

	@Test
	@DisplayName("设为主部门：仅正常状态允许")
	void setPrimaryThrowsWhenStatusNotNormal() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		UserDeptEntity existing = relation(RELATION_ID, UserOrgRelationStatusEnum.PART_TIME.getCode());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);

		assertThatThrownBy(() -> sysUserDeptService.setPrimary(USER_ID, RELATION_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);
	}

	@Test
	@DisplayName("调离：状态置为已调离并维护主部门")
	void transferSetsStatusTransferred() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		UserDeptEntity existing = relation(RELATION_ID, UserOrgRelationStatusEnum.NORMAL.getCode());
		when(userDeptMapper.selectByIdAndUserId(RELATION_ID, USER_ID)).thenReturn(existing);

		sysUserDeptService.transfer(USER_ID, RELATION_ID);

		ArgumentCaptor<UserDeptEntity> captor = ArgumentCaptor.forClass(UserDeptEntity.class);
		verify(userDeptMapper).updateById(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(UserOrgRelationStatusEnum.TRANSFERRED.getCode());
		assertThat(captor.getValue().getIsPrimary()).isFalse();
		verify(userDeptPrimaryCoordinator).ensurePrimaryInvariant(USER_ID);
	}

	@Test
	@DisplayName("更新：受保护用户不允许修改")
	void updateThrowsWhenUserNotOperable() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		doThrow(new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, USER_ID))
			.when(userOperationChecker)
			.requireOperable(List.of(USER_ID));

		UserDeptUpdateForm form = new UserDeptUpdateForm();
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());
		form.setVersion(0L);

		assertThatThrownBy(() -> sysUserDeptService.update(USER_ID, RELATION_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
	}

	private static UserEntity activeUser(Long id) {
		UserEntity entity = new UserEntity();
		entity.setId(id);
		entity.setUsername("tester");
		return entity;
	}

	private static SysDeptEntity activeDept(Long id) {
		SysDeptEntity dept = new SysDeptEntity();
		dept.setId(id);
		dept.setStatus(true);
		return dept;
	}

	private static UserDeptEntity relation(Long id, int status) {
		UserDeptEntity entity = new UserDeptEntity();
		entity.setId(id);
		entity.setUserId(USER_ID);
		entity.setDeptId(DEPT_ID);
		entity.setStatus(status);
		return entity;
	}

}
