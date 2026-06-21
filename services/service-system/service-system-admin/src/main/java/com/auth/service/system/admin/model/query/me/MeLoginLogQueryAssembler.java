package com.auth.service.system.admin.model.query.me;

import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

/**
 * 个人中心登录日志查询 → 管理端查询条件组装
 *
 * @author Bunny
 */
@UtilityClass
public class MeLoginLogQueryAssembler {

	/**
	 * 个人中心登录日志最大可查天数
	 */
	private static final int ME_LOGIN_LOG_MAX_DAYS = 180;

	/**
	 * 将个人中心查询转为管理端内部查询，并强制当前用户与时间窗口。
	 * @param userId 当前用户 ID
	 * @param query 个人中心查询条件
	 * @return 管理端登录日志查询
	 */
	public static LogLoginLogQuery toLogLoginLogQuery(Long userId, MeLoginLogPageQuery query) {
		LogLoginLogQuery internalQuery = new LogLoginLogQuery();
		internalQuery.setPageIndex(query.getPageIndex());
		internalQuery.setPageSize(query.getPageSize());
		internalQuery.setLoginType(query.getLoginType());
		internalQuery.setLoginResult(query.getLoginResult());
		internalQuery.setUserId(userId);

		LocalDateTime now = LocalDateTime.now();
		internalQuery.setLoginTimeStart(now.minusDays(ME_LOGIN_LOG_MAX_DAYS));
		internalQuery.setLoginTimeEnd(now);
		return internalQuery;
	}

}