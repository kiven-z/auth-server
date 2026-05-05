package com.auth.service.auth.infrastructure.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.model.dto.inner.GrantSubjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 授权影响面查询：批量反查受影响用户 ID，SQL 语义与 {@code UserRolesCte} 一致。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationImpactQueryService {

	/**
	 * grant 主体类型：用户
	 */
	public static final String SUBJECT_USER = "USER";

	/**
	 * grant 主体类型：部门
	 */
	public static final String SUBJECT_DEPT = "DEPT";

	/**
	 * grant 主体类型：岗位
	 */
	public static final String SUBJECT_POST = "POST";

	private final AuthorizationImpactMapper authorizationImpactMapper;

	private static List<Long> distinctNonNull(List<Long> rows) {
		if (CollUtil.isEmpty(rows)) {
			return List.of();
		}
		return rows.stream().filter(java.util.Objects::nonNull).distinct().toList();
	}

	/**
	 * 按单个角色编码查询受影响用户 ID
	 * @param roleCode 角色编码
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByRoleCode(String roleCode) {
		if (CharSequenceUtil.isBlank(roleCode)) {
			return List.of();
		}
		List<Long> rows = authorizationImpactMapper.selectUserIdsByRoleCode(roleCode);
		return distinctNonNull(rows);
	}

	/**
	 * 按多个角色编码查询受影响用户 ID
	 * @param roleCodes 角色编码列表
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByRoleCodes(List<String> roleCodes) {
		if (CollUtil.isEmpty(roleCodes)) {
			return List.of();
		}
		List<String> cleaned = roleCodes.stream().filter(CharSequenceUtil::isNotBlank).distinct().toList();
		if (cleaned.isEmpty()) {
			return List.of();
		}
		return distinctNonNull(authorizationImpactMapper.selectUserIdsByRoleCodes(cleaned));
	}

	/**
	 * 按多个权限码查询受影响用户 ID
	 * @param permissionCodes 权限码列表
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByPermissionCodes(List<String> permissionCodes) {
		if (CollUtil.isEmpty(permissionCodes)) {
			return List.of();
		}
		List<String> cleaned = permissionCodes.stream().filter(CharSequenceUtil::isNotBlank).distinct().toList();
		if (cleaned.isEmpty()) {
			return List.of();
		}
		return distinctNonNull(authorizationImpactMapper.selectUserIdsByPermissionCodes(cleaned));
	}

	/**
	 * 按多个策略码查询受影响用户 ID
	 * @param policyCodes 策略码列表
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByPolicyCodes(List<String> policyCodes) {
		if (CollUtil.isEmpty(policyCodes)) {
			return List.of();
		}
		List<String> cleaned = policyCodes.stream().filter(CharSequenceUtil::isNotBlank).distinct().toList();
		if (cleaned.isEmpty()) {
			return List.of();
		}
		return distinctNonNull(authorizationImpactMapper.selectUserIdsByPolicyCodes(cleaned));
	}

	/**
	 * 按部门 ID 列表查询部门下用户（user_dept）
	 * @param deptIds 部门 ID
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByDeptIds(List<Long> deptIds) {
		List<Long> ids = distinctNonNull(deptIds);
		if (ids.isEmpty()) {
			return List.of();
		}
		return distinctNonNull(authorizationImpactMapper.selectUserIdsByDeptIds(ids));
	}

	/**
	 * 按岗位 ID 列表查询岗位下用户（user_post）
	 * @param postIds 岗位 ID
	 * @return 用户 ID 列表
	 */
	public List<Long> listUserIdsByPostIds(List<Long> postIds) {
		List<Long> ids = distinctNonNull(postIds);
		if (ids.isEmpty()) {
			return List.of();
		}
		return distinctNonNull(authorizationImpactMapper.selectUserIdsByPostIds(ids));
	}

	/**
	 * 按授权主体列表合并查询：USER 直接收集 subjectId；DEPT/POST 走成员表批量查询。
	 * @param subjects 主体列表
	 * @return 去重后的用户 ID
	 */
	public List<Long> listUserIdsByGrantSubjects(List<GrantSubjectRequest> subjects) {
		if (CollUtil.isEmpty(subjects)) {
			return List.of();
		}
		Set<Long> out = new LinkedHashSet<>();
		Set<Long> deptIdSet = new LinkedHashSet<>();
		Set<Long> postIdSet = new LinkedHashSet<>();

		for (GrantSubjectRequest row : subjects) {
			if (row == null || CharSequenceUtil.isBlank(row.getSubjectType()) || row.getSubjectId() == null) {
				continue;
			}
			String t = row.getSubjectType().trim();
			Long sid = row.getSubjectId();
			if (SUBJECT_USER.equalsIgnoreCase(t)) {
				out.add(sid);
			}
			else if (SUBJECT_DEPT.equalsIgnoreCase(t)) {
				deptIdSet.add(sid);
			}
			else if (SUBJECT_POST.equalsIgnoreCase(t)) {
				postIdSet.add(sid);
			}
		}

		out.addAll(listUserIdsByDeptIds(List.copyOf(deptIdSet)));
		out.addAll(listUserIdsByPostIds(List.copyOf(postIdSet)));
		return List.copyOf(out);
	}

}
