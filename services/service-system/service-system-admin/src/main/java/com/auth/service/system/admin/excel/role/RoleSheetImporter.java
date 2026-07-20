package com.auth.service.system.admin.excel.role;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.ImportRowValidator;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysRoleService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.auth.module.file.importer.model.ImportErrorCode.INVALID_FORMAT;

/**
 * 角色导入器：封装行级校验与批量写入逻辑。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleSheetImporter extends AbstractSheetImporter<SysRoleImportRow, SysRoleForm, RoleSheetImporter.Context> {

	/**
	 * 导入校验字段名：roleCode
	 */
	private static final String FIELD_ROLE_CODE = "roleCode";

	/**
	 * 导入校验字段名：roleName
	 */
	private static final String FIELD_ROLE_NAME = "roleName";

	private final SysRoleMapper sysRoleMapper;

	private final SysRoleService sysRoleService;

	@Override
	protected Class<SysRoleImportRow> rowType() {
		return SysRoleImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysRoleImportRow> rows) {
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysRoleImportRow::getRoleCode,
				codes -> sysRoleMapper.selectReferenceByRoleCodes(codes)
					.stream()
					.map(RoleReferencePO::getRoleCode)
					.collect(Collectors.toSet()));
		return Context.builder()
			.duplicateRoleCodesInFile(uniqueness.duplicatesInFile())
			.existingRoleCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysRoleForm convertRow(SysRoleImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		ImportRowValidator v = ImportRowValidator.of(rowNum, errors);
		String roleCode = v.require(row.getRoleCode(), FIELD_ROLE_CODE);
		String roleName = v.require(row.getRoleName(), FIELD_ROLE_NAME);
		v.unique(roleCode, context.duplicateRoleCodesInFile(), context.existingRoleCodes(), FIELD_ROLE_CODE);
		v.assertThat(AuthCodeConvention.isWellFormedRoleCode(roleCode), INVALID_FORMAT, FIELD_ROLE_CODE, roleCode);

		ImportRowSupport.requireEnableStatus(v, row.getStatusLabel());

		return v.mapOrNull(() -> {
			SysRoleForm form = new SysRoleForm();
			form.setRoleCode(roleCode);
			form.setRoleName(roleName);
			form.setStatus(EnableStatusLabels.parseImport(row.getStatusLabel()) == EnableStatus.ENABLED);
			form.setOrderNum(row.getOrderNum());
			form.setRemark(row.getRemark());
			return form;
		});
	}

	@Override
	protected void saveBatch(List<SysRoleForm> forms) {
		sysRoleService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Set<String> duplicateRoleCodesInFile;

		Set<String> existingRoleCodes;

	}

}