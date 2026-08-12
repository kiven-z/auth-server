package com.auth.module.file.api.model.dto;

import com.auth.module.file.api.model.enums.ExportPreCheckStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出前置检查结果
 *
 * @author Bunny
 */
@Getter
@Setter
public class ExportPreCheckResult implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 检查状态
	 */
	private ExportPreCheckStatus status;

	/**
	 * 预估总行数
	 */
	private long totalRows;

	/**
	 * 最大可导出行数
	 */
	private long maxRows;

	/**
	 * 分片数（EXCEEDED 为 0，NORMAL 为 1，SHARDED 为实际分片数）
	 */
	private int shardCount;

	/**
	 * 产物格式（XLSX / ZIP；EXCEEDED 时为 null）
	 */
	private String exportFormat;

	/**
	 * 构造 NORMAL 结果（未超过分片阈值，单 xlsx）
	 * @param totalRows 预估总行数
	 * @param maxRows 最大可导出行数
	 * @return 检查结果
	 */
	public static ExportPreCheckResult normal(long totalRows, long maxRows) {
		ExportPreCheckResult result = new ExportPreCheckResult();
		result.setStatus(ExportPreCheckStatus.NORMAL);
		result.setTotalRows(totalRows);
		result.setMaxRows(maxRows);
		result.setShardCount(1);
		result.setExportFormat("XLSX");
		return result;
	}

	/**
	 * 构造 SHARDED 结果（超过分片阈值，多片 zip）
	 * @param totalRows 预估总行数
	 * @param maxRows 最大可导出行数
	 * @param shardCount 分片数
	 * @return 检查结果
	 */
	public static ExportPreCheckResult sharded(long totalRows, long maxRows, int shardCount) {
		ExportPreCheckResult result = new ExportPreCheckResult();
		result.setStatus(ExportPreCheckStatus.SHARDED);
		result.setTotalRows(totalRows);
		result.setMaxRows(maxRows);
		result.setShardCount(shardCount);
		result.setExportFormat("ZIP");
		return result;
	}

	/**
	 * 构造 EXCEEDED 结果（超过总上限，不可导出）
	 * @param totalRows 预估总行数
	 * @param maxRows 最大可导出行数
	 * @return 检查结果
	 */
	public static ExportPreCheckResult exceeded(long totalRows, long maxRows) {
		ExportPreCheckResult result = new ExportPreCheckResult();
		result.setStatus(ExportPreCheckStatus.EXCEEDED);
		result.setTotalRows(totalRows);
		result.setMaxRows(maxRows);
		result.setShardCount(0);
		result.setExportFormat(null);
		return result;
	}

}
