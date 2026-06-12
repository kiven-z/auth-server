package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.SysDeptPageQuery;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptCodeIdResolver;
import com.auth.service.system.admin.support.dept.DeptPathResolver;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysDeptSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysDeptSpreadsheetService 部门导出")
@ExtendWith(MockitoExtension.class)
class SysDeptSpreadsheetServiceTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private DeptPathResolver deptPathResolver;

	@Mock
	private SysDeptService sysDeptService;

	@Mock
	private DeptCodeIdResolver deptCodeIdResolver;

	private DeptSheetImporter deptSheetImporter;

	private SysDeptSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields pageRangeScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		// 导出测试不触发导入逻辑，使用真实 importer 实例避免 mock 继承类失败
		deptSheetImporter = new DeptSheetImporter(sysDeptService, deptCodeIdResolver);
		spreadsheetService = new SysDeptSpreadsheetService(sysDeptMapper, deptPathResolver, deptSheetImporter);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysDeptPageQuery filter = new SysDeptPageQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = pageRangeScope();

		SysDeptPageRowPO row = new SysDeptPageRowPO();
		row.setId(2L);
		row.setParentId(1L);
		row.setParentDeptCode("ROOT");
		row.setParentDeptName("总部");
		row.setDeptCode("RD");
		row.setDeptName("研发部");
		row.setStatus(true);
		row.setOrderNum(1);
		row.setRemark("备注样例");

		IPage<SysDeptPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysDeptMapper.selectListByPage(any(Page.class), eq(filter))).thenReturn(page);
		when(deptPathResolver.resolve(List.of(2L))).thenReturn(Map.of(2L, "总部/研发部"));

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysDeptMapper).selectListByPage(any(Page.class), eq(filter));
	}

	@Test
	@DisplayName("导出行写入部门全路径列")
	void export_whenPathResolved_writesDeptPathColumn() throws IOException {
		SysDeptPageQuery filter = new SysDeptPageQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = pageRangeScope();

		SysDeptPageRowPO row = new SysDeptPageRowPO();
		row.setId(2L);
		row.setParentId(1L);
		row.setDeptCode("RD");
		row.setDeptName("研发部");
		row.setStatus(true);
		row.setOrderNum(1);

		IPage<SysDeptPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysDeptMapper.selectListByPage(any(Page.class), eq(filter))).thenReturn(page);
		when(deptPathResolver.resolve(List.of(2L))).thenReturn(Map.of(2L, "总部/研发部"));

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
			Sheet sheet = workbook.getSheetAt(0);
			Row header = sheet.getRow(0);
			Row data = sheet.getRow(1);
			assertThat(header.getCell(5).getStringCellValue()).isEqualTo("部门路径");
			assertThat(data.getCell(5).getStringCellValue()).isEqualTo("总部/研发部");
		}
	}

	@Test
	@DisplayName("CURRENT_PAGE 只需 filter.pageIndex，不需要 fromPage/toPage")
	void export_whenCurrentPage_usesFilterPageIndex() throws IOException {
		// CURRENT_PAGE 无需 fromPage/toPage
		SysDeptPageQuery filter = new SysDeptPageQuery();
		filter.setPageIndex(3);
		filter.setPageSize(10);
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setExportScope(ExportScope.CURRENT_PAGE);

		SysDeptPageRowPO row = new SysDeptPageRowPO();
		row.setId(1L);
		row.setParentId(0L);
		row.setDeptCode("ROOT");
		row.setDeptName("总部");
		row.setStatus(true);
		row.setOrderNum(1);

		IPage<SysDeptPageRowPO> page = new Page<>(3, 10, 1);
		page.setRecords(List.of(row));
		when(sysDeptMapper.selectListByPage(any(Page.class), eq(filter))).thenReturn(page);
		when(deptPathResolver.resolve(List.of(1L))).thenReturn(Map.of(1L, "总部"));

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysDeptMapper).selectListByPage(argThat(p -> p.getCurrent() == 3), eq(filter));
	}

}
