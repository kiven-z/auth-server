package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.SysDeptQuery;
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
 * {@link SysDeptSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysDeptSpreadsheetService 部门导出")
@ExtendWith(MockitoExtension.class)
class SysDeptSpreadsheetServiceTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	private SysDeptSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields baseScope(int toPage) {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(toPage);
		return scope;
	}

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysDeptSpreadsheetService(sysDeptMapper);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysDeptQuery filter = new SysDeptQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = baseScope(1);

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

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysDeptMapper).selectListByPage(any(Page.class), eq(filter));
	}

	@Test
	@DisplayName("CURRENT_PAGE 使用 filter 的 pageIndex 导出单页")
	void export_whenCurrentPage_usesFilterPageIndex() throws IOException {
		SysDeptQuery filter = new SysDeptQuery();
		filter.setPageIndex(3);
		filter.setPageSize(10);
		ExportScopeQueryFields scope = baseScope(99);
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

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysDeptMapper).selectListByPage(argThat(p -> p.getCurrent() == 3), eq(filter));
	}

}
