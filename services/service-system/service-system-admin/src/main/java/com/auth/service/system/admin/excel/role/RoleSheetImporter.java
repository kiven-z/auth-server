package com.auth.service.system.admin.excel.role;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色导入器：封装行级校验与批量写入逻辑。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleSheetImporter extends AbstractSheetImporter<SysRoleImportRow, SysRoleForm> {

	private final SysRoleMapper sysRoleMapper;

	private final SysRoleService sysRoleService;

	private Set<String> duplicateRoleCodesInFile = Set.of();

	private Set<String> existingRoleCodes = Set.of();

	@Override
	protected Class<SysRoleImportRow> rowType() {
		return SysRoleImportRow.class;
	}

	@Override
	protected void beforeConvert(List<SysRoleImportRow> rows) {
		duplicateRoleCodesInFile = detectDuplicateRoleCodesInFile(rows);
		existingRoleCodes = loadExistingRoleCodes(rows);
	}

	@Override
	protected SysRoleForm convertRow(SysRoleImportRow row, int rowNum, List<ImportRowError> errors) {
		if (CharSequenceUtil.isBlank(row.getRoleCode())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": roleCode is required"));
			return null;
		}
		if (CharSequenceUtil.isBlank(row.getRoleName())) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": roleName is required"));
			return null;
		}
		String roleCode = row.getRoleCode().trim();
		if (duplicateRoleCodesInFile.contains(roleCode)) {
			errors
				.add(new ImportRowError(rowNum, "Row " + rowNum + ": roleCode '" + roleCode + "' duplicated in file"));
			return null;
		}
		if (existingRoleCodes.contains(roleCode)) {
			errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": roleCode '" + roleCode + "' already exists"));
			return null;
		}
		if (!AuthCodeConvention.isWellFormedRoleCode(roleCode)) {
			errors.add(new ImportRowError(rowNum,
					"Row " + rowNum + ": roleCode '" + roleCode + "' does not match required format"));
			return null;
		}
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode(roleCode);
		form.setRoleName(row.getRoleName().trim());
		form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
		form.setOrderNum(row.getOrderNum());
		form.setRemark(row.getRemark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysRoleForm> forms) {
		forms.forEach(sysRoleService::create);
	}

	private Set<String> detectDuplicateRoleCodesInFile(List<SysRoleImportRow> rows) {
		Set<String> seen = new HashSet<>();
		Set<String> duplicates = new HashSet<>();
		for (SysRoleImportRow row : rows) {
			if (CharSequenceUtil.isBlank(row.getRoleCode())) {
				continue;
			}
			String roleCode = row.getRoleCode().trim();
			if (!seen.add(roleCode)) {
				duplicates.add(roleCode);
			}
		}
		return duplicates;
	}

	private Set<String> loadExistingRoleCodes(List<SysRoleImportRow> rows) {
		List<String> roleCodes = rows.stream()
			.map(SysRoleImportRow::getRoleCode)
			.filter(CharSequenceUtil::isNotBlank)
			.map(String::trim)
			.distinct()
			.toList();
		if (roleCodes.isEmpty()) {
			return Set.of();
		}
		return sysRoleMapper.selectReferenceByRoleCodes(roleCodes)
			.stream()
			.map(RoleReferencePO::getRoleCode)
			.collect(Collectors.toSet());
	}

}
