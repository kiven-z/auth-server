package com.auth.service.system.admin.excel.post;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.service.SysPostService;
import com.auth.service.system.admin.support.dept.DeptCodeIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 岗位导入器：封装行级校验与批量写入逻辑。
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class PostSheetImporter extends AbstractSheetImporter<SysPostImportRow, SysPostForm> {

	private final DeptCodeIdResolver deptCodeIdResolver;

	private final SysPostService sysPostService;

	private Map<String, Long> deptIdByCode = Map.of();

	@Override
	protected Class<SysPostImportRow> rowType() {
		return SysPostImportRow.class;
	}

	@Override
	protected void beforeConvert(List<SysPostImportRow> rows) {
		deptIdByCode = deptCodeIdResolver.resolveIdsByCodes(rows, SysPostImportRow::getDeptCode);
	}

	@Override
	protected SysPostForm convertRow(SysPostImportRow row, int rowNum, List<ImportRowError> errors) {
		if (CharSequenceUtil.isBlank(row.getDeptCode())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode is required"));
			return null;
		}
		if (CharSequenceUtil.isBlank(row.getPostCode())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postCode is required"));
			return null;
		}
		if (CharSequenceUtil.isBlank(row.getPostName())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postName is required"));
			return null;
		}
		Long deptId = resolveDeptId(row.getDeptCode(), rowNum, errors);
		if (deptId == null) {
			return null;
		}
		SysPostForm form = new SysPostForm();
		form.setDeptId(deptId);
		form.setPostCode(row.getPostCode().trim());
		form.setPostName(row.getPostName().trim());
		form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
		form.setOrderNum(row.getOrderNum());
		form.setRemark(row.getRemark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysPostForm> forms) {
		forms.forEach(sysPostService::create);
	}

	private Long resolveDeptId(String deptCode, int rowNum, List<ImportRowError> errors) {
		String deptCodeTrimmed = deptCode.trim();
		Long deptId = deptIdByCode.get(deptCodeTrimmed);
		if (deptId == null) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode '" + deptCode + "' not found"));
			return null;
		}
		return deptId;
	}

}
