package com.auth.service.system.admin.excel.post;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
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
 * {@link SysPostSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPostSpreadsheetService 岗位导出")
@ExtendWith(MockitoExtension.class)
class SysPostSpreadsheetServiceTest {

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private PostSheetImporter postSheetImporter;

	private SysPostSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields pageRangeScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysPostSpreadsheetService(sysPostMapper, postSheetImporter);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysPostQuery filter = new SysPostQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = pageRangeScope();

		SysPostPageRowPO row = new SysPostPageRowPO();
		row.setId(1L);
		row.setDeptCode("D01");
		row.setDeptName("研发部");
		row.setPostCode("P01");
		row.setPostName("工程师");
		row.setStatus(true);
		row.setOrderNum(1);
		row.setRemark("备注样例");

		IPage<SysPostPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysPostMapper.selectListByPage(any(Page.class), eq(filter), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysPostMapper).selectListByPage(any(Page.class), eq(filter), anyString());
	}

	@Test
	@DisplayName("CURRENT_PAGE 只需 filter.pageIndex，不需要 fromPage/toPage")
	void export_whenCurrentPage_usesFilterPageIndex() throws IOException {
		// CURRENT_PAGE 无需 fromPage/toPage
		SysPostQuery filter = new SysPostQuery();
		filter.setPageIndex(3);
		filter.setPageSize(10);
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setExportScope(ExportScope.CURRENT_PAGE);

		SysPostPageRowPO row = new SysPostPageRowPO();
		row.setId(1L);
		row.setDeptCode("D01");
		row.setDeptName("研发部");
		row.setPostCode("P01");
		row.setPostName("工程师");
		row.setStatus(true);
		row.setOrderNum(1);

		IPage<SysPostPageRowPO> page = new Page<>(3, 10, 1);
		page.setRecords(List.of(row));
		when(sysPostMapper.selectListByPage(any(Page.class), eq(filter), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysPostMapper).selectListByPage(argThat(p -> p.getCurrent() == 3), eq(filter), anyString());
	}

}
