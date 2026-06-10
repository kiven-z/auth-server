package com.auth.service.system.admin.service.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.mapper.SysUserQueryMapper;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.admin.model.SysUserSearchItemResponse;
import com.auth.service.system.admin.model.po.sysuser.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.sysuser.UserPostProfilePO;
import com.auth.service.system.admin.model.po.sysuser.UserSearchItemPO;
import com.auth.service.system.admin.model.vo.sysuser.SysUserProfileResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
	private SysUserQueryMapper sysUserQueryMapper;

	@Mock
	private SysUserMapper sysUserMapper;

	private SysUserQueryServiceImpl sysUserQueryService;

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserQueryService = new SysUserQueryServiceImpl(sysUserQueryMapper, userReferenceChecker);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserQueryService, sysUserQueryMapper);
	}

	@Test
	@DisplayName("userId 为空时抛出 ILLEGAL_USER_ID_NOT_NULL")
	void getProfileRejectsNullUserId() {
		assertThatThrownBy(() -> sysUserQueryService.getProfile(null)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.ILLEGAL_USER_ID_NOT_NULL);
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
		deptRow.setDeptId(10L);
		deptRow.setDeptName("研发部");
		when(sysUserQueryMapper.selectDeptProfileByUserId(1L)).thenReturn(List.of(deptRow));

		UserPostProfilePO postRow = new UserPostProfilePO();
		postRow.setId(100L);
		postRow.setPostId(20L);
		postRow.setPostName("工程师");
		when(sysUserQueryMapper.selectPostProfileByUserId(1L)).thenReturn(List.of(postRow));

		SysUserProfileResponse profile = sysUserQueryService.getProfile(1L);

		assertThat(profile.getUsername()).isEqualTo("alice");
		assertThat(profile.getDeptList()).hasSize(1);
		assertThat(profile.getDeptList().get(0).getDeptName()).isEqualTo("研发部");
		assertThat(profile.getPostList()).hasSize(1);
		assertThat(profile.getPostList().get(0).getPostName()).isEqualTo("工程师");
		verify(sysUserQueryMapper).selectDeptProfileByUserId(1L);
		verify(sysUserQueryMapper).selectPostProfileByUserId(1L);
	}

	@Test
	@DisplayName("关键词为 null 时不查库并返回空列表")
	void searchReturnsEmptyWhenKeywordNull() {
		assertThat(sysUserQueryService.searchByKeyword(null, 10)).isEmpty();
		verifyNoInteractions(sysUserQueryMapper);
	}

	@Test
	@DisplayName("关键词为空白时不查库并返回空列表")
	void searchReturnsEmptyWhenKeywordBlank() {
		assertThat(sysUserQueryService.searchByKeyword("   ", 10)).isEmpty();
		verifyNoInteractions(sysUserQueryMapper);
	}

	@Test
	@DisplayName("limit 为 null 时默认使用 20")
	void searchUsesDefaultLimitWhenNull() {
		when(sysUserQueryMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("alice", null);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserQueryMapper).searchByKeyword(eq("alice"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(20);
	}

	@Test
	@DisplayName("limit 超过 50 时封顶为 50")
	void searchCapsLimitAtFifty() {
		when(sysUserQueryMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("bob", 200);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserQueryMapper).searchByKeyword(eq("bob"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(50);
	}

	@Test
	@DisplayName("limit 小于 1 时提升为 1")
	void searchRaisesTinyLimitToOne() {
		when(sysUserQueryMapper.searchByKeyword(anyString(), anyInt())).thenReturn(List.of());

		sysUserQueryService.searchByKeyword("c", 0);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysUserQueryMapper).searchByKeyword(eq("c"), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(1);
	}

	@Test
	@DisplayName("正常搜索时返回 Mapper 结果")
	void searchReturnsMapperRows() {
		UserSearchItemPO row = new UserSearchItemPO();
		row.setId(1L);
		row.setUsername("u1");
		when(sysUserQueryMapper.searchByKeyword("kw", 15)).thenReturn(List.of(row));

		List<SysUserSearchItemResponse> result = sysUserQueryService.searchByKeyword("kw", 15);

		assertThat(result).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(1L);
			assertThat(r.getUsername()).isEqualTo("u1");
		});
	}

}
