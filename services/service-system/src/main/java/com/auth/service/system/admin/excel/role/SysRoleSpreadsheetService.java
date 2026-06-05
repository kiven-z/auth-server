package com.auth.service.system.admin.excel.role;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysRoleSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 角色 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysRoleSpreadsheetService
        extends AbstractSyncPagedExportService<SysRoleExportQuery, SysRoleEntity, SysRoleSpreadsheetRow> {

    private static final String SHEET_NAME = "角色";

    private static final String FILENAME_PREFIX = "角色";

    private static final int STATUS_COLUMN_INDEX = 3;

    private final SysRoleMapper sysRoleMapper;

    public SysRoleSpreadsheetService(SysRoleMapper sysRoleMapper) {
        super(buildExcelWriter(), FILENAME_PREFIX);
        this.sysRoleMapper = sysRoleMapper;
    }

    /**
     * 构建 Excel 导出 writer
     *
     * @return Excel 导出 writer
     */
    private static ExcelExportWriter<SysRoleSpreadsheetRow> buildExcelWriter() {
        return new ExcelExportWriter<>(SysRoleSpreadsheetRow.class, SHEET_NAME)
                .registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
                .registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
    }

    /**
     * 按筛选条件与页码区间导出角色 Excel
     *
     * @param query 导出查询
     * @return 文件字节响应
     * @throws IOException 写入失败
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> export(SysRoleExportQuery query) throws IOException {
        return super.export(query);
    }

    @Override
    protected List<SysRoleEntity> fetchPage(SysRoleExportQuery query, int pageIndex, int pageSize) {
        String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
        Page<SysRoleEntity> pageParams = new Page<>(pageIndex, pageSize);
        IPage<SysRoleEntity> page = sysRoleMapper.selectListByPage(pageParams, query, orderBySql);
        return page.getRecords();
    }

    @Override
    protected List<SysRoleSpreadsheetRow> toExportRows(List<SysRoleEntity> rows) {
        return SysRoleSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
    }

}
