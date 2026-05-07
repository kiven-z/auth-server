package com.auth.service.system.admin.user.service.impl;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserProfileMapper;
import com.auth.service.system.admin.service.impl.SysUserProfileServiceImpl;
import com.auth.service.system.admin.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.admin.vo.SysUserPostProfileItemResponse;
import com.auth.service.system.admin.vo.SysUserProfileResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserProfileServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserProfileServiceImpl 用户档案")
@ExtendWith(MockitoExtension.class)
class SysUserProfileServiceImplTest {

	@Mock
	private UserMapper userMapper;

	@Mock
	private SysUserProfileMapper sysUserProfileMapper;

	private SysUserProfileServiceImpl sysUserProfileService;

	@BeforeEach
	void setUp() throws Exception {
		sysUserProfileService = new SysUserProfileServiceImpl(sysUserProfileMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserProfileService, userMapper);
	}

	@Test
	@DisplayName("userId 为空时抛出 ILLEGAL_USER_ID_NOT_NULL")
	void getProfileRejectsNullUserId() {
		assertThatThrownBy(() -> sysUserProfileService.getProfile(null)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.ILLEGAL_USER_ID_NOT_NULL);
	}

	@Test
	@DisplayName("用户不存在时抛出 USER_NOT_FOUND")
	void getProfileRejectsMissingUser() {
		when(userMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysUserProfileService.getProfile(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemResultCodeEnum.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("存在用户时组装基本信息并填充部门与岗位列表")
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
		user.setLastLoginIp("127.0.0.1");
		user.setLastLoginRegion("local");
		user.setLastLoginTime(null);

		when(userMapper.selectById(1L)).thenReturn(user);

		SysUserDeptProfileItemResponse deptRow = new SysUserDeptProfileItemResponse();
		deptRow.setDeptId(10L);
		deptRow.setDeptName("研发部");
		when(sysUserProfileMapper.selectDeptProfileByUserId(1L)).thenReturn(List.of(deptRow));

		SysUserPostProfileItemResponse postRow = new SysUserPostProfileItemResponse();
		postRow.setId(100L);
		postRow.setPostId(20L);
		postRow.setPostName("工程师");
		when(sysUserProfileMapper.selectPostProfileByUserId(1L)).thenReturn(List.of(postRow));

		SysUserProfileResponse profile = sysUserProfileService.getProfile(1L);

		assertThat(profile.getUsername()).isEqualTo("alice");
		assertThat(profile.getDeptList()).hasSize(1);
		assertThat(profile.getDeptList().get(0).getDeptName()).isEqualTo("研发部");
		assertThat(profile.getPostList()).hasSize(1);
		assertThat(profile.getPostList().get(0).getPostName()).isEqualTo("工程师");
		verify(sysUserProfileMapper).selectDeptProfileByUserId(1L);
		verify(sysUserProfileMapper).selectPostProfileByUserId(1L);
	}

	@Test
	@DisplayName("Mapper 返回 null 列表时转为空集合")
	void getProfileTreatsNullListsAsEmpty() {
		UserEntity user = new UserEntity();
		user.setId(2L);
		user.setUsername("bob");
		when(userMapper.selectById(2L)).thenReturn(user);
		when(sysUserProfileMapper.selectDeptProfileByUserId(2L)).thenReturn(null);
		when(sysUserProfileMapper.selectPostProfileByUserId(2L)).thenReturn(null);

		SysUserProfileResponse profile = sysUserProfileService.getProfile(2L);

		assertThat(profile.getDeptList()).isEmpty();
		assertThat(profile.getPostList()).isEmpty();
	}

}
