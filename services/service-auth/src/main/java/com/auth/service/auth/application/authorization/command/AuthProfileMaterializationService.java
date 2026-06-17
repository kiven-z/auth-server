package com.auth.service.auth.application.authorization.command;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.auth.service.auth.shared.support.BatchPartition;

import java.util.Collection;
import java.util.List;

/**
 * 授权画像物化：分批重建缓存或驱逐 Redis 画像
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthProfileMaterializationService {

	private final AuthProfileQueryPort authProfileQueryPort;

	private final AuthProfileCachePort authProfileCachePort;

	/**
	 * 分批重建并写入 Redis 画像。
	 * @param userIds 用户 ID
	 * @param batchSize 每批大小
	 * @return 成功刷新数量
	 */
	public int refreshInBatches(Collection<Long> userIds, int batchSize) {
		// 如果用户ID集合为空或批量大小小于等于0，则返回0
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		// 分批刷新用户授权画像
		int refreshed = 0;
		for (List<Long> batch : BatchPartition.partitionIds(userIds, batchSize)) {
			for (Long userId : batch) {
				// 刷新单个用户授权画像
				if (refreshOne(userId)) {
					refreshed++;
				}
			}
		}

		return refreshed;
	}

	/**
	 * 分批驱逐 Redis 画像。
	 * @param userIds 用户 ID
	 * @param batchSize 每批大小
	 * @return 成功驱逐数量
	 */
	public int evictInBatches(Collection<Long> userIds, int batchSize) {
		// 如果用户ID集合为空或批量大小小于等于0，则返回0
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		// 驱逐数量
		int evicted = 0;
		// 分批驱逐用户授权画像
		for (List<Long> batch : BatchPartition.partitionIds(userIds, batchSize)) {
			for (Long userId : batch) {
				if (evictOne(userId)) {
					evicted++;
				}
			}
		}
		return evicted;
	}

	/**
	 * 刷新单个用户授权画像。
	 * @param userId 用户ID
	 * @return 是否成功刷新
	 */
	private boolean refreshOne(Long userId) {
		// 如果用户ID为空或小于等于0，则返回false
		if (userId == null) {
			return false;
		}
		// 构建用户授权画像
		try {
			AuthProfile profile = authProfileQueryPort.buildByUserId(userId);
			authProfileCachePort.cacheProfile(profile);
			return true;
		}
		catch (RuntimeException ex) {
			log.warn("Failed to refresh auth profile for userId={}: {}", userId, ex.getMessage());
			return false;
		}
	}

	/**
	 * 驱逐单个用户授权画像。
	 * @param userId 用户ID
	 * @return 是否成功驱逐
	 */
	private boolean evictOne(Long userId) {
		// 如果用户ID为空或小于等于0，则返回false
		if (userId == null) {
			return false;
		}

		try {
			// 移除用户授权画像缓存
			authProfileCachePort.evictProfile(userId);
			return true;
		}
		catch (RuntimeException ex) {
			log.warn("Failed to evict auth profile for userId={}: {}", userId, ex.getMessage());
			return false;
		}
	}

}
