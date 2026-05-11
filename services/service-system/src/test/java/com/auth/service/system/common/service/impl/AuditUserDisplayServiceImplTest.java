package com.auth.service.system.common.service.impl;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuditUserDisplayServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("AuditUserDisplayServiceImpl 审计用户展示名")
@ExtendWith(MockitoExtension.class)
class AuditUserDisplayServiceImplTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@InjectMocks
	private AuditUserDisplayServiceImpl auditUserDisplayService;

	@Test
	@DisplayName("mapUsernamesByIds：空集合与 null 不访问数据库并返回空 Map")
	void mapUsernamesByIdsReturnsEmptyWithoutDbWhenNoIds() {
		// ── null ─────────────────────────────────────────────────
		assertThat(auditUserDisplayService.mapUsernamesByIds(null)).isEmpty();
		verifyNoInteractions(sysUserMapper);

		// ── empty collection ────────────────────────────────────
		assertThat(auditUserDisplayService.mapUsernamesByIds(Collections.emptyList())).isEmpty();
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("mapUsernamesByIds：过滤 null 主键并去重后批量查询")
	void mapUsernamesByIdsFiltersNullsAndDedupes() {
		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");

		when(sysUserMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		Map<Long, String> result = auditUserDisplayService.mapUsernamesByIds(Arrays.asList(1L, null, 1L, 2L));

		assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(1L, "alice", 2L, "bob"));
	}

	@Test
	@DisplayName("mapUsernamesByIds：全部为 null 主键时不访问数据库")
	void mapUsernamesByIdsAllNullIdsSkipsDb() {
		assertThat(auditUserDisplayService.mapUsernamesByIds(Arrays.asList(null, null))).isEmpty();
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("enrichAuditUsernames：为分页行填充创建人、更新人用户名")
	void enrichAuditUsernamesSetsUsernamesOnRecords() {
		BaseResponse row = new BaseResponse();
		row.setCreateUser(1L);
		row.setUpdateUser(2L);
		Page<BaseResponse> page = new Page<>();
		page.setRecords(List.of(row));

		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");
		when(sysUserMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		auditUserDisplayService.enrichAuditUsernames(page);

		assertThat(row.getCreateUsername()).isEqualTo("alice");
		assertThat(row.getUpdateUsername()).isEqualTo("bob");
	}

	@Test
	@DisplayName("enrichAuditUsernames(List)：非分页列表同样填充用户名")
	void enrichAuditUsernamesListSetsUsernamesOnRecords() {
		BaseResponse row = new BaseResponse();
		row.setCreateUser(1L);
		row.setUpdateUser(2L);
		List<BaseResponse> list = List.of(row);

		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");
		when(sysUserMapper.selectBatchIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		auditUserDisplayService.enrichAuditUsernames(list);

		assertThat(row.getCreateUsername()).isEqualTo("alice");
		assertThat(row.getUpdateUsername()).isEqualTo("bob");
	}

	@Test
	@DisplayName("enrichAuditUsernames(IPage)：page 为 null 时不访问数据库")
	void enrichAuditUsernamesPageNullSkipsDb() {
		auditUserDisplayService.enrichAuditUsernames((Page<BaseResponse>) null);
		verifyNoInteractions(sysUserMapper);
	}

	@Test
	@DisplayName("enrichAuditUsernames(IPage)：当前页无记录时不访问用户表")
	void enrichAuditUsernamesPageEmptyRecordsSkipsDb() {
		Page<BaseResponse> page = new Page<>();
		page.setRecords(Collections.emptyList());

		auditUserDisplayService.enrichAuditUsernames(page);

		verifyNoInteractions(sysUserMapper);
	}

}
