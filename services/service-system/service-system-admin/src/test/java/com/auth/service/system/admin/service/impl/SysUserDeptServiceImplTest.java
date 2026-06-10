package com.auth.service.system.admin.service.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.admin.support.userdept.UserDeptPrimaryCoordinator;
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
import static org.mockito.Mockito.*;

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
	private AuditUserDisplayService auditUserDisplayService;

	private SysUserDeptServiceImpl sysUserDeptService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserDeptServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	private static SysDeptEntity activeDept() {
		SysDeptEntity dept = new SysDeptEntity();
		dept.setId(SysUserDeptServiceImplTest.DEPT_ID);
		dept.setStatus(true);
		return dept;
	}

	private static UserDeptEntity relation(int status) {
		UserDeptEntity entity = new UserDeptEntity();
		entity.setId(SysUserDeptServiceImplTest.RELATION_ID);
		entity.setUserId(USER_ID);
		entity.setDeptId(DEPT_ID);
		entity.setStatus(status);
		return entity;
	}

	@BeforeEach
	void setUp() {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserDeptService = new SysUserDeptServiceImpl(userReferenceChecker, userOperationChecker,
				deptReferenceChecker, userDeptMapper, userDeptPrimaryCoordinator, userAuthorizationInvalidationTrigger,
				auditUserDisplayService);
	}

	@Test
	@DisplayName("新增：写入关联并触发用户授权失效")
	void createInsertsRelationAndSubmitsInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(0);
		when(userDeptMapper.selectActiveByUserId(USER_ID)).thenReturn(List.of());
		when(deptReferenceChecker.getExistingActive(DEPT_ID)).thenReturn(activeDept());

		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setUserId(USER_ID);
		form.setDeptId(DEPT_ID);
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());

		sysUserDeptService.create(form);

		verify(userOperationChecker).requireOperable(List.of(USER_ID));
		ArgumentCaptor<UserDeptEntity> captor = ArgumentCaptor.forClass(UserDeptEntity.class);
		verify(userDeptMapper).insert(captor.capture());
		assertThat(captor.getValue().getDeptId()).isEqualTo(DEPT_ID);
		assertThat(captor.getValue().getIsPrimary()).isTrue();
		verify(userDeptPrimaryCoordinator).ensurePrimaryInvariant(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "create-dept");
	}

	@Test
	@DisplayName("新增：重复部门抛出 USER_DEPT_DUPLICATE")
	void createThrowsWhenDuplicateDept() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		when(userDeptMapper.countByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(1);
		when(deptReferenceChecker.getExistingActive(DEPT_ID)).thenReturn(activeDept());

		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setUserId(USER_ID);
		form.setDeptId(DEPT_ID);
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());

		assertThatThrownBy(() -> sysUserDeptService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_DUPLICATE);

		verify(userDeptMapper, never()).insert(any(UserDeptEntity.class));
	}

	@Test
	@DisplayName("设为主部门：仅正常状态允许")
	void setPrimaryThrowsWhenStatusNotNormal() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptEntity existing = relation(UserOrgRelationStatusEnum.PART_TIME.getCode());
		when(userDeptMapper.selectByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(existing);

		assertThatThrownBy(() -> sysUserDeptService.setPrimary(USER_ID, DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_DEPT_PRIMARY_NOT_ELIGIBLE);
	}

	@Test
	@DisplayName("调离：状态置为已调离并维护主部门")
	void transferSetsStatusTransferred() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptEntity existing = relation(UserOrgRelationStatusEnum.NORMAL.getCode());
		when(userDeptMapper.selectByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(existing);

		sysUserDeptService.transfer(USER_ID, DEPT_ID);

		ArgumentCaptor<UserDeptEntity> captor = ArgumentCaptor.forClass(UserDeptEntity.class);
		verify(userDeptMapper).updateById(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(UserOrgRelationStatusEnum.TRANSFERRED.getCode());
		assertThat(captor.getValue().getIsPrimary()).isFalse();
		verify(userDeptPrimaryCoordinator).ensurePrimaryInvariant(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "transfer-dept");
	}

	@Test
	@DisplayName("删除：按部门 ID 删除并仅触发当前用户授权失效")
	void deleteByDeptIdsDeletesRelationAndSubmitsUserInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserDeptEntity existing = relation(UserOrgRelationStatusEnum.NORMAL.getCode());
		when(userDeptMapper.selectByUserIdAndDeptId(USER_ID, DEPT_ID)).thenReturn(existing);

		sysUserDeptService.deleteByDeptIds(USER_ID, List.of(DEPT_ID));

		verify(userDeptMapper).deleteById(RELATION_ID);
		verify(userDeptPrimaryCoordinator).ensurePrimaryInvariant(USER_ID);
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "delete-dept");
	}

	@Test
	@DisplayName("更新：受保护用户不允许修改")
	void updateThrowsWhenUserNotOperable() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		doThrow(new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, USER_ID))
			.when(userOperationChecker)
			.requireOperable(List.of(USER_ID));

		UserDeptAssignForm form = new UserDeptAssignForm();
		form.setUserId(USER_ID);
		form.setDeptId(DEPT_ID);
		form.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());

		assertThatThrownBy(() -> sysUserDeptService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
	}

}
