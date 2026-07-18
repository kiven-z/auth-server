package com.auth.service.system.message.support.recipient;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.RecipientUserMapper;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_RECIPIENT_SCOPE_INVALID;

/**
 * 接收人分批扫描：按范围游标推进，避免一次性加载全量用户 ID
 *
 * @author Bunny
 */
@Component
public class RecipientBatchScanner {

	private final RecipientUserMapper recipientUserMapper;

	public RecipientBatchScanner(RecipientUserMapper recipientUserMapper) {
		this.recipientUserMapper = recipientUserMapper;
	}

	/**
	 * 基于 {@code user_id > lastUserId} 分批查询并消费
	 * @param batchSize 每批上限
	 * @param queryAfter 查询函数：(lastUserId, limit) → 本批 userId（升序）
	 * @param consumer 每批消费
	 */
	private static void scanByCursor(int batchSize, BiFunction<Long, Integer, List<Long>> queryAfter,
			Consumer<List<Long>> consumer) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException("batchSize must be positive");
		}
		long lastUserId = 0L;
		while (true) {
			List<Long> batch = queryAfter.apply(lastUserId, batchSize);
			if (CollUtil.isEmpty(batch)) {
				return;
			}
			consumer.accept(batch);
			lastUserId = batch.get(batch.size() - 1);
		}
	}

	/**
	 * USER/POST/DEPT 必须带范围 ID
	 * @param scope 接收范围
	 */
	private static void requireScopeIds(RecipientScope scope) {
		if (scope.getType() == RecipientScopeType.ALL) {
			return;
		}
		if (CollUtil.isEmpty(scope.safeIds())) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, scope.getType().name());
		}
	}

	/**
	 * 按范围分批扫描启用用户 ID
	 * @param scope 接收范围
	 * @param batchSize 每批上限
	 * @param consumer 每批 userId（非空、升序）
	 */
	public void scan(RecipientScope scope, int batchSize, Consumer<List<Long>> consumer) {
		// 校验 scope 非空
		if (scope == null || scope.getType() == null) {
			throw new MessageException(IN_APP_RECIPIENT_SCOPE_INVALID, "scope");
		}

		RecipientScopeType type = scope.getType();
		requireScopeIds(scope);
		BiFunction<Long, Integer, List<Long>> queryAfter = resolveQuery(scope, type);
		scanByCursor(batchSize, queryAfter, consumer);
	}

	/**
	 * 按范围类型选择游标查询
	 * @param scope 接收范围
	 * @param type 范围类型
	 * @return (lastUserId, limit) → 本批 userId
	 */
	private BiFunction<Long, Integer, List<Long>> resolveQuery(RecipientScope scope, RecipientScopeType type) {
		List<Long> ids = scope.safeIds();
		return switch (type) {
			case USER -> (lastUserId, limit) -> Objects
				.requireNonNullElse(recipientUserMapper.selectEnabledUserIdsAfter(ids, lastUserId, limit), List.of());
			case POST -> (lastUserId, limit) -> Objects
				.requireNonNullElse(recipientUserMapper.selectUserIdsByPostIdsAfter(ids, lastUserId, limit), List.of());
			case DEPT ->
				scope.includeChildrenOrDefault()
						? (lastUserId,
								limit) -> Objects.requireNonNullElse(recipientUserMapper
									.selectUserIdsByDeptIdsWithChildrenAfter(ids, lastUserId, limit), List.of())
						: (lastUserId, limit) -> Objects.requireNonNullElse(
								recipientUserMapper.selectUserIdsByDeptIdsAfter(ids, lastUserId, limit), List.of());
			case ALL -> (lastUserId, limit) -> Objects
				.requireNonNullElse(recipientUserMapper.selectAllEnabledUserIdsAfter(lastUserId, limit), List.of());
		};
	}

}
