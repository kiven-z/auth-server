package com.auth.service.system.admin.menu.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.menu.feign.AuthInnerPrincipalFeignClient;
import com.auth.service.system.admin.menu.feign.dto.EffectiveCodesVO;
import com.auth.service.system.admin.menu.model.vo.RouteNodeVO;
import com.auth.service.system.admin.menu.service.MenuWebRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.auth.common.web.autoconfigure.FeignUtil.isOk;

/**
 * 门户动态路由查询实现：已接入 auth 生效编码 Feign，建树与菜单过滤后续迭代。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class MenuWebRouteServiceImpl implements MenuWebRouteService {

	private final AuthInnerPrincipalFeignClient authInnerPrincipalFeignClient;

	public MenuWebRouteServiceImpl(AuthInnerPrincipalFeignClient authInnerPrincipalFeignClient) {
		this.authInnerPrincipalFeignClient = authInnerPrincipalFeignClient;
	}

	/**
	 * 拉取生效角色/权限码后返回占位空树；后续在此拼接 sys_menu 过滤与 VO 映射。
	 * @param userId 用户 ID
	 * @return 当前为空列表
	 */
	@Override
	public List<RouteNodeVO> listWebRoutes(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}

		Result<EffectiveCodesVO> result = authInnerPrincipalFeignClient.getEffectiveCodes(userId);
		if (isOk(result)) {
			return Collections.emptyList();
		}

		EffectiveCodesVO data = result.getData();
		List<String> roleCodes = data.getRoleCodes();
		List<String> permissionCodes = data.getPermissionCodes();
		log.debug("Menu web route: effective roleCount={}, permissionCount={} (tree assembly pending)",
				roleCodes.size(), permissionCodes.size());
		return Collections.emptyList();
	}

}
