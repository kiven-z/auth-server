package com.auth.service.auth.infrastructure.authorization;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.model.dto.inner.GrantSubjectRequest;

/**
 * AuthorizationImpactQueryService 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationImpactQueryService 影响面查询")
class AuthorizationImpactQueryServiceTest {

	@Mock
	private AuthorizationImpactMapper authorizationImpactMapper;

	@InjectMocks
	private AuthorizationImpactQueryService authorizationImpactQueryService;

	@Test
	@DisplayName("角色编码为空时不调 Mapper")
	void listUserIdsByRoleCode_blank_returnsEmpty() {
		assertThat(authorizationImpactQueryService.listUserIdsByRoleCode(null)).isEmpty();
		assertThat(authorizationImpactQueryService.listUserIdsByRoleCode("  ")).isEmpty();
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("角色编码列表为空时不调 Mapper")
	void listUserIdsByRoleCodes_empty_returnsEmpty() {
		assertThat(authorizationImpactQueryService.listUserIdsByRoleCodes(null)).isEmpty();
		assertThat(authorizationImpactQueryService.listUserIdsByRoleCodes(List.of())).isEmpty();
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("grant 主体列表为空时返回空")
	void listUserIdsByGrantSubjects_empty_returnsEmpty() {
		assertThat(authorizationImpactQueryService.listUserIdsByGrantSubjects(null)).isEmpty();
		verifyNoInteractions(authorizationImpactMapper);
	}

	@Test
	@DisplayName("grant 主体：USER 直接进集合，DEPT/POST 走 Mapper 合并去重")
	void listUserIdsByGrantSubjects_mergesUserDeptPost() {
		GrantSubjectRequest u = new GrantSubjectRequest();
		u.setSubjectType("USER");
		u.setSubjectId(1L);
		GrantSubjectRequest d = new GrantSubjectRequest();
		d.setSubjectType("DEPT");
		d.setSubjectId(10L);
		GrantSubjectRequest p = new GrantSubjectRequest();
		p.setSubjectType("POST");
		p.setSubjectId(20L);

		when(authorizationImpactMapper.selectUserIdsByDeptIds(List.of(10L))).thenReturn(List.of(100L));
		when(authorizationImpactMapper.selectUserIdsByPostIds(List.of(20L))).thenReturn(List.of(100L, 200L));

		List<Long> result = authorizationImpactQueryService.listUserIdsByGrantSubjects(List.of(u, d, p));

		assertThat(result).containsExactlyInAnyOrder(1L, 100L, 200L);
	}

	@Test
	@DisplayName("权限码列表委托 Mapper 并去重 null")
	void listUserIdsByPermissionCodes_delegates() {
		when(authorizationImpactMapper.selectUserIdsByPermissionCodes(List.of("a", "b")))
			.thenReturn(Arrays.asList(1L, null, 1L));

		List<Long> result = authorizationImpactQueryService.listUserIdsByPermissionCodes(List.of("a", "b"));

		assertThat(result).containsExactly(1L);
	}

	@Test
	@DisplayName("单角色编码委托 selectUserIdsByRoleCode")
	void listUserIdsByRoleCode_delegates() {
		when(authorizationImpactMapper.selectUserIdsByRoleCode("ADMIN")).thenReturn(List.of(7L));

		assertThat(authorizationImpactQueryService.listUserIdsByRoleCode("ADMIN")).containsExactly(7L);
	}

	@Test
	@DisplayName("批量角色编码过滤空白后委托 Mapper")
	void listUserIdsByRoleCodes_filtersBlank() {
		when(authorizationImpactMapper.selectUserIdsByRoleCodes(List.of("R1"))).thenReturn(List.of(9L));

		List<Long> result = authorizationImpactQueryService.listUserIdsByRoleCodes(List.of("  ", "R1", ""));

		assertThat(result).containsExactly(9L);
	}

}
