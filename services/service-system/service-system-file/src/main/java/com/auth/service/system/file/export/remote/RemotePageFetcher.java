package com.auth.service.system.file.export.remote;

import cn.hutool.core.net.url.UrlBuilder;
import com.auth.common.core.model.response.Result;
import com.auth.common.core.utils.JsonSupport;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.module.file.api.model.dto.ExportPageDataDTO;
import com.auth.module.file.exception.ExportExecutionException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程分页取数适配器（通过 HTTP 回调业务服务内部接口）
 *
 * @author Bunny
 */
@Slf4j
@Component
public class RemotePageFetcher {

	private final RestTemplate loadBalancedRestTemplate;

	public RemotePageFetcher(@Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate) {
		this.loadBalancedRestTemplate = loadBalancedRestTemplate;
	}

	/**
	 * 远程拉取分页数据
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @param bizType 业务类型
	 * @param querySnapshotJson 查询条件快照
	 * @param scopeSnapshotJson 权限快照
	 * @param pageNum 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 分页数据
	 */
	public IPage<Map<String, Object>> fetchPage(String fetchService, String fetchPath, String bizType,
			String querySnapshotJson, String scopeSnapshotJson, int pageNum, int pageSize) {
		String url = UrlBuilder.of().setScheme("http").setHost(fetchService).addPath(fetchPath).build();

		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.put("X-Internal-Call", "true");

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("bizType", bizType);
		requestBody.put("querySnapshotJson", querySnapshotJson);
		requestBody.put("scopeSnapshotJson", scopeSnapshotJson);
		requestBody.put("pageNum", pageNum);
		requestBody.put("pageSize", pageSize);

		try {
			ResponseEntity<String> response = RestTemplateUtil.exchange(loadBalancedRestTemplate, url, HttpMethod.POST,
					headers, JsonSupport.toJson(requestBody));

			String responseBody = response.getBody();
			if (responseBody == null) {
				throw ExportExecutionException.remoteFetchFailed("Response body is empty");
			}

			Result<ExportPageDataDTO> result = parseExportPageResult(response, responseBody, fetchService, fetchPath,
					bizType, pageNum);

			if (!FeignUtil.isSuccessWithData(result)) {
				throw ExportExecutionException.remoteFetchFailed(
						"Remote fetch failed: code=" + result.getCode() + ", message=" + result.getMessage());
			}

			ExportPageDataDTO data = result.getData();
			Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, data.getTotal());
			page.setRecords(data.getRecords());
			return page;
		}
		catch (ExportExecutionException exception) {
			throw exception;
		}
		catch (Exception exception) {
			log.error("Remote page fetch failed: service={}, path={}, bizType={}, pageNum={}", fetchService, fetchPath,
					bizType, pageNum, exception);
			throw ExportExecutionException.remoteFetchFailed(exception.getMessage());
		}
	}

	/**
	 * 解析远程分页响应
	 * @param response HTTP 响应
	 * @param responseBody 响应体
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @param bizType 业务类型
	 * @param pageNum 页码
	 * @return 解析后的业务结果
	 */
	private Result<ExportPageDataDTO> parseExportPageResult(ResponseEntity<String> response, String responseBody,
			String fetchService, String fetchPath, String bizType, int pageNum) {
		try {
			return JsonSupport.fromJson(responseBody, Result.class, ExportPageDataDTO.class);
		}
		catch (IllegalArgumentException exception) {
			MediaType contentType = response.getHeaders().getContentType();
			String preview = responseBody.length() > 256 ? responseBody.substring(0, 256) : responseBody;
			log.error(
					"Remote page fetch JSON parse failed: service={}, path={}, bizType={}, pageNum={}, contentType={}, bodyPreview={}",
					fetchService, fetchPath, bizType, pageNum, contentType, preview, exception);
			throw ExportExecutionException.remoteFetchFailed(exception.getMessage());
		}
	}

}