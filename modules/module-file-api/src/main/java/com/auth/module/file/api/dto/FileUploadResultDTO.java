package com.auth.module.file.api.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果（跨模块契约）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResultDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private StoragePlatformEnum storagePlatform;

	private String uploadMode;

	private String bucket;

	private String objectKey;

	private String url;

	private String originalName;

	private String extension;

	private String contentType;

	private Long size;

	private String etag;

	private String bizType;

	private String bizId;

	private String remark;

	private Long createUser;

	private Long updateUser;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String createUsername;

	private String updateUsername;

}
