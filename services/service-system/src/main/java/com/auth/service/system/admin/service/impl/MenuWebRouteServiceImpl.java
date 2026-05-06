package com.auth.service.system.admin.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.mapper.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.RouteNodeVO;
import com.auth.service.system.admin.service.MenuWebRouteService;
import com.auth.service.system.admin.service.MenuWebRouteTreeBuilder;
import com.auth.service.system.common.feign.AuthenticationInnerFeignClient;
import com.auth.service.system.common.feign.dto.EffectiveCodesVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.auth.common.web.autoconfigure.FeignUtil.isOk;

/**
 * 门户动态路由：拉取 auth 生效编码后构建菜单路由树
 *
 * @author Bunny
 */
@Slf4j
@Service
public class MenuWebRouteServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements MenuWebRouteService {

	private final AuthenticationInnerFeignClient authenticationInnerFeignClient;

	private final MenuWebRouteTreeBuilder menuWebRouteTreeBuilder;

	public MenuWebRouteServiceImpl(AuthenticationInnerFeignClient authenticationInnerFeignClient,
			MenuWebRouteTreeBuilder menuWebRouteTreeBuilder) {
		this.authenticationInnerFeignClient = authenticationInnerFeignClient;
		this.menuWebRouteTreeBuilder = menuWebRouteTreeBuilder;
	}

	@Override
	public List<RouteNodeVO> listWebRoutes(Long userId) {
		// 用户Id不能为空
		if (userId == null) {
			return Collections.emptyList();
		}

		// 查询当前用户角色
		Result<EffectiveCodesVO> result = authenticationInnerFeignClient.getEffectiveCodes(userId);
		if (!isOk(result)) {
			log.warn("Menu web route: effective codes unavailable userId={}", userId);
			return Collections.emptyList();
		}

		// 判断当前用户是否是管理员
		EffectiveCodesVO data = result.getData();
		List<String> roleCodes = data.getRoleCodes();
		List<String> permissionCodes = data.getPermissionCodes();
		boolean isAdmin = PermissionConstant.isAdmin(userId, roleCodes);
		log.debug("Menu web route: userId={} admin={} roleCount={} permissionCount={}", userId, isAdmin,
				roleCodes.size(), permissionCodes.size());

		List<SysMenuEntity> menuList = list(Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getStatus, 0));

		// 管理员走管理员的菜单，非管理员走非管理员原生的菜单
		return isAdmin ? menuWebRouteTreeBuilder.buildAdminRoutes(menuList)
				: menuWebRouteTreeBuilder.buildRoutes(menuList);
	}

}
