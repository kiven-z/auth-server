package com.auth.service.auth.authorization.domain.invalidation.model;

/**
 * 授权失效执行结果摘要。
 *
 * @param impactedUserCount 影响面反查得到的去重用户数
 * @param versionBumpedCount 实际递增 {@code perm_version} 的用户数
 * @param profileRefreshedCount 成功重建并写入 Redis 画像的用户数
 * @param profileEvictedCount 成功驱逐 Redis 画像的用户数
 * @author Bunny
 */
public record InvalidationOutcome(int impactedUserCount, int versionBumpedCount, int profileRefreshedCount,
		int profileEvictedCount) {

	/**
	 * 无受影响用户时的结果。
	 * @return 零影响结果
	 */
	public static InvalidationOutcome empty() {
		return new InvalidationOutcome(0, 0, 0, 0);
	}

}
