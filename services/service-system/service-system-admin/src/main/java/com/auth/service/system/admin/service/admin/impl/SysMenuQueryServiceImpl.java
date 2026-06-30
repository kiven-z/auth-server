package com.auth.service.system.admin.service.admin.impl;

import java.util.Collections;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.menu.SysMenuConverter;
import com.auth.service.system.admin.mapper.menu.SysMenuMapper;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.admin.SysMenuQueryService;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.auth.service.system.admin.support.menu.MenuWebRouteTreeAssembler;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesSnapshot;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class SysMenuQueryServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserEffectiveCodesResolver userEffectiveCodesResolver;

	private final MenuWebRouteTreeAssembler menuWebRouteTreeAssembler;

	private final MenuReferenceChecker menuReferenceChecker;

	private final SysMenuRoleMapper sysMenuRoleMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RouteNodeVO> listWebRoutes(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}

		UserEffectiveCodesSnapshot codes = userEffectiveCodesResolver.resolve(userId).orElse(null);
		if (codes == null) {
			log.warn("Menu web route: effective codes unavailable userId={}", userId);
			return Collections.emptyList();
		}

		List<SysMenuEntity> menuList = list(
				Wrappers.lambdaQuery(SysMenuEntity.class).eq(SysMenuEntity::getStatus, Boolean.TRUE));

		List<String> roleCodes = codes.roleCodes();
		boolean isAdmin = PermissionConstant.isAdmin(userId, roleCodes);
		return isAdmin ? menuWebRouteTreeAssembler.buildAdminRoutes(menuList)
				: menuWebRouteTreeAssembler.buildRoutes(menuList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuListVO> listFlat(SysMenuQuery query) {
		List<SysMenuEntity> entities = baseMapper.selectListByQuery(query);
		List<SysMenuListVO> list = SysMenuConverter.INSTANCE.toListVoList(entities);

		auditUserDisplayService.enrichAuditUsernames(list, null, null);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuQuery query) {
		IPage<SysMenuEntity> entityPage = baseMapper.selectPageByQuery(pageParams, query);
		IPage<SysMenuListVO> result = entityPage.convert(SysMenuConverter.INSTANCE::toListVo);

		auditUserDisplayService.enrichAuditUsernames(result, null, null);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysMenuDetailVO getDetail(Long id) {
		SysMenuEntity entity = menuReferenceChecker.getExisting(id);
		SysMenuDetailVO detail = SysMenuConverter.INSTANCE.toDetailVo(entity);

		List<RoleReferencePO> boundRoles = sysMenuRoleMapper.selectBoundRolesByMenuId(id);
		detail.setBoundRoles(ReferenceConverter.INSTANCE.toRoleReferenceList(boundRoles));
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
