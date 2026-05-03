package com.auth.service.system.admin.menu.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.menu.feign.AuthInnerPrincipalFeignClient;
import com.auth.service.system.admin.menu.feign.dto.EffectiveCodesVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@link AuthInnerPrincipalFeignClient} 降级：返回空编码列表，避免门户路由接口整体失败
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthInnerPrincipalFeignClientFallback implements AuthInnerPrincipalFeignClient {

	@Override
	public Result<EffectiveCodesVO> getEffectiveCodes(Long userId) {
		log.error("AuthInnerPrincipalFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesVO());
	}

}
