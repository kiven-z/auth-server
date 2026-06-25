package com.auth.service.auth.convert;

import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * 授权失效幂等事件统计 PO → VO；含 null 默认值，不使用 MapStruct。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationInvalidationEventStatsConverter {

	/**
	 * 统计 PO 转 VO
	 * @param statsPo 统计 PO
	 * @return 统计 VO
	 */
	public static AuthorizationInvalidationEventStatsVO toVo(AuthorizationInvalidationEventStatsPO statsPo) {
		AuthorizationInvalidationEventStatsPO safePo = Objects.requireNonNullElse(statsPo,
				new AuthorizationInvalidationEventStatsPO());
		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();

		statsVo.setTotalCount(Objects.requireNonNullElse(safePo.getTotalCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(safePo.getProcessingCount(), 0L));
		statsVo.setCompletedCount(Objects.requireNonNullElse(safePo.getCompletedCount(), 0L));
		return statsVo;
	}

}
