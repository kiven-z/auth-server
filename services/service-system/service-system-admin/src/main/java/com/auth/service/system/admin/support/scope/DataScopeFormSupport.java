package com.auth.service.system.admin.support.scope;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据范围表单解析
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DataScopeFormSupport {

	private final DeptReferenceChecker deptReferenceChecker;

	/**
	 * 解析范围类型并规范化部门 ID
	 * @param form 数据范围表单
	 * @return 已解析类型与部门 ID
	 */
	public ResolvedDataScope resolve(SysDataScopeForm form) {
		DataScopeStorageType scopeType = DataScopeStorageType.parse(form.getScopeType());
		if (scopeType == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID);
		}
		List<Long> scopeDeptIds = normalizeDeptIds(scopeType, form.getScopeDeptIds());
		return new ResolvedDataScope(scopeType, scopeDeptIds);
	}

	/**
	 * 规范化部门 ID：DEPT* 必填且须存在；ALL/SELF 清空
	 * @param scopeType 范围类型
	 * @param rawDeptIds 表单部门 ID
	 * @return 去重后的部门 ID；非部门维为空列表
	 */
	private List<Long> normalizeDeptIds(DataScopeStorageType scopeType, List<Long> rawDeptIds) {
		if (scopeType != DataScopeStorageType.DEPT && scopeType != DataScopeStorageType.DEPT_AND_CHILD) {
			return List.of();
		}
		if (CollUtil.isEmpty(rawDeptIds)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED);
		}
		List<Long> distinctIds = rawDeptIds.stream().distinct().toList();
		deptReferenceChecker.requireExistingDeptIds(distinctIds);
		return distinctIds;
	}

	/**
	 * 表单解析结果
	 *
	 * @param scopeType 已解析的范围类型
	 * @param scopeDeptIds 已规范化的部门 ID（ALL/SELF 为空列表）
	 */
	public record ResolvedDataScope(DataScopeStorageType scopeType, List<Long> scopeDeptIds) {
	}

}
