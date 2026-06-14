package com.auth.service.auth.infrastructure.persistence.adapter;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.auth.application.port.invalidation.UserPermVersionPort;
import com.auth.service.auth.infrastructure.persistence.UserPermVersionMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户权限版本递增 Port 适配器
 *
 * @author Bunny
 */
@Repository
public class UserPermVersionAdapter implements UserPermVersionPort {

	private final UserPermVersionMapper userPermVersionMapper;

	public UserPermVersionAdapter(UserPermVersionMapper userPermVersionMapper) {
		this.userPermVersionMapper = userPermVersionMapper;
	}

	/**
	 * 分批处理用户ID集合。
	 * @param ids 用户ID集合
	 * @param batchSize 每批大小
	 * @return 分批后的用户ID集合
	 */
	private static List<List<Long>> partition(List<Long> ids, int batchSize) {
		List<List<Long>> batches = new ArrayList<>();
		for (int index = 0; index < ids.size(); index += batchSize) {
			int end = Math.min(index + batchSize, ids.size());
			batches.add(ids.subList(index, end));
		}
		return batches;
	}

	@Override
	public int incrementInBatches(Collection<Long> userIds, int batchSize) {
		// 如果用户ID集合为空或批量大小小于等于0，则返回0
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return 0;
		}

		// 分批递增用户权限版本
		int totalUpdated = 0;
		for (List<Long> batch : partition(ids, batchSize)) {
			totalUpdated += userPermVersionMapper.incrementPermVersionByUserIds(batch);
		}
		return totalUpdated;
	}

}
