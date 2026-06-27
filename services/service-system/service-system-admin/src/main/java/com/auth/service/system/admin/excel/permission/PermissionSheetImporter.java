package com.auth.service.system.admin.excel.permission;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限导入器：封装行级校验与批量写入逻辑。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionSheetImporter extends AbstractSheetImporter<SysPermissionImportRow, SysPermissionForm> {

	private final SysPermissionMapper sysPermissionMapper;

	private final SysPermissionService sysPermissionService;

	private Set<String> duplicatePermissionCodesInFile = Set.of();

	private Set<String> existingPermissionCodes = Set.of();

	@Override
	protected Class<SysPermissionImportRow> rowType() {
		return SysPermissionImportRow.class;
	}

	@Override
	protected void beforeConvert(List<SysPermissionImportRow> rows) {
		duplicatePermissionCodesInFile = detectDuplicatePermissionCodesInFile(rows);
		existingPermissionCodes = loadExistingPermissionCodes(rows);
	}

	@Override
	protected SysPermissionForm convertRow(SysPermissionImportRow row, int rowNum, List<ImportRowError> errors) {
		final String permissionCodeStr = ": permissionCode '";

		if (CharSequenceUtil.isBlank(row.getPermissionCode())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": permissionCode is required"));
			return null;
		}
		if (CharSequenceUtil.isBlank(row.getPermissionName())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": permissionName is required"));
			return null;
		}
		String permissionCode = row.getPermissionCode().trim();
		if (duplicatePermissionCodesInFile.contains(permissionCode)) {
			errors.add(new ImportRowError(rowNum,
					"Row " + rowNum + permissionCodeStr + permissionCode + " duplicated in file"));
			return null;
		}
		if (existingPermissionCodes.contains(permissionCode)) {
			errors.add(new ImportRowError(rowNum,
					"Row " + rowNum + permissionCodeStr + permissionCode + "' already exists"));
			return null;
		}
		if (!AuthCodeConvention.isWellFormedPermissionCode(permissionCode)) {
			errors.add(new ImportRowError(rowNum,
					"Row " + rowNum + permissionCodeStr + permissionCode + "' does not match required format"));
			return null;
		}
		SysPermissionForm form = new SysPermissionForm();
		form.setPermissionCode(permissionCode);
		form.setPermissionName(row.getPermissionName().trim());
		form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
		form.setOrderNum(row.getOrderNum());
		form.setRemark(row.getRemark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysPermissionForm> forms) {
		forms.forEach(sysPermissionService::create);
	}

	private Set<String> detectDuplicatePermissionCodesInFile(List<SysPermissionImportRow> rows) {
		Set<String> seen = new HashSet<>();
		Set<String> duplicates = new HashSet<>();
		for (SysPermissionImportRow row : rows) {
			if (CharSequenceUtil.isBlank(row.getPermissionCode())) {
				continue;
			}
			String permissionCode = row.getPermissionCode().trim();
			if (!seen.add(permissionCode)) {
				duplicates.add(permissionCode);
			}
		}
		return duplicates;
	}

	private Set<String> loadExistingPermissionCodes(List<SysPermissionImportRow> rows) {
		List<String> permissionCodes = rows.stream()
			.map(SysPermissionImportRow::getPermissionCode)
			.filter(CharSequenceUtil::isNotBlank)
			.map(String::trim)
			.distinct()
			.toList();
		if (permissionCodes.isEmpty()) {
			return Set.of();
		}
		return sysPermissionMapper.selectReferenceByPermissionCodes(permissionCodes)
			.stream()
			.map(PermissionReferencePO::getPermissionCode)
			.collect(Collectors.toSet());
	}

}
