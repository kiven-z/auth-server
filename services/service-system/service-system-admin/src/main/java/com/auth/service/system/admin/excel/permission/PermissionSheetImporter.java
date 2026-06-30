package com.auth.service.system.admin.excel.permission;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.ImportRowValidator;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import org.springframework.stereotype.Component;

import static com.auth.module.file.importer.model.ImportErrorCode.INVALID_FORMAT;

/**
 * 权限导入器：封装行级校验与批量写入逻辑。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionSheetImporter
		extends AbstractSheetImporter<SysPermissionImportRow, SysPermissionForm, PermissionSheetImporter.Context> {

	/**
	 * 导入校验字段名：permissionCode
	 */
	private static final String FIELD_PERMISSION_CODE = "permissionCode";

	/**
	 * 导入校验字段：permissionName
	 */
	private static final String FIELD_PERMISSION_NAME = "permissionName";

	private final SysPermissionMapper sysPermissionMapper;

	private final SysPermissionService sysPermissionService;

	@Override
	protected Class<SysPermissionImportRow> rowType() {
		return SysPermissionImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysPermissionImportRow> rows) {
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysPermissionImportRow::getPermissionCode,
				codes -> sysPermissionMapper.selectReferenceByPermissionCodes(codes)
					.stream()
					.map(PermissionReferencePO::getPermissionCode)
					.collect(Collectors.toSet()));

		return Context.builder()
			.duplicatePermissionCodesInFile(uniqueness.duplicatesInFile())
			.existingPermissionCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysPermissionForm convertRow(SysPermissionImportRow row, int rowNum, List<ImportRowError> errors,
			Context context) {
		ImportRowValidator v = ImportRowValidator.of(rowNum, errors);
		String permissionCode = v.require(row.getPermissionCode(), FIELD_PERMISSION_CODE);
		String permissionName = v.require(row.getPermissionName(), FIELD_PERMISSION_NAME);

		return ImportRowSupport
			.requireEnableStatus(v
				.unique(permissionCode, context.duplicatePermissionCodesInFile(), context.existingPermissionCodes(),
						FIELD_PERMISSION_CODE)
				.assertThat(AuthCodeConvention.isWellFormedPermissionCode(permissionCode), INVALID_FORMAT,
						FIELD_PERMISSION_CODE, permissionCode),
					row.getStatusLabel())
			.mapOrNull(() -> {
				SysPermissionForm form = new SysPermissionForm();
				form.setPermissionCode(permissionCode);
				form.setPermissionName(permissionName);
				form.setStatus(EnableStatusLabels.parseImport(row.getStatusLabel()) == EnableStatus.ENABLED);
				form.setOrderNum(row.getOrderNum());
				form.setRemark(row.getRemark());
				return form;
			});
	}

	@Override
	protected void saveBatch(List<SysPermissionForm> forms) {
		sysPermissionService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Set<String> duplicatePermissionCodesInFile;

		Set<String> existingPermissionCodes;

	}

}
