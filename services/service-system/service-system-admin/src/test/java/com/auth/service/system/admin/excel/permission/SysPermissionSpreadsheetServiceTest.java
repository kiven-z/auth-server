package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.service.admin.SysPermissionService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysPermissionSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPermissionSpreadsheetService 权限导出")
@ExtendWith(MockitoExtension.class)
class SysPermissionSpreadsheetServiceTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private SysPermissionService sysPermissionService;

	private SysPermissionSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields baseScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		PermissionSheetImporter permissionSheetImporter = new PermissionSheetImporter(sysPermissionMapper,
				sysPermissionService);
		spreadsheetService = new SysPermissionSpreadsheetService(sysPermissionMapper, permissionSheetImporter);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysPermissionQuery filter = new SysPermissionQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = baseScope();

		SysPermissionEntity row = new SysPermissionEntity();
		row.setId(1L);
		row.setPermissionCode("sys:permission:query");
		row.setPermissionName("权限查询");
		row.setStatus(true);
		row.setOrderNum(1);

		IPage<SysPermissionEntity> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysPermissionMapper.selectListByPage(any(Page.class), eq(filter), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysPermissionMapper).selectListByPage(any(Page.class), eq(filter), anyString());
	}

	@Test
	@DisplayName("导入模板下载返回非空 Excel 字节")
	void downloadImportTemplate_returnsNonEmptyExcelBytes() throws IOException {
		ResponseEntity<byte[]> response = spreadsheetService.downloadImportTemplate();

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
	}

}
