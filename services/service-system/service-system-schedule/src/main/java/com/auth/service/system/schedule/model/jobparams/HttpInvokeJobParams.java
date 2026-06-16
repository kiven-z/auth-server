package com.auth.service.system.schedule.model.jobparams;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * HTTP 内置任务 {@code job_params} 载荷
 *
 * @author Bunny
 */
@Schema(name = "HttpInvokeJobParams", title = "HTTP 调用任务参数")
@Getter
@Setter
public class HttpInvokeJobParams implements VersionedJobParams, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "载荷协议版本")
	@NotNull
	private Integer version = VersionedJobParams.VERSION_1;

	@Schema(title = "HTTP 方法", allowableValues = { "GET", "POST", "PUT", "DELETE" })
	@NotNull
	private HttpMethod method;

	@Schema(title = "请求 URL")
	@NotBlank
	private String url;

	@Schema(title = "请求头")
	private Map<String, String> headers;

	@Schema(title = "查询参数（拼接到 URL）")
	private Map<String, String> query;

	@Schema(title = "请求体（JSON 对象或数组；GET/DELETE 忽略）")
	private JsonNode body;

}
