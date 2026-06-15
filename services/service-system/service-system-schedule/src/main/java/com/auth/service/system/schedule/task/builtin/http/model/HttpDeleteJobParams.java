package com.auth.service.system.schedule.task.builtin.http.model;

import com.auth.service.system.schedule.task.builtin.model.VersionedJobParams;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * HTTP DELETE 内置任务 {@code job_params} 载荷
 *
 * @author Bunny
 */
@Schema(name = "HttpDeleteJobParams", title = "HTTP DELETE 任务参数")
@Getter
@Setter
public class HttpDeleteJobParams implements VersionedJobParams, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "载荷协议版本")
	@NotNull
	private Integer version = VersionedJobParams.VERSION_1;

	@Schema(title = "请求 URL")
	@NotBlank
	private String url;

	@Schema(title = "请求头")
	private Map<String, String> headers;

}
