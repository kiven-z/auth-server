package com.auth.service.system.admin.support.granttable;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.LongConsumer;

/**
 * grant_table 授权前校验主体存在且启用；新增主体类型时在此注册即可。
 *
 * @author Bunny
 */
@Component
public class GrantTableSubjectExistenceVerifier {

	private final Map<GrantTableSubjectType, LongConsumer> activeSubjectChecks;

	public GrantTableSubjectExistenceVerifier(DeptReferenceChecker deptReferenceChecker,
			PostReferenceChecker postReferenceChecker) {
		activeSubjectChecks = new EnumMap<>(GrantTableSubjectType.class);
		if (deptReferenceChecker != null) {
			activeSubjectChecks.put(GrantTableSubjectType.DEPT, deptReferenceChecker::getExistingActive);
		}
		if (postReferenceChecker != null) {
			activeSubjectChecks.put(GrantTableSubjectType.POST, postReferenceChecker::getExistingActive);
		}
	}

	/**
	 * 校验授权主体存在且为启用状态。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 */
	public void requireExistingActive(GrantTableSubjectType subjectType, Long subjectId) {
		LongConsumer check = activeSubjectChecks.get(subjectType);
		if (check == null) {
			throw new IllegalArgumentException("Unsupported grant subject type: " + subjectType);
		}
		check.accept(subjectId);
	}

}
