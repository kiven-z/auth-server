package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostExportQuery;
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
 * {@link SysPostExportServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPostExportServiceImpl 岗位导出")
@ExtendWith(MockitoExtension.class)
class SysPostExportServiceImplTest {

	@Mock
	private SysPostMapper sysPostMapper;

	private SysPostExportServiceImpl exportService;

	private static SysPostExportQuery baseQuery(int fromPage, int toPage) {
		SysPostExportQuery query = new SysPostExportQuery();
		query.setFromPage(fromPage);
		query.setToPage(toPage);
		return query;
	}

	@BeforeEach
	void setUp() {
		exportService = new SysPostExportServiceImpl(sysPostMapper);
	}

	@Test
	@DisplayName("结束页小于起始页时抛出页码区间异常")
	void export_whenToPageBeforeFromPage_throwsPageRangeInvalid() {
		SysPostExportQuery query = baseQuery(2, 1);

		assertThatThrownBy(() -> exportService.export(query)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.EXPORT_PAGE_RANGE_INVALID);
	}

	@Test
	@DisplayName("跨页行数超过上限时抛出行数限制异常")
	void export_whenRowSpanExceedsLimit_throwsRowLimitExceeded() {
		SysPostExportQuery query = baseQuery(1, 50);
		query.setPageSize(30);

		assertThatThrownBy(() -> exportService.export(query)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.EXPORT_ROW_LIMIT_EXCEEDED);
	}

	@Test
	@DisplayName("单页导出成功返回非空 Excel 字节")
	void export_whenSinglePage_returnsExcelBytes() throws IOException {
		SysPostExportQuery query = baseQuery(1, 1);
		query.setPageSize(10);

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
		when(sysPostMapper.selectListByPage(any(Page.class), eq(query), anyString())).thenReturn(page);

		ResponseEntity<byte[]> response = exportService.export(query);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		verify(sysPostMapper).selectListByPage(any(Page.class), eq(query), anyString());
	}

}
