package com.auth.service.system.admin.service.impl;

import com.auth.module.file.export.impl.ExcelExportStrategy;
import com.auth.service.system.admin.convert.SysRoleExportConverter;
import com.auth.service.system.admin.excel.RoleTypeColorCellWriteHandler;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.excel.export.ExportPageValidator;
import com.auth.service.system.admin.excel.role.SysRoleExportRow;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
import com.auth.service.system.admin.service.SysRoleExportService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色 Excel 导出实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysRoleExportServiceImpl implements SysRoleExportService {

    private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String SHEET_NAME = "角色";

    private static final int ROLE_TYPE_COLUMN_INDEX = 2;

    private static final int STATUS_COLUMN_INDEX = 3;

    private final SysRoleMapper sysRoleMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> export(SysRoleExportQuery query) throws IOException {
        // FIXME 结构和岗位类似，考虑合并
        int fromPage = query.getFromPage();
        int toPage = query.getToPage();
        int pageSize = ExportPageValidator.normalizePageSize(query.getPageSize());
        ExportPageValidator.validatePageRange(fromPage, toPage, pageSize);

        String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
        List<SysRoleEntity> rows = new ArrayList<>();
        for (int pageIndex = fromPage; pageIndex <= toPage; pageIndex++) {
            Page<SysRoleEntity> pageParams = new Page<>(pageIndex, pageSize);
            IPage<SysRoleEntity> page = sysRoleMapper.selectListByPage(pageParams, query, orderBySql);
            rows.addAll(page.getRecords());
        }
        List<SysRoleExportRow> exportRows = SysRoleExportConverter.INSTANCE.toExportRows(rows);

        String filename = "角色_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".xlsx";
        ExcelExportStrategy<SysRoleExportRow> strategy = new ExcelExportStrategy<>(SysRoleExportRow.class, SHEET_NAME)
                .registerWriteHandler(new RoleTypeColorCellWriteHandler(ROLE_TYPE_COLUMN_INDEX))
                .registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            return strategy.export(exportRows, outputStream, filename);
        }
    }

}
