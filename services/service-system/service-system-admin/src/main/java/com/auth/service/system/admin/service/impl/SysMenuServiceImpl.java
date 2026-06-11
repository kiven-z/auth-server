package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.SysMenuConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.admin.service.SysMenuService;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesSnapshot;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 菜单服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserEffectiveCodesResolver userEffectiveCodesResolver;

	private final MenuWebRouteTreeBuilder menuWebRouteTreeBuilder;

	private final MenuReferenceChecker menuReferenceChecker;

	public SysMenuServiceImpl(AuditUserDisplayService auditUserDisplayService,
			UserEffectiveCodesResolver userEffectiveCodesResolver, MenuWebRouteTreeBuilder menuWebRouteTreeBuilder,
			MenuReferenceChecker menuReferenceChecker) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.userEffectiveCodesResolver = userEffectiveCodesResolver;
		this.menuWebRouteTreeBuilder = menuWebRouteTreeBuilder;
		this.menuReferenceChecker = menuReferenceChecker;
	}

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

		List<String> roleCodes = codes.roleCodes();
		List<String> permissionCodes = codes.permissionCodes();
		boolean isAdmin = PermissionConstant.isAdmin(userId, roleCodes);
		log.debug("Menu web route: userId={} admin={} roleCount={} permissionCount={}", userId, isAdmin,
				roleCodes.size(), permissionCodes.size());

		List<SysMenuEntity> menuList = list(
				Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getStatus, Boolean.TRUE));

		return isAdmin ? menuWebRouteTreeBuilder.buildAdminRoutes(menuList)
				: menuWebRouteTreeBuilder.buildRoutes(menuList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysMenuListVO> listFlat(SysMenuQuery query) {
		List<SysMenuEntity> entities = baseMapper.selectListByQuery(query);
		List<SysMenuListVO> list = SysMenuConverter.INSTANCE.toListVoList(entities);
		auditUserDisplayService.enrichAuditUsernames(list);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuQuery query) {
		IPage<SysMenuEntity> entityPage = baseMapper.selectPageByQuery(pageParams, query);
		IPage<SysMenuListVO> result = entityPage.convert(SysMenuConverter.INSTANCE::toListVo);
		auditUserDisplayService.enrichAuditUsernames(result);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysMenuDetailVO getDetail(Long id) {
		SysMenuEntity entity = getById(id);
		return SysMenuConverter.INSTANCE.toDetailVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String create(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(ObjectUtil.defaultIfNull(entity.getParentId(), 0L));
		save(entity);

		Long menuId = entity.getId();
		return String.valueOf(menuId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(ObjectUtil.defaultIfNull(entity.getParentId(), 0L));
		updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Long menuId, SysMenuMoveForm form) {
		SysMenuEntity existing = menuReferenceChecker.getExisting(menuId);
		long newParentId = menuReferenceChecker.normalizeAndRequireParent(form.getParentId());

		if (TreeParentIdUtil.normalize(existing.getParentId()) == newParentId) {
			return;
		}

		menuReferenceChecker.requireMoveTargetValid(menuId, newParentId);
		existing.setParentId(newParentId);
		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysMenuBatchStatusForm form) {
		Boolean status = form.getStatus();
		List<SysMenuEntity> list = form.getIds().stream().filter(Objects::nonNull).map(id -> {
			SysMenuEntity sysMenuEntity = new SysMenuEntity();
			sysMenuEntity.setId(id);
			sysMenuEntity.setStatus(status);
			return sysMenuEntity;
		}).toList();
		super.updateBatchById(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		Long blockedParentId = baseMapper.selectFirstBlockedParentId(idsToDelete);
		if (blockedParentId != null) {
			throw new SystemBusinessException(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN, blockedParentId);
		}

		removeByIds(idsToDelete);
	}

}
