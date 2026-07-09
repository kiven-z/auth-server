package com.auth.module.file.api.model.enums;

import lombok.Getter;

/**
 * 文件上传模式枚举
 *
 * @author Bunny
 */
@Getter
public enum FileUploadMode {

	/**
	 * 简单直传
	 */
	SIMPLE("SIMPLE");

	/**
	 * 模式编码
	 */
	private final String code;

	/**
	 * 构造文件上传模式枚举
	 * @param code 模式编码
	 */
	FileUploadMode(String code) {
		this.code = code;
	}

}
