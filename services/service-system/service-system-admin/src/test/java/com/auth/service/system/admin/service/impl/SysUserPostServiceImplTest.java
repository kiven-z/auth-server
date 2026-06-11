package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.mapper.UserPostMapper;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.po.userpost.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
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
 * {@link SysUserPostServiceImpl} 单元测试
 */
@DisplayName("SysUserPostServiceImpl 用户岗位关联")
@ExtendWith(MockitoExtension.class)
class SysUserPostServiceImplTest {

	private static final Long USER_ID = 10L;

	private static final Long RELATION_ID = 100L;

	private static final Long POST_ID = 30L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserPostMapper userPostMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysUserPostServiceImpl sysUserPostService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserPostServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserPostService = new SysUserPostServiceImpl(userReferenceChecker, auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserPostService, userPostMapper);
	}

	@Test
	@DisplayName("分页查询：映射业务字段并补充审计用户名")
	void getPageMapsRowsAndEnrichesAuditUsernames() {
		// 校验用户存在
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		UserPostPageQuery query = new UserPostPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		LocalDateTime now = LocalDateTime.of(2026, 6, 11, 10, 0, 0);
		UserPostPageRowPO row = new UserPostPageRowPO();
		row.setId(RELATION_ID);
		row.setUserId(USER_ID);
		row.setPostId(POST_ID);
		row.setPostCode("DEV");
		row.setPostName("开发工程师");
		row.setIsPrimary(true);
		row.setStatus(UserOrgRelationStatusEnum.NORMAL.getCode());
		row.setRemark("核心岗位");
		row.setCreateTime(now);
		row.setUpdateTime(now);
		row.setCreateUser(1L);
		row.setUpdateUser(1L);
		Page<UserPostPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(row));
		poPage.setTotal(1);
		when(userPostMapper.selectListByPage(any(Page.class), eq(USER_ID), eq(query))).thenReturn(poPage);

		PageResponse<UserPostPageVO> result = sysUserPostService.getPage(USER_ID, query);

		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getList()).hasSize(1);
		UserPostPageVO vo = result.getList().get(0);
		assertThat(vo.getId()).isEqualTo(RELATION_ID);
		assertThat(vo.getPostCode()).isEqualTo("DEV");
		assertThat(vo.getPostName()).isEqualTo("开发工程师");
		assertThat(vo.getIsPrimary()).isTrue();
		assertThat(vo.getStatus()).isEqualTo(UserOrgRelationStatusEnum.NORMAL.getCode());
		assertThat(vo.getRemark()).isEqualTo("核心岗位");
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

}
