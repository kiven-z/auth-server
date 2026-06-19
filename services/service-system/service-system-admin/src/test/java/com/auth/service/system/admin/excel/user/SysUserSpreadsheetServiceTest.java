package com.auth.service.system.admin.excel.user;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
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
 * {@link SysUserSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserSpreadsheetService 用户导出")
@ExtendWith(MockitoExtension.class)
class SysUserSpreadsheetServiceTest {

	@Mock
	private SysUserMapper sysUserMapper;

	private SysUserSpreadsheetService spreadsheetService;

	private static ExportScopeQueryFields baseScope() {
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		scope.setFromPage(1);
		scope.setToPage(1);
		return scope;
	}

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysUserSpreadsheetService(sysUserMapper);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出筛选与范围
		SysUserPageQuery filter = new SysUserPageQuery();
		filter.setPageSize(10);
		ExportScopeQueryFields scope = baseScope();

		SysUserPageRowPO row = new SysUserPageRowPO();
		row.setId(1L);
		row.setUsername("admin");
		row.setNickname("管理员");
		row.setPhone("13800000000");
		row.setEmail("admin@example.com");
		row.setEmployeeNo("E001");
		row.setStatus(1);

		IPage<SysUserPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(sysUserMapper.selectListByPage(any(Page.class), eq(filter), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = spreadsheetService.export(filter, scope);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysUserMapper).selectListByPage(any(Page.class), eq(filter), anyString());
	}

}
