package com.auth.module.file.export;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.validation.ExportPageValidator;
import com.auth.module.file.export.writer.ExcelExportWriter;

/**
 * 同步分页导出模板：解析范围 → 分页拉取 → 转换行 → 写 Excel → 交付下载
 *
 * @param <Q> 导出查询类型
 * @param <PO> 持久层行类型
 * @param <ROW> Excel 导出行类型
 * @author Bunny
 */
public abstract class AbstractSyncPagedExportService<Q extends ExportScopeQuery, PO, ROW> {

	private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final ExcelExportWriter<ROW> excelWriter;

	private final String filenamePrefix;

	/**
	 * @param excelWriter Excel 写入器
	 * @param filenamePrefix 文件名前缀（不含扩展名）
	 */
	protected AbstractSyncPagedExportService(ExcelExportWriter<ROW> excelWriter, String filenamePrefix) {
		this.excelWriter = excelWriter;
		this.filenamePrefix = filenamePrefix;
	}

	/**
	 * 执行同步导出
	 * @param query 导出查询
	 * @return 附件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> export(Q query) throws IOException {
		ExportPageRange range = ExportScopeResolver.resolve(query);
		ExportPageValidator.validatePageRange(range.fromPage(), range.toPage(), range.pageSize());

		List<PO> rows = fetchByPageRange(query, range);
		List<ROW> exportRows = toExportRows(rows);
		String filename = filenamePrefix + "_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".xlsx";
		byte[] bytes = excelWriter.write(exportRows);
		return SyncFileDelivery.deliver(bytes, filename);
	}

	/**
	 * 拉取指定页码区间的数据
	 * @param query 导出查询
	 * @param range 页码区间
	 * @return 数据列表
	 */
	private List<PO> fetchByPageRange(Q query, ExportPageRange range) {
		List<PO> rows = new ArrayList<>();
		for (int pageIndex = range.fromPage(); pageIndex <= range.toPage(); pageIndex++) {
			rows.addAll(fetchPage(query, pageIndex, range.pageSize()));
		}
		return rows;
	}

	/**
	 * 拉取单页数据
	 * @param query 导出查询
	 * @param pageIndex 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 当前页记录
	 */
	protected abstract List<PO> fetchPage(Q query, int pageIndex, int pageSize);

	/**
	 * 将持久层行转为导出行
	 * @param rows 持久层行列表
	 * @return Excel 行列表
	 */
	protected abstract List<ROW> toExportRows(List<PO> rows);

}
