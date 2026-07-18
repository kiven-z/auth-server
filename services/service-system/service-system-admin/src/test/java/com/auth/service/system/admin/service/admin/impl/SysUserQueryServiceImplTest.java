package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesSnapshot;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysUserQueryServiceImpl 用户只读查询")
@ExtendWith(MockitoExtension.class)
class SysUserQueryServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private UserEffectiveCodesResolver userEffectiveCodesResolver;

	private SysUserQueryServiceImpl sysUserQueryService;

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserQueryService = new SysUserQueryServiceImpl(auditUserDisplayService, sysRoleMapper, sysPermissionMapper,
				userReferenceChecker, userEffectiveCodesResolver);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserQueryService, sysUserMapper);
	}

	@Test
	@DisplayName("分页查询：调用审计用户展示填充")
	void getPageInvokesAuditEnrichment() {
		Page<UserEntity> pageParams = new Page<>(1, 10);
		SysUserPageQuery query = new SysUserPageQuery();
		Page<SysUserPageRowPO> emptyPage = new Page<>(1, 10);
		when(sysUserMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(emptyPage);

		PageResponse<SysUserPageVO> result = sysUserQueryService.getPage(pageParams, query);

		assertThat(result.getList()).isEmpty();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("userId 为空时抛出 PARAM_REQUIRED")
	void getProfileRejectsNullUserId() {
		assertThatThrownBy(() -> sysUserQueryService.getProfile(null)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.PARAM_REQUIRED);
	}

	@Test
	@DisplayName("用户不存在时抛出 USER_NOT_FOUND")
	void getProfileRejectsMissingUser() {
		when(sysUserMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysUserQueryService.getProfile(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("正常查询时聚合用户、部门与岗位档案")
	void getProfileReturnsAggregatedData() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("alice");
		user.setNickname("Alice");
		user.setEmail("a@example.com");
		user.setPhone("13800000000");
		user.setAvatar("https://example.com/a.png");
		user.setStatus(0);
		user.setGender(1);
		user.setBirthday(LocalDate.of(1990, 1, 2));
		user.setIntroduction("bio");

		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserDeptProfilePO deptRow = new UserDeptProfilePO();
		deptRow.setId(10L);
		deptRow.setDeptName("研发部");
		when(sysUserMapper.selectDeptProfileByUserId(1L)).thenReturn(List.of(deptRow));

		UserPostProfilePO postRow = new UserPostProfilePO();
		postRow.setId(20L);
		postRow.setPostName("工程师");
		when(sysUserMapper.selectPostProfileByUserId(1L)).thenReturn(List.of(postRow));

		SysUserProfileVO profile = sysUserQueryService.getProfile(1L);

		assertThat(profile.getUsername()).isEqualTo("alice");
		assertThat(profile.getDeptList()).hasSize(1);
		assertThat(profile.getDeptList().get(0).getDeptName()).isEqualTo("研发部");
		assertThat(profile.getPostList()).hasSize(1);
		assertThat(profile.getPostList().get(0).getPostName()).isEqualTo("工程师");
		verify(sysUserMapper).selectDeptProfileByUserId(1L);
		verify(sysUserMapper).selectPostProfileByUserId(1L);
	}

	@Test
	@DisplayName("用户详情：聚合档案、组织绑定与生效授权")
	void getDetailReturnsAggregatedData() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("alice");
		user.setNickname("Alice");
		user.setEmail("a@example.com");
		user.setPhone("13800000000");
		user.setEmployeeNo("E001");
		user.setAvatar("https://example.com/a.png");
		user.setStatus(1);
		user.setGender(1);
		user.setBirthday(LocalDate.of(1990, 1, 2));
		user.setIntroduction("bio");
		user.setRemark("note");

		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserDeptProfilePO deptRow = new UserDeptProfilePO();
		deptRow.setId(10L);
		deptRow.setDeptName("研发部");
		when(sysUserMapper.selectDeptProfileByUserId(1L)).thenReturn(List.of(deptRow));

		UserPostProfilePO postRow = new UserPostProfilePO();
		postRow.setId(20L);
		postRow.setPostName("工程师");
		when(sysUserMapper.selectPostProfileByUserId(1L)).thenReturn(List.of(postRow));

		UserEffectiveCodesSnapshot codes = UserEffectiveCodesSnapshot.builder()
			.roleCodes(List.of("ADMIN"))
			.permissionCodes(List.of("sys:user:query"))
			.build();
		when(userEffectiveCodesResolver.resolve(1L)).thenReturn(Optional.of(codes));

		RoleReferencePO roleRow = new RoleReferencePO();
		roleRow.setId(100L);
		roleRow.setRoleCode("ADMIN");
		roleRow.setRoleName("管理员");
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("ADMIN"))).thenReturn(List.of(roleRow));

		PermissionReferencePO permissionRow = new PermissionReferencePO();
		permissionRow.setId(200L);
		permissionRow.setPermissionCode("sys:user:query");
		permissionRow.setPermissionName("用户查询");
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:user:query")))
			.thenReturn(List.of(permissionRow));

		SysUserDetailVO detail = sysUserQueryService.getDetail(1L);

		assertThat(detail.getUsername()).isEqualTo("alice");
		assertThat(detail.getEmployeeNo()).isEqualTo("E001");
		assertThat(detail.getRemark()).isEqualTo("note");
		assertThat(detail.getDeptList()).hasSize(1);
		assertThat(detail.getDeptList().get(0).getDeptName()).isEqualTo("研发部");
		assertThat(detail.getPostList()).hasSize(1);
		assertThat(detail.getPostList().get(0).getPostName()).isEqualTo("工程师");
		assertThat(detail.getEffectiveRoles()).hasSize(1);
		assertThat(detail.getEffectiveRoles().get(0).getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getEffectivePermissions()).hasSize(1);
		assertThat(detail.getEffectivePermissions().get(0).getPermissionCode()).isEqualTo("sys:user:query");
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("用户详情：授权服务不可用时返回空生效列表")
	void getDetailReturnsEmptyEffectiveWhenAuthUnavailable() {
		UserEntity user = new UserEntity();
		user.setId(2L);
		user.setUsername("bob");
		when(sysUserMapper.selectById(2L)).thenReturn(user);
		when(sysUserMapper.selectDeptProfileByUserId(2L)).thenReturn(List.of());
		when(sysUserMapper.selectPostProfileByUserId(2L)).thenReturn(List.of());
		when(userEffectiveCodesResolver.resolve(2L)).thenReturn(Optional.empty());

		SysUserDetailVO detail = sysUserQueryService.getDetail(2L);

		assertThat(detail.getUsername()).isEqualTo("bob");
		assertThat(detail.getDeptList()).isEmpty();
		assertThat(detail.getPostList()).isEmpty();
		assertThat(detail.getEffectiveRoles()).isEmpty();
		assertThat(detail.getEffectivePermissions()).isEmpty();
		verify(sysRoleMapper, never()).selectReferenceByRoleCodes(any());
		verify(sysPermissionMapper, never()).selectReferenceByPermissionCodes(any());
	}

	@Test
	@DisplayName("关键词为 null 时不查库并返回空列表")
	void searchReturnsEmptyWhenKeywordNull() {
		assertThat(sysUserQueryService.searchByKeyword(null, 10)).isEmpty();
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("关键词为空白时不查库并返回空列表")
	void searchReturnsEmptyWhenKeywordBlank() {
		assertThat(sysUserQueryService.searchByKeyword("   ", 10)).isEmpty();
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("limit 为 null 时默认使用 20")
	void searchUsesDefaultLimitWhenNull() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("alice", null);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("alice"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(20);
	}

	@Test
	@DisplayName("limit 超过 50 时封顶为 50")
	void searchCapsLimitAtFifty() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("bob", 200);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("bob"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(50);
	}

	@Test
	@DisplayName("limit 小于 1 时提升为 1")
	void searchRaisesTinyLimitToOne() {
		when(sysUserMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("c", 0);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserMapper).searchByKeyword(eq("c"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(1);
	}

	@Test
	@DisplayName("正常搜索时返回 Mapper 结果")
	void searchReturnsMapperRows() {
		UserSearchItemPO row = new UserSearchItemPO();
		row.setId(1L);
		row.setUsername("u1");
		when(sysUserMapper.searchByKeyword("kw", 15)).thenReturn(List.of(row));

		List<SysUserSearchItemVO> result = sysUserQueryService.searchByKeyword("kw", 15);

		assertThat(result).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(1L);
			assertThat(r.getUsername()).isEqualTo("u1");
		});
	}

}
