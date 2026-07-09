package com.auth.module.file.api.model.dto;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件引用（跨模块契约）
 *
 * @author Bunny
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileReferenceDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 文件记录 ID
	 */
	private Long id;

	/**
	 * 访问 URL
	 */
	private String url;

	/**
	 * MIME 类型
	 */
	private String contentType;

	/**
	 * 原始文件名
	 */
	private String originalName;

	/**
	 * 文件大小（字节）
	 */
	private Long size;

	/**
	 * 存储平台
	 */
	private StoragePlatformEnum storagePlatform;

}
