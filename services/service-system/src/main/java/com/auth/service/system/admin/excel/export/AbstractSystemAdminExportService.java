package com.auth.service.system.admin.excel.export;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQuery;
import com.auth.module.file.export.validation.ExportValidationException;
import com.auth.module.file.export.writer.ExcelExportWriter;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * 系统管理域同步分页导出基类：将 module-file 校验异常转为
 * {@link com.auth.service.system.common.exception.SystemBusinessException}
 *
 * @param <Q> 导出查询类型
 * @param <PO> 持久层行类型
 * @param <ROW> Excel 导出行类型
 * @author Bunny
 */
public abstract class AbstractSystemAdminExportService<Q extends ExportScopeQuery, PO, ROW>
		extends AbstractSyncPagedExportService<Q, PO, ROW> {

	/**
	 * @param excelWriter Excel 写入器
	 * @param filenamePrefix 文件名前缀
	 */
	protected AbstractSystemAdminExportService(ExcelExportWriter<ROW> excelWriter, String filenamePrefix) {
		super(excelWriter, filenamePrefix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<byte[]> export(Q query) throws IOException {
		try {
			return super.export(query);
		}
		catch (ExportValidationException exception) {
			throw SystemExportExceptions.translate(exception);
		}
	}

}
