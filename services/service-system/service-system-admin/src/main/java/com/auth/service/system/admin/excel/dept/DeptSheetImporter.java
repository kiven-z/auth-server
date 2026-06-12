package com.auth.service.system.admin.excel.dept;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptCodeIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 部门导入器：封装行级校验与批量写入逻辑。
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class DeptSheetImporter extends AbstractSheetImporter<SysDeptImportRow, SysDeptForm> {

	private final SysDeptService sysDeptService;

	private final DeptCodeIdResolver deptCodeIdResolver;

	private Map<String, Long> parentIdByCode = Map.of();

	@Override
	protected Class<SysDeptImportRow> rowType() {
		return SysDeptImportRow.class;
	}

	@Override
	protected void beforeConvert(List<SysDeptImportRow> rows) {
		parentIdByCode = deptCodeIdResolver.resolveIdsByCodes(rows, SysDeptImportRow::getParentDeptCode);
	}

	@Override
	protected SysDeptForm convertRow(SysDeptImportRow row, int rowNum, List<ImportRowError> errors) {
		if (CharSequenceUtil.isBlank(row.getDeptCode())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode is required"));
			return null;
		}
		if (CharSequenceUtil.isBlank(row.getDeptName())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptName is required"));
			return null;
		}
		Long parentId = resolveParentId(row.getParentDeptCode(), rowNum, errors);
		if (parentId == null && CharSequenceUtil.isNotBlank(row.getParentDeptCode())) {
			return null;
		}
		SysDeptForm form = new SysDeptForm();
		form.setParentId(parentId != null ? parentId : 0L);
		form.setDeptCode(row.getDeptCode().trim());
		form.setDeptName(row.getDeptName().trim());
		form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
		form.setOrderNum(row.getOrderNum());
		form.setRemark(row.getRemark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysDeptForm> forms) {
		sysDeptService.importBatch(forms);
	}

	private Long resolveParentId(String parentDeptCode, int rowNum, List<ImportRowError> errors) {
		if (CharSequenceUtil.isBlank(parentDeptCode)) {
			return 0L;
		}
		String parentDeptCodeTrimmed = parentDeptCode.trim();
		Long parentId = parentIdByCode.get(parentDeptCodeTrimmed);
		if (parentId == null) {
			errors.add(new ImportRowError(rowNum,
					"Row " + rowNum + ": parentDeptCode '" + parentDeptCode + "' not found"));
			return null;
		}
		return parentId;
	}

}
