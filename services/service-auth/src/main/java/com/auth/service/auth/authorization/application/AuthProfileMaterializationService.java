package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 授权画像物化：分批重建缓存或驱逐 Redis 画像
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthProfileMaterializationService {

	private final AuthProfileQueryService authProfileQueryService;

	private final AuthProfileCacheApplicationService authProfileCacheApplicationService;

	/**
	 * 分批处理用户ID集合
	 * @param userIds 用户ID集合
	 * @param batchSize 每批大小
	 * @return 分批后的用户ID集合
	 */
	private static List<List<Long>> partition(Collection<Long> userIds, int batchSize) {
		// 过滤掉空和负数
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();

		// 分批后的用户ID集合
		List<List<Long>> batches = new ArrayList<>();
		for (int index = 0; index < ids.size(); index += batchSize) {
			int end = Math.min(index + batchSize, ids.size());
			batches.add(ids.subList(index, end));
		}

		return batches;
	}

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
		for (List<Long> batch : partition(userIds, batchSize)) {
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
		for (List<Long> batch : partition(userIds, batchSize)) {
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
			AuthProfile profile = authProfileQueryService.buildByUserId(userId);
			authProfileCacheApplicationService.cacheProfile(profile);
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
			authProfileCacheApplicationService.evictProfile(userId);
			return true;
		}
		catch (RuntimeException ex) {
			log.warn("Failed to evict auth profile for userId={}: {}", userId, ex.getMessage());
			return false;
		}
	}

}
