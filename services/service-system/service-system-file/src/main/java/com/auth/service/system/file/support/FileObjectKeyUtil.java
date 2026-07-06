package com.auth.service.system.file.support;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件对象键工具。
 *
 * @author Bunny
 */
@UtilityClass
public class FileObjectKeyUtil {

	/**
	 * 公开对象键前缀
	 */
	public static final String PUBLIC_PREFIX = "public/";

	/**
	 * 私有对象键前缀
	 */
	public static final String PRIVATE_PREFIX = "private/";

	/**
	 * 生成对象键，避免同名覆盖。
	 * @param bizTypeCode 业务类型编码
	 * @param isPrivate 是否私有
	 * @param originalFilename 原始文件名
	 * @return 对象键
	 */
	public static String build(String bizTypeCode, boolean isPrivate, String originalFilename) {
		String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

		String extension = FileUtil.extName(originalFilename);
		String randomName = UUID.fastUUID().toString(true);
		String filename = CharSequenceUtil.isBlank(extension) ? randomName : randomName + "." + extension;

		String visibilityPrefix = isPrivate ? PRIVATE_PREFIX : PUBLIC_PREFIX;
		String normalizedBizType = ReUtil.replaceAll(CharSequenceUtil.trim(bizTypeCode), "[^a-zA-Z0-9_-]", "_");

		return visibilityPrefix + normalizedBizType + "/" + dateDir + "/" + filename;
	}

	/**
	 * 按目标私有性切换对象键可见性前缀，保留 bizType/date/uuid 部分不变。
	 * @param objectKey 当前对象键
	 * @param targetPrivate 目标是否私有
	 * @return 切换前缀后的对象键
	 */
	public static String switchVisibilityPrefix(String objectKey, boolean targetPrivate) {
		String targetPrefix = targetPrivate ? PRIVATE_PREFIX : PUBLIC_PREFIX;
		if (objectKey.startsWith(PUBLIC_PREFIX)) {
			return targetPrefix + objectKey.substring(PUBLIC_PREFIX.length());
		}
		if (objectKey.startsWith(PRIVATE_PREFIX)) {
			return targetPrefix + objectKey.substring(PRIVATE_PREFIX.length());
		}
		throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, "objectKey");
	}

}
