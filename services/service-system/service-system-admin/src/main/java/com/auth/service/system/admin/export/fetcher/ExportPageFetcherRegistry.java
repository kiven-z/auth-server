package com.auth.service.system.admin.export.fetcher;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导出分页取数器注册表
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class ExportPageFetcherRegistry {

	private final List<ExportPageFetcher<?>> fetchers;

	private Map<String, ExportPageFetcher<?>> fetcherMap;

	@PostConstruct
	public void init() {
		this.fetcherMap = fetchers.stream().collect(Collectors.toMap(ExportPageFetcher::bizType, Function.identity()));
		log.info("ExportPageFetcherRegistry initialized with {} fetchers: {}", fetcherMap.size(), fetcherMap.keySet());
	}

	/**
	 * 按业务类型获取取数器
	 * @param bizType 业务类型
	 * @param <Q> 查询条件类型
	 * @return 取数器
	 */
	@SuppressWarnings("unchecked")
	public <Q extends PageQueryRequest> ExportPageFetcher<Q> get(String bizType) {
		ExportPageFetcher<?> fetcher = fetcherMap.get(bizType);
		if (fetcher == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID, "bizType");
		}
		return (ExportPageFetcher<Q>) fetcher;
	}

	/**
	 * 检查业务类型是否已注册
	 * @param bizType 业务类型
	 * @return 是否已注册
	 */
	public boolean contains(String bizType) {
		return fetcherMap.containsKey(bizType);
	}

}
