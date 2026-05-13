package com.auth.service.system.admin.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.SysMenuConverter;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.admin.service.SysMenuService;
import com.auth.service.system.common.feign.AuthenticationInnerFeignClient;
import com.auth.service.system.common.feign.dto.EffectiveCodesInnerDTO;
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

import static com.auth.common.web.resttemplate.FeignUtil.isSuccess;

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

	private final AuthenticationInnerFeignClient authenticationInnerFeignClient;

	private final MenuWebRouteTreeBuilder menuWebRouteTreeBuilder;

	public SysMenuServiceImpl(AuditUserDisplayService auditUserDisplayService,
			AuthenticationInnerFeignClient authenticationInnerFeignClient,
			MenuWebRouteTreeBuilder menuWebRouteTreeBuilder) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.authenticationInnerFeignClient = authenticationInnerFeignClient;
		this.menuWebRouteTreeBuilder = menuWebRouteTreeBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RouteNodeVO> listWebRoutes(Long userId) {
		// 用户Id不能为空
		if (userId == null) {
			return Collections.emptyList();
		}

		// 查询当前用户角色
		Result<EffectiveCodesInnerDTO> result = authenticationInnerFeignClient.getEffectiveCodes(userId);
		if (BooleanUtil.isFalse(isSuccess(result))) {
			log.warn("Menu web route: effective codes unavailable userId={}", userId);
			return Collections.emptyList();
		}

		// 判断当前用户是否是管理员
		EffectiveCodesInnerDTO data = result.getData();
		List<String> roleCodes = data.getRoleCodes();
		List<String> permissionCodes = data.getPermissionCodes();
		boolean isAdmin = PermissionConstant.isAdmin(userId, roleCodes);
		log.debug("Menu web route: userId={} admin={} roleCount={} permissionCount={}", userId, isAdmin,
				roleCodes.size(), permissionCodes.size());

		List<SysMenuEntity> menuList = list(
				Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getStatus, Boolean.TRUE));

		// 管理员走管理员的菜单，非管理员走非管理员原生的菜单
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
	public void batchUpdateStatus(SysMenuBatchStatusForm form) {
		Boolean status = form.getStatus();
		List<SysMenuEntity> list = form.getIds().stream().filter(Objects::nonNull).map(id -> {
			SysMenuEntity sysMenuEntity = new SysMenuEntity();
			sysMenuEntity.setId(id);
			sysMenuEntity.setStatus(status);
			return sysMenuEntity;
		}).toList();
		updateBatchById(list);
	}

}
