package com.auth.service.system.authorization.model.constants;

import lombok.experimental.UtilityClass;

/**
 * authorization 模块批量处理批次大小
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationBatchSizes {

	/**
	 * Outbox 补偿扫描每批抢占条数
	 */
	public static final int OUTBOX_POLL = 50;

	/**
	 * SUCCESS Outbox 历史清理每批上限
	 */
	public static final int SUCCESS_CLEANUP = 500;

}
