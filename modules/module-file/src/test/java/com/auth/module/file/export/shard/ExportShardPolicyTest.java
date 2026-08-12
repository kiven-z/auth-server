package com.auth.module.file.export.shard;

import com.auth.module.file.exception.ExportRowLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportShardPolicy} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportShardPolicy 分片策略")
class ExportShardPolicyTest {

	@Test
	@DisplayName("assertWithinMaxRows：未超限时通过")
	void assertWithinMaxRows_whenWithinLimit_passes() {
		ExportShardPolicy policy = ExportShardPolicy.of(200_000L, 50_000);

		policy.assertWithinMaxRows(200_000L);
		policy.assertWithinMaxRows(1L);
	}

	@Test
	@DisplayName("assertWithinMaxRows：超限时抛人话异常")
	void assertWithinMaxRows_whenExceeded_throwsHumanMessage() {
		ExportShardPolicy policy = ExportShardPolicy.of(200_000L, 50_000);

		assertThatThrownBy(() -> policy.assertWithinMaxRows(200_001L))
			.isInstanceOf(ExportRowLimitExceededException.class)
			.hasMessage("最多导出 200000 行，当前约 200001 行，请缩小筛选范围后重试");
	}

	@Test
	@DisplayName("exceedsShardThreshold：超过阈值返回 true")
	void exceedsShardThreshold_whenAboveShardRows_returnsTrue() {
		ExportShardPolicy policy = ExportShardPolicy.of(200_000L, 50_000);

		assertThat(policy.exceedsShardThreshold(50_001L)).isTrue();
		assertThat(policy.exceedsShardThreshold(50_000L)).isFalse();
	}

	@Test
	@DisplayName("exceedsShardThreshold：shardRows<=0 视为不分片")
	void exceedsShardThreshold_whenShardRowsDisabled_returnsFalse() {
		ExportShardPolicy policy = ExportShardPolicy.of(200_000L, 0);

		assertThat(policy.exceedsShardThreshold(999_999L)).isFalse();
		assertThat(policy.shardRows()).isZero();
	}

}
