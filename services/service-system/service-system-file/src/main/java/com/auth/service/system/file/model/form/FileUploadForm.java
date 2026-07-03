package com.auth.service.system.file.model.form;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 单文件上传表单
 *
 * @author Bunny
 */
@Schema(name = "FileUploadForm", title = "文件上传表单")
@Getter
@Setter
public class FileUploadForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "上传文件", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "file 不能为空")
	private transient MultipartFile file;

	@Schema(title = "存储平台（MINIO / ALIYUN_OSS）")
	@Size(max = 32, message = "storagePlatform 长度不能超过32个字符")
	private String storagePlatform;

	@Schema(title = "业务类型，如 avatar、attachment")
	@Size(max = 64, message = "bizType 长度不能超过64个字符")
	private String bizType;

	@Schema(title = "业务主键ID")
	@Size(max = 64, message = "bizId 长度不能超过64个字符")
	private String bizId;

	@Schema(title = "备注")
	@Size(max = 500, message = "remark 长度不能超过500个字符")
	private String remark;

}
