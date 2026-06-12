package com.auth.service.system.admin.excel.post;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.ImportRowError;
import com.auth.module.file.importer.SpreadsheetImportResult;
import com.auth.module.file.importer.reader.ExcelImportReader;
import com.auth.service.system.admin.convert.SysPostSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.service.SysPostService;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 岗位 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysPostSpreadsheetService
        extends AbstractSyncPagedExportService<SysPostQuery, SysPostPageRowPO, SysPostSpreadsheetRow> {

    /**
     * 单次导入最大行数
     */
    private static final int MAX_IMPORT_ROWS = 500;

    private static final String SHEET_NAME = "岗位";

    private static final String FILENAME_PREFIX = "岗位";

    private static final int STATUS_COLUMN_INDEX = 4;

    private final SysPostMapper sysPostMapper;

    private final SysDeptMapper sysDeptMapper;

    private final SysPostService sysPostService;

    public SysPostSpreadsheetService(SysPostMapper sysPostMapper, SysDeptMapper sysDeptMapper,
                                     SysPostService sysPostService) {
        super(buildExcelWriter(), FILENAME_PREFIX);
        this.sysPostMapper = sysPostMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysPostService = sysPostService;
    }

    /**
     * 构建 Excel 导出 writer
     *
     * @return Excel 导出 writer
     */
    private static ExcelExportWriter<SysPostSpreadsheetRow> buildExcelWriter() {
        return new ExcelExportWriter<>(SysPostSpreadsheetRow.class, SHEET_NAME)
                .registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
                .registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
    }

    /**
     * 状态文案 → 布尔值（缺省启用）
     */
    private static Boolean parseStatusLabel(String label) {
        if ("停用".equals(label)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 按筛选条件与页码区间导出岗位 Excel
     *
     * @param filter 列表筛选
     * @param scope  导出范围
     * @return 文件字节响应
     * @throws IOException 写入失败
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> export(SysPostQuery filter, ExportScopeQueryFields scope) throws IOException {
        return super.export(filter, scope);
    }

    @Override
    protected List<SysPostPageRowPO> fetchPage(SysPostQuery filter, int pageIndex, int pageSize) {
        String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(filter.getSort());
        Page<SysPostEntity> pageParams = new Page<>(pageIndex, pageSize);
        IPage<SysPostPageRowPO> page = sysPostMapper.selectListByPage(pageParams, filter, orderBySql);
        return page.getRecords();
    }

    @Override
    protected List<SysPostSpreadsheetRow> toExportRows(List<SysPostPageRowPO> rows) {
        return SysPostSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
    }

    /**
     * 从上传文件批量导入岗位（解析 → 校验 → 写入，全部通过才落库）
     *
     * @param file 上传的 Excel 文件
     * @return 导入结果（含行级错误）
     * @throws IOException 文件读取失败
     */
    @Transactional(rollbackFor = Exception.class)
    public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
        List<SysPostImportRow> rows = new ExcelImportReader<>(SysPostImportRow.class).read(file);

        if (rows.isEmpty()) {
            return SpreadsheetImportResult.success(0);
        }
        if (rows.size() > MAX_IMPORT_ROWS) {
            return SpreadsheetImportResult.withErrors(List.of(
                    new ImportRowError(0, "Import row count exceeds limit: max=" + MAX_IMPORT_ROWS)));
        }

        List<ImportRowError> errors = new ArrayList<>();
        List<SysPostForm> forms = new ArrayList<>(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 1;
            SysPostImportRow row = rows.get(i);

            if (CharSequenceUtil.isBlank(row.getDeptCode())) {
                errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode is required"));
                continue;
            }
            if (CharSequenceUtil.isBlank(row.getPostCode())) {
                errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postCode is required"));
                continue;
            }
            if (CharSequenceUtil.isBlank(row.getPostName())) {
                errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postName is required"));
                continue;
            }

            Long deptId = resolveDeptId(row.getDeptCode(), rowNum, errors);
            if (deptId == null) {
                continue;
            }

            SysPostForm form = new SysPostForm();
            form.setDeptId(deptId);
            form.setPostCode(row.getPostCode().trim());
            form.setPostName(row.getPostName().trim());
            form.setStatus(parseStatusLabel(row.getStatusLabel()));
            form.setOrderNum(row.getOrderNum());
            form.setRemark(row.getRemark());
            forms.add(form);
        }

        if (!errors.isEmpty()) {
            return SpreadsheetImportResult.withErrors(errors);
        }

        sysPostService.importBatch(forms);
        return SpreadsheetImportResult.success(forms.size());
    }

    /**
     * 通过部门编码查询部门 ID；找不到时记录错误并返回 null
     */
    private Long resolveDeptId(String deptCode, int rowNum, List<ImportRowError> errors) {
        SysDeptEntity dept = sysDeptMapper
                .selectOne(new LambdaQueryWrapper<SysDeptEntity>().eq(SysDeptEntity::getDeptCode, deptCode.trim()));
        if (dept == null) {
            errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode '" + deptCode + "' not found"));
            return null;
        }
        return dept.getId();
    }

}
