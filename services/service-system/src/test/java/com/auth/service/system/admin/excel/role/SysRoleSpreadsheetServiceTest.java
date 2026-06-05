package com.auth.service.system.admin.excel.role;

import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
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

	private static SysRoleExportQuery baseQuery() {
		SysRoleExportQuery query = new SysRoleExportQuery();
		query.setFromPage(1);
		query.setToPage(1);
		return query;
	}

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysRoleSpreadsheetService(sysRoleMapper);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		// 构造单页导出查询
		SysRoleExportQuery query = baseQuery();
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

		ResponseEntity<byte[]> response = spreadsheetService.export(query);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysRoleMapper).selectListByPage(any(Page.class), eq(query), anyString());
	}

}
