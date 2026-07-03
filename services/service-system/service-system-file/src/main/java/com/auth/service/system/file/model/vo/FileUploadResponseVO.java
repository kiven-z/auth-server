package com.auth.service.system.file.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件上传响应
 *
 * @author Bunny
 */
@Schema(name = "FileUploadResponseVO", title = "文件上传响应")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FileUploadResponseVO extends BaseResponse {

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "存储桶名称")
	private String bucket;

	@Schema(title = "对象键")
	private String objectKey;

	@Schema(title = "访问URL")
	private String url;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "扩展名")
	private String extension;

	@Schema(title = "文件类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "对象ETag")
	private String etag;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

	@Schema(title = "备注")
	private String remark;

}
