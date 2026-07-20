package com.auth.service.system.admin.excel.role;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.service.admin.SysRoleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysRoleSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysRoleSpreadsheetService 角色导出")
@ExtendWith(MockitoExtension.class)
class SysRoleSpreadsheetServiceTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private SysRoleService sysRoleService;

	private SysRoleSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields baseScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		RoleSheetImporter roleSheetImporter = new RoleSheetImporter(sysRoleMapper, sysRoleService);
		spreadsheetService = new SysRoleSpreadsheetService(sysRoleMapper, roleSheetImporter);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysRoleQuery filter = new SysRoleQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = baseScope();

		SysRoleEntity row = new SysRoleEntity();
		row.setId(1L);
		row.setRoleCode("ADMIN");
		row.setRoleName("管理员");
		row.setStatus(true);
		row.setOrderNum(1);
		row.setRemark("备注样例");

		IPage<SysRoleEntity> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysRoleMapper.selectListByPage(any(Page.class), eq(filter), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysRoleMapper).selectListByPage(any(Page.class), eq(filter), anyString());
	}

	@Test
	@DisplayName("导入模板下载返回非空 Excel 且表头与示例行正确")
	void downloadImportTemplate_returnsTemplateWithExpectedHeaders() throws IOException {
		ResponseEntity<byte[]> response = spreadsheetService.downloadImportTemplate();

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("role_import_template.xlsx");

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
			Sheet sheet = workbook.getSheetAt(0);
			assertThat(sheet.getSheetName()).isEqualTo("角色导入模板");
			Row header = sheet.getRow(0);
			assertThat(header.getCell(0).getStringCellValue()).isEqualTo("角色编码");
			assertThat(header.getCell(1).getStringCellValue()).isEqualTo("角色名称");
			assertThat(header.getCell(2).getStringCellValue()).isEqualTo("状态");
			assertThat(header.getCell(3).getStringCellValue()).isEqualTo("显示顺序");
			assertThat(header.getCell(4).getStringCellValue()).isEqualTo("备注");

			Row sample = sheet.getRow(1);
			assertThat(sample.getCell(0).getStringCellValue()).contains("ADMIN");
			assertThat(sample.getCell(1).getStringCellValue()).contains("系统管理员");
			assertThat(sample.getCell(2).getStringCellValue()).isEqualTo("启用");
			assertThat(sample.getCell(3).getNumericCellValue()).isEqualTo(1.0);
			assertThat(sample.getCell(4).getStringCellValue()).isEqualTo("（可选）");
		}
	}

}
