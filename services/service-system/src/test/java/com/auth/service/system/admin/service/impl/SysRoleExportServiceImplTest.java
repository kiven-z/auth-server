package com.auth.service.system.admin.service.impl;

import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysRoleExportServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysRoleExportServiceImpl 角色导出")
@ExtendWith(MockitoExtension.class)
class SysRoleExportServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	private SysRoleExportServiceImpl exportService;

	private static SysRoleExportQuery baseQuery(int fromPage, int toPage) {
		SysRoleExportQuery query = new SysRoleExportQuery();
		query.setFromPage(fromPage);
		query.setToPage(toPage);
		return query;
	}

	@BeforeEach
	void setUp() {
		exportService = new SysRoleExportServiceImpl(sysRoleMapper);
	}

	@Test
	@DisplayName("结束页小于起始页时抛出页码区间异常")
	void export_whenToPageBeforeFromPage_throwsPageRangeInvalid() {
		SysRoleExportQuery query = baseQuery(2, 1);

		assertThatThrownBy(() -> exportService.export(query)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.EXPORT_PAGE_RANGE_INVALID);
	}

	@Test
	@DisplayName("跨页行数超过上限时抛出行数限制异常")
	void export_whenRowSpanExceedsLimit_throwsRowLimitExceeded() {
		SysRoleExportQuery query = baseQuery(1, 50);
		query.setPageSize(30);

		assertThatThrownBy(() -> exportService.export(query)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.EXPORT_ROW_LIMIT_EXCEEDED);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		SysRoleExportQuery query = baseQuery(1, 1);
		query.setPageSize(10);

		SysRoleEntity row = new SysRoleEntity();
		row.setId(1L);
		row.setRoleCode("ADMIN");
		row.setRoleName("管理员");
		row.setRoleType(1);
		row.setStatus(true);
		row.setOrderNum(1);
		row.setRemark("备注样例");

		IPage<SysRoleEntity> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysRoleMapper.selectListByPage(any(Page.class), eq(query), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = exportService.export(query);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysRoleMapper).selectListByPage(any(Page.class), eq(query), anyString());
	}

	@Test
	@DisplayName("FULL_ASYNC 导出抛出范围不支持异常")
	void export_whenFullAsync_throwsScopeNotSupported() {
		SysRoleExportQuery query = baseQuery(1, 1);
		query.setExportScope(ExportScope.FULL_ASYNC);

		assertThatThrownBy(() -> exportService.export(query)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.EXPORT_SCOPE_NOT_SUPPORTED);
	}

}
