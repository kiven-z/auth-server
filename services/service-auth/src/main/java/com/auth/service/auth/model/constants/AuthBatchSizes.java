package com.auth.service.auth.model.constants;

import lombok.experimental.UtilityClass;

/**
 * auth 模块批量处理批次大小
 *
 * @author Bunny
 */
@UtilityClass
public class AuthBatchSizes {

	/**
	 * 授权失效：版本递增、画像刷新/驱逐、影响面查询分片
	 */
	public static final int INVALIDATION = 500;

	/**
	 * processing 占位超时清理每批上限
	 */
	public static final int PROCESSING_CLAIM_CLEANUP = 100;

	/**
	 * 已完成幂等事件历史清理每批上限
	 */
	public static final int SUCCESS_CLEANUP = 500;

}
