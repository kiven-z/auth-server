package com.auth.service.system.admin.export;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.module.file.export.scope.FileExportScopeSnapshots;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.export.fetcher.ExportPageFetcher;
import com.auth.service.system.admin.export.fetcher.ExportPageFetcherRegistry;
import com.auth.service.system.admin.model.request.InnerExportPageRequest;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * 导出分页取数编排：按业务类型分发并恢复权限快照
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ExportPageDispatchService {

	private final ExportPageFetcherRegistry fetcherRegistry;

	/**
	 * 按请求分发取数（恢复权限快照后路由到对应 Fetcher）
	 * @param request 内部分页取数请求
	 * @return 分页数据
	 */
	public ExportPageDataDTO fetchPage(InnerExportPageRequest request) {
		String bizType = CharSequenceUtil.trimToNull(request.getBizType());
		if (bizType == null) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "bizType");
		}

		ExportPageFetcher<PageQueryRequest> fetcher = fetcherRegistry.get(bizType.toUpperCase());
		PageQueryRequest query = parseQuery(request.getQuerySnapshotJson(), fetcher.queryType());
		AuthProfile profile = FileExportScopeSnapshots.requireAuthProfile(request.getScopeSnapshotJson());

		IPage<Map<String, Object>> page = executeWithScope(profile,
				() -> fetcher.fetchPage(query, request.getPageNum(), request.getPageSize()));
		return ExportPageDataDTO.builder().records(page.getRecords()).total(page.getTotal()).build();
	}

	private PageQueryRequest parseQuery(String querySnapshotJson, Class<?> queryType) {
		if (CharSequenceUtil.isBlank(querySnapshotJson)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "querySnapshotJson");
		}
		try {
			return (PageQueryRequest) JsonSupport.fromJson(querySnapshotJson, queryType);
		}
		catch (IllegalArgumentException exception) {
			log.warn("Invalid querySnapshotJson: {}", querySnapshotJson, exception);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "querySnapshotJson");
		}
	}

	private <T> T executeWithScope(AuthProfile profile, ScopedOperation<T> operation) {
		SecurityContext originalContext = SecurityContextHolder.getContext();
		SecurityContext newContext = SecurityContextHolder.createEmptyContext();
		newContext.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, Collections.emptyList()));
		SecurityContextHolder.setContext(newContext);
		try {
			return operation.execute();
		}
		finally {
			SecurityContextHolder.setContext(originalContext);
		}
	}

	@FunctionalInterface
	private interface ScopedOperation<T> {

		T execute();

	}

}
