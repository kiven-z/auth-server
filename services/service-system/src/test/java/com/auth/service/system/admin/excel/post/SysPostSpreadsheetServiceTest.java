package com.auth.service.system.admin.excel.post;

import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostExportQuery;
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

    private SysPostSpreadsheetService spreadsheetService;

    private static SysPostExportQuery baseQuery(int toPage) {
        SysPostExportQuery query = new SysPostExportQuery();
        query.setFromPage(1);
        query.setToPage(toPage);
        return query;
    }

    @BeforeEach
    void setUp() {
        spreadsheetService = new SysPostSpreadsheetService(sysPostMapper);
    }

    @Test
    @DisplayName("单页导出成功返回非空 Excel 字节")
    void export_whenSinglePage_returnsExcelBytes() throws IOException {
        // 构造单页导出查询
        SysPostExportQuery query = baseQuery(1);
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

        ResponseEntity<byte[]> response = spreadsheetService.export(query);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull().isNotEmpty();
        verify(sysPostMapper).selectListByPage(any(Page.class), eq(query), anyString());
    }

    @Test
    @DisplayName("CURRENT_PAGE 使用 pageIndex 导出单页")
    void export_whenCurrentPage_usesPageIndex() throws IOException {
        // CURRENT_PAGE 应忽略 fromPage/toPage，改用 pageIndex
        SysPostExportQuery query = baseQuery(99);
        query.setExportScope(ExportScope.CURRENT_PAGE);
        query.setPageIndex(3);
        query.setPageSize(10);

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
        when(sysPostMapper.selectListByPage(any(Page.class), eq(query), anyString())).thenReturn(page);

        ResponseEntity<byte[]> response = spreadsheetService.export(query);

        assertThat(response.getBody()).isNotNull().isNotEmpty();
        verify(sysPostMapper).selectListByPage(argThat(p -> p.getCurrent() == 3), eq(query), anyString());
    }

}
