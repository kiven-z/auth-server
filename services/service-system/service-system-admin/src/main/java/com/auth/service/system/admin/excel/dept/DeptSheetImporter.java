package com.auth.service.system.admin.excel.dept;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.ImportRowValidator;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import org.springframework.stereotype.Component;

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;

/**
 * 部门导入器：封装行级校验与批量写入逻辑。
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class DeptSheetImporter extends AbstractSheetImporter<SysDeptImportRow, SysDeptForm, DeptSheetImporter.Context> {

	/**
	 * 导入校验字段名：deptCode
	 */
	private static final String FIELD_DEPT_CODE = "deptCode";

	/**
	 * 导入校验字段名：deptName
	 */
	private static final String FIELD_DEPT_NAME = "deptName";

	/**
	 * 导入校验字段名：parentDeptCode
	 */
	private static final String FIELD_PARENT_DEPT_CODE = "parentDeptCode";

	private final SysDeptMapper sysDeptMapper;

	private final SysDeptService sysDeptService;

	private final DeptLookupSupport deptLookupSupport;

	@Override
	protected Class<SysDeptImportRow> rowType() {
		return SysDeptImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysDeptImportRow> rows) {
		Map<String, Long> parentIdByCode = deptLookupSupport.resolveIdsByCodes(rows,
				SysDeptImportRow::getParentDeptCode);
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysDeptImportRow::getDeptCode,
				codes -> sysDeptMapper.selectActiveByDeptCodes(codes)
					.stream()
					.map(SysDeptEntity::getDeptCode)
					.collect(Collectors.toSet()));
		return Context.builder()
			.parentIdByCode(parentIdByCode)
			.duplicateDeptCodesInFile(uniqueness.duplicatesInFile())
			.existingDeptCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysDeptForm convertRow(SysDeptImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		ImportRowValidator v = ImportRowValidator.of(rowNum, errors);
		String deptCode = v.require(row.getDeptCode(), FIELD_DEPT_CODE);
		String deptName = v.require(row.getDeptName(), FIELD_DEPT_NAME);
		Long parentId = resolveParentId(row.getParentDeptCode(), v, context);

		v.unique(deptCode, context.duplicateDeptCodesInFile(), context.existingDeptCodes(), FIELD_DEPT_CODE);
		ImportRowSupport.requireEnableStatus(v, row.getStatusLabel());

		return v.mapOrNull(() -> {
			SysDeptForm form = new SysDeptForm();
			form.setParentId(Objects.requireNonNullElse(parentId, ROOT_PARENT_ID));
			form.setDeptCode(deptCode);
			form.setDeptName(deptName);
			form.setStatus(EnableStatusLabels.parseImport(row.getStatusLabel()) == EnableStatus.ENABLED);
			form.setOrderNum(row.getOrderNum());
			form.setRemark(row.getRemark());
			return form;
		});
	}

	@Override
	protected void saveBatch(List<SysDeptForm> forms) {
		sysDeptService.createBatchFromImport(forms);
	}

	/**
	 * 解析父部门ID
	 * @param parentDeptCode 父部门编码
	 * @param v 校验器
	 * @param context 上下文
	 * @return 父部门ID
	 */
	private Long resolveParentId(String parentDeptCode, ImportRowValidator v, Context context) {
		if (CharSequenceUtil.isBlank(parentDeptCode)) {
			return ROOT_PARENT_ID;
		}
		String parentDeptCodeTrimmed = parentDeptCode.trim();
		Long parentId = context.parentIdByCode().get(parentDeptCodeTrimmed);
		v.refExists(parentId, FIELD_PARENT_DEPT_CODE, parentDeptCode);
		return parentId;
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Map<String, Long> parentIdByCode;

		Set<String> duplicateDeptCodesInFile;

		Set<String> existingDeptCodes;

	}

}
