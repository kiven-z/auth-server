package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.AuthInnerFeignClient;
import com.auth.service.system.authorization.feign.dto.EffectiveCodesInnerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.auth.common.web.resttemplate.FeignUtil.isSuccessWithData;

/**
 * 查询用户生效角色码与权限码（admin 经 trigger 访问 auth 内部能力）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class UserEffectiveCodesTrigger {

	private final AuthInnerFeignClient authInnerFeignClient;

	/**
	 * 解析用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 快照；不可用或失败时为空
	 */
	public Optional<UserEffectiveCodesSnapshot> resolve(Long userId) {
		if (userId == null) {
			return Optional.empty();
		}

		Result<EffectiveCodesInnerDTO> result = authInnerFeignClient.getEffectiveCodes(userId);
		if (BooleanUtil.isFalse(isSuccessWithData(result))) {
			log.warn("Effective codes unavailable: userId={}", userId);
			return Optional.empty();
		}

		EffectiveCodesInnerDTO data = result.getData();
		return Optional.of(new UserEffectiveCodesSnapshot(data.getRoleCodes(), data.getPermissionCodes()));
	}

}
