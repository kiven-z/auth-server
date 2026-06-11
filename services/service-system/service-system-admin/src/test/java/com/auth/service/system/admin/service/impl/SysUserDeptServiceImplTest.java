package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
	private UserDeptMapper userDeptMapper;

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

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserDeptService = new SysUserDeptServiceImpl(userReferenceChecker, userOperationChecker,
				userAuthorizationInvalidationTrigger, auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserDeptService, userDeptMapper);
	}

	@Test
	@DisplayName("分页查询：映射业务字段并补充审计用户名")
	void getPageMapsRowsAndEnrichesAuditUsernames() {
		// 准备分页查询条件与模拟数据
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

}
