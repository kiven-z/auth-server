package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.user.SysUserConverter;
import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesSnapshot;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户只读查询实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysUserQueryServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysRoleMapper sysRoleMapper;

	private final SysPermissionMapper sysPermissionMapper;

	private final UserReferenceChecker userReferenceChecker;

	private final UserEffectiveCodesResolver userEffectiveCodesResolver;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysUserPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysUserPageVO> voPage = page.convert(SysUserConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserProfileVO getProfile(Long userId) {
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "userId");
		}
		UserEntity user = userReferenceChecker.getExistingActive(userId);
		SysUserProfileVO response = SysUserConverter.INSTANCE.toProfileVo(user);

		List<UserDeptProfilePO> deptProfileList = baseMapper.selectDeptProfileByUserId(userId);
		response.setDeptList(ReferenceConverter.INSTANCE.toUserDeptReferenceList(deptProfileList));

		List<UserPostProfilePO> postProfileList = baseMapper.selectPostProfileByUserId(userId);
		response.setPostList(ReferenceConverter.INSTANCE.toUserPostReferenceList(postProfileList));
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserDetailVO getDetail(Long userId) {
		SysUserProfileVO profile = getProfile(userId);
		SysUserDetailVO detail = SysUserConverter.INSTANCE.fromProfile(profile);

		UserEffectiveCodesSnapshot effectiveCodesSnapshot = UserEffectiveCodesSnapshot.builder()
			.permissionCodes(List.of())
			.roleCodes(List.of())
			.build();
		UserEffectiveCodesSnapshot codes = userEffectiveCodesResolver.resolve(userId).orElse(effectiveCodesSnapshot);

		List<String> roledCodes = codes.roleCodes();
		if (CollUtil.isNotEmpty(roledCodes)) {
			List<RoleReferencePO> roleReferenceList = sysRoleMapper.selectReferenceByRoleCodes(roledCodes);
			detail.setEffectiveRoles(ReferenceConverter.INSTANCE.toRoleReferenceList(roleReferenceList));
		}

		List<String> permissionCodes = codes.permissionCodes();
		if (CollUtil.isNotEmpty(permissionCodes)) {
			List<PermissionReferencePO> permissionReferenceList = sysPermissionMapper
				.selectReferenceByPermissionCodes(permissionCodes);
			detail.setEffectivePermissions(
					ReferenceConverter.INSTANCE.toPermissionReferenceList(permissionReferenceList));
		}

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserSearchItemVO> searchByKeyword(String keyword, Integer limit) {
		if (CharSequenceUtil.isBlank(keyword)) {
			return Collections.emptyList();
		}

		limit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
		List<UserSearchItemPO> searchItemList = baseMapper.searchByKeyword(keyword, limit);
		List<UserSearchItemPO> list = Objects.requireNonNullElse(searchItemList, List.of());

		return SysUserConverter.INSTANCE.toSearchItemVoList(list);
	}

}
