package com.auth.module.file.export;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.model.ExportPageRange;
import com.auth.module.file.export.validation.ExportPageValidator;
import com.auth.module.file.export.writer.ExcelExportWriter;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 同步分页导出模板：解析范围 → 分页拉取 → 转换行 → 写 Excel → 交付下载
 *
 * @param <F> 列表筛选类型
 * @param <P> 持久层行类型
 * @param <R> Excel 导出行类型
 * @author Bunny
 */
public abstract class AbstractSyncPagedExportService<F extends PageQueryRequest, P, R> {

	private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final ExcelExportWriter<R> excelWriter;

	private final String filenamePrefix;

	/**
	 * @param excelWriter Excel 写入器
	 * @param filenamePrefix 文件名前缀（不含扩展名）
	 */
	protected AbstractSyncPagedExportService(ExcelExportWriter<R> excelWriter, String filenamePrefix) {
		this.excelWriter = excelWriter;
		this.filenamePrefix = filenamePrefix;
	}

	/**
	 * 执行同步导出
	 * @param filter 列表筛选（与分页查询一致）
	 * @param scope 导出范围
	 * @return 附件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> export(F filter, ExportScopeQueryFields scope) throws IOException {
		ExportScopeQuery scopeQuery = ExportScopeQueries.merge(filter, scope);
		ExportPageRange range = ExportScopeResolver.resolve(scopeQuery);
		ExportPageValidator.validatePageRange(range.fromPage(), range.toPage(), range.pageSize());

		List<P> rows = fetchByPageRange(filter, range);
		List<R> exportRows = toExportRows(rows);
		String filename = filenamePrefix + "_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".xlsx";
		byte[] bytes = excelWriter.write(exportRows);
		return SyncFileDelivery.deliver(bytes, filename);
	}

	/**
	 * 拉取指定页码区间的数据
	 * @param filter 列表筛选
	 * @param range 页码区间
	 * @return 数据列表
	 */
	private List<P> fetchByPageRange(F filter, ExportPageRange range) {
		List<P> rows = new ArrayList<>();
		for (int pageIndex = range.fromPage(); pageIndex <= range.toPage(); pageIndex++) {
			rows.addAll(fetchPage(filter, pageIndex, range.pageSize()));
		}
		return rows;
	}

	/**
	 * 拉取单页数据
	 * @param filter 列表筛选
	 * @param pageIndex 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 当前页记录
	 */
	protected abstract List<P> fetchPage(F filter, int pageIndex, int pageSize);

	/**
	 * 将持久层行转为导出行
	 * @param rows 持久层行列表
	 * @return Excel 行列表
	 */
	protected abstract List<R> toExportRows(List<P> rows);

}
