package com.auth.module.file.importer;

import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.file.importer.reader.ExcelImportReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 单 Sheet 批量导入骨架
 *
 * @param <R> Excel 行类型（含 {@code @ExcelProperty} 注解）
 * @param <F> 业务表单类型
 * @author Bunny
 */
public abstract class AbstractSheetImporter<R, F> {

	/**
	 * 默认单次导入最大行数，子类可 override 调整
	 */
	protected int maxRows() {
		return 500;
	}

	/**
	 * 行类型，供 {@link ExcelImportReader} 解析使用
	 * @return 行类型 Class
	 */
	protected abstract Class<R> rowType();

	/**
	 * 将单行 Excel 数据转为业务表单；校验失败时追加错误并返回 null
	 * @param row 原始行数据
	 * @param rowNum 行号（1-based，不含表头）
	 * @param errors 错误收集列表
	 * @return 转换成功返回表单，失败返回 null
	 */
	protected abstract F convertRow(R row, int rowNum, List<ImportRowError> errors);

	/**
	 * 批量写入，实现类应在事务内逐条调用业务 create 等方法
	 * @param forms 已通过校验的表单列表
	 */
	protected abstract void saveBatch(List<F> forms);

	/**
	 * 行转换前的预处理钩子，子类可用于批量预加载上下文数据（如编码映射）
	 * @param rows 已解析的所有行
	 */
	protected void beforeConvert(List<R> rows) {
		// default no-op
	}

	/**
	 * 执行导入（模板方法，不可 override）
	 * @param file 上传文件
	 * @return 导入结果
	 * @throws IOException 文件读取失败
	 */
	public final SpreadsheetImportResult doImport(MultipartFile file) throws IOException {
		List<R> rows = new ExcelImportReader<>(rowType()).read(file);

		if (rows.isEmpty()) {
			return SpreadsheetImportResult.success(0);
		}
		if (rows.size() > maxRows()) {
			return SpreadsheetImportResult
				.withErrors(List.of(new ImportRowError(0, "Import row count exceeds limit: max=" + maxRows())));
		}

		List<ImportRowError> errors = new ArrayList<>();
		List<F> forms = new ArrayList<>(rows.size());
		beforeConvert(rows);

		for (int i = 0; i < rows.size(); i++) {
			F form = convertRow(rows.get(i), i + 1, errors);
			if (form != null) {
				forms.add(form);
			}
		}

		if (!errors.isEmpty()) {
			return SpreadsheetImportResult.withErrors(errors);
		}

		saveBatch(forms);
		return SpreadsheetImportResult.success(forms.size());
	}

}
