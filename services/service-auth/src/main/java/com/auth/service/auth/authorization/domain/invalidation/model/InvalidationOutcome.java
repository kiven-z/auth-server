package com.auth.service.auth.authorization.domain.invalidation.model;

/**
 * 授权失效执行结果摘要（后续编排层填充 bump/evict 计数，当前反查阶段仅含解析人数）。
 *
 * @param impactedUserCount 影响面反查得到的去重用户数
 * @author Bunny
 */
public record InvalidationOutcome(int impactedUserCount) {

	/**
	 * 无受影响用户时的结果。
	 * @return 零影响结果
	 */
	public static InvalidationOutcome empty() {
		return new InvalidationOutcome(0);
	}

}
