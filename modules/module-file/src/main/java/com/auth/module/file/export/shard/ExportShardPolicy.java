package com.auth.module.file.export.shard;

import com.auth.module.file.exception.ExportRowLimitExceededException;

/**
 * 导出分片策略（总上限与分片阈值）
 *
 * @author Bunny
 */
public record ExportShardPolicy(long maxRows, int shardRows) {

	public ExportShardPolicy {
		if (maxRows <= 0) {
			throw new IllegalArgumentException("maxRows must be positive");
		}
		if (shardRows < 0) {
			throw new IllegalArgumentException("shardRows must be >= 0");
		}
	}

	/**
	 * 构建分片策略
	 * @param maxRows 总行数上限
	 * @param shardRows 分片阈值（{@code <=0} 表示不分片）
	 * @return 策略
	 */
	public static ExportShardPolicy of(long maxRows, int shardRows) {
		return new ExportShardPolicy(maxRows, shardRows);
	}

	/**
	 * 断言总行数未超过上限
	 * @param totalRows 总行数
	 * @throws ExportRowLimitExceededException 超过上限
	 */
	public void assertWithinMaxRows(long totalRows) {
		if (totalRows > maxRows) {
			throw new ExportRowLimitExceededException(maxRows, totalRows);
		}
	}

	/**
	 * 是否超过分片阈值（需要切成多文件）
	 * @param totalRows 总行数
	 * @return 是否需要分片
	 */
	public boolean exceedsShardThreshold(long totalRows) {
		return shardRows > 0 && totalRows > shardRows;
	}

	/**
	 * 计算分片数（未超阈值时为 1）
	 * @param totalRows 总行数
	 * @return 分片数
	 */
	public int shardCount(long totalRows) {
		if (!exceedsShardThreshold(totalRows)) {
			return 1;
		}
		return (int) Math.ceil((double) totalRows / shardRows);
	}

}
