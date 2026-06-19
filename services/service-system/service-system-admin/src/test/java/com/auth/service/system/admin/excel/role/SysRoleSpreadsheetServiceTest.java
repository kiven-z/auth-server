package com.auth.service.system.admin.excel.role;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
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
 * {@link SysRoleSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysRoleSpreadsheetService 角色导出")
@ExtendWith(MockitoExtension.class)
class SysRoleSpreadsheetServiceTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	private SysRoleSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields baseScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysRoleSpreadsheetService(sysRoleMapper);
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

}
