package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.convert.SysPostManageConverter;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysPostDetailMapper;
import com.auth.service.system.admin.model.form.post.SysPostAssignRoleForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.post.SysPostAssignedRoleRow;
import com.auth.service.system.admin.service.SysPostManageService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位扩展管理实现 FIXME 查询条件看是否可以做成动态的，传入的内容几乎一致
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostManageServiceImpl implements SysPostManageService {

	private final PostReferenceChecker postReferenceChecker;

	private final SysPostDetailMapper sysPostDetailMapper;

	private final GrantTableMapper grantTableMapper;

	private final RoleGrantReferenceChecker roleGrantReferenceChecker;

	private final GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysPostAssignedRoleRow> listAssignedRoles(Long postId) {
		postReferenceChecker.getExistingActive(postId);

		List<SysDeptBoundRolePO> boundRoles = sysPostDetailMapper.selectBoundRolesByPostId(postId);
		return SysPostManageConverter.INSTANCE.toAssignedRoleRowList(boundRoles);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void replacePostRoles(Long postId, SysPostAssignRoleForm form) {
		postReferenceChecker.getExistingActive(postId);
		List<String> roleCodes = CollUtil.emptyIfNull(form.getRoleCodes()).stream().distinct().toList();
		roleGrantReferenceChecker.requirePostGrantEligibleRoles(roleCodes);

		Long operatorId = SecurityUserUtils.getUserId();
		grantTableMapper.deleteBySubject(GrantTableSubjectType.POST.name(), postId);
		if (CollUtil.isNotEmpty(roleCodes)) {
			grantTableMapper.batchInsertSubjectRoleGrants(GrantTableSubjectType.POST.name(), postId, roleCodes,
					operatorId);
		}

		grantInvalidationTrigger.submitByGrantSubjects(List.of(new GrantSubjectKey(GrantTableSubjectType.POST, postId)),
				"replace-post-roles:" + postId);
	}

}
