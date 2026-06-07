package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserServiceImpl 用户管理")
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysUserServiceImpl sysUserService;

	@BeforeEach
	void setUp() throws Exception {
		sysUserService = new SysUserServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysUserService, sysUserMapper);
	}

	@Test
	@DisplayName("分页查询：委托 Mapper 并补充审计用户名")
	void getPageDelegatesToMapperAndEnrichesAuditUsernames() {
		Page<UserEntity> pageParams = new Page<>(1, 10);
		SysUserPageQuery query = new SysUserPageQuery();
		query.setUsername("admin");
		Page<SysUserPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(new SysUserPageRowPO()));
		when(sysUserMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(poPage);

		PageResponse<SysUserPageVO> result = sysUserService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

}
