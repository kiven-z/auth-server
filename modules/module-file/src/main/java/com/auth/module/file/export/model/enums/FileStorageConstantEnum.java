package com.auth.module.file.export.model.enums;

import com.auth.module.file.exception.FileStorageException;
import lombok.Getter;

import java.io.File;

/**
 * 文件存储常量
 *
 * @author Bunny
 */
@Getter
public enum FileStorageConstantEnum {

	/**
	 * favicon 图标文件
	 */
	FAVICON("favicon"),

	/**
	 * 头像文件
	 */
	AVATAR("avatar"),

	/**
	 * 消息文件
	 */
	MESSAGE("messages"),

	/**
	 * 轮播文件
	 */
	CAROUSEL("carousel"),

	/**
	 * 备份文件
	 */
	BACKUP("backup"),

	/**
	 * 图片文件
	 */
	IMAGES("images"),

	/**
	 * 视频文件
	 */
	VIDEO("video"),

	/**
	 * 默认文件
	 */
	DEFAULT("default"),;

	private final String typeName;

	private final String path;

	FileStorageConstantEnum(String typeName) {
		this.typeName = typeName;
		this.path = File.separator + typeName + File.separator;
	}

	/**
	 * 根据传入的类型字符串获取对应的枚举值
	 * @param typeName 类型字符串，用于查找对应的枚举
	 * @return 返回对应枚举的路径值
	 * @throws FileStorageException 当传入的类型不存在时抛出异常
	 */
	public static String getType(String typeName) {
		for (FileStorageConstantEnum constant : values()) {
			if (constant.getTypeName().equals(typeName)) {
				return constant.getPath();
			}
		}
		// 如果找不到对应的枚举，尝试使用 DEFAULT
		if (!typeName.equals(DEFAULT.typeName)) {
			return DEFAULT.getPath();
		}
		throw new FileStorageException("上传类型错误或缺失", null);
	}

	/**
	 * 直接通过枚举值获取路径（更安全的方式）
	 */
	public static String getType(FileStorageConstantEnum constant) {
		return constant.getPath();
	}

}