package com.auth.service.system.file.export.precheck;

import com.auth.module.file.api.model.dto.ExportPreCheckResult;
import com.auth.module.file.api.model.request.ExportPreCheckRequest;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.file.export.shard.ExportShardPolicy;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 导出前置检查：估算行数并判断产物格式
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileExportPreCheckService {

	private final RemotePageFetcher remotePageFetcher;

	private final FileExportTaskProperties properties;

	/**
	 * 执行前置检查（取首页 total，对比分片阈值与总上限）
	 * @param request 检查请求
	 * @return 检查结果
	 */
	public ExportPreCheckResult preCheck(ExportPreCheckRequest request) {
		AuthProfile profile = SecurityUserUtils.currentAuthProfile();
		if (profile == null || profile.getUserId() == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "authProfile");
		}

		String scopeSnapshotJson = FileExportScopeSnapshots.toJson(profile);
		String fetchService = request.getFetchService();
		String fetchPath = request.getFetchPath();
		String bizType = request.getBizType();
		String querySnapshotJson = request.getQuerySnapshotJson();
		IPage<Map<String, Object>> page = remotePageFetcher.fetchPage(fetchService, fetchPath, bizType,
				querySnapshotJson, scopeSnapshotJson, 1, 1);

		ExportShardPolicy policy = ExportShardPolicy.of(properties.getMaxRows(), properties.getShardRows());
		long totalRows = page.getTotal();
		long maxRows = policy.maxRows();
		if (totalRows > maxRows) {
			return ExportPreCheckResult.exceeded(totalRows, maxRows);
		}
		if (policy.exceedsShardThreshold(totalRows)) {
			return ExportPreCheckResult.sharded(totalRows, maxRows, policy.shardCount(totalRows));
		}
		return ExportPreCheckResult.normal(totalRows, maxRows);
	}

}
