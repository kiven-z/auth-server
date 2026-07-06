package com.auth.service.system.file.storage.platform.aliyun;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.file.storage.core.capability.ImageStyleUrlCapability;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 图片样式 URL 能力
 *
 * <p>
 * 拼接 {@code x-oss-process=style/{style}}；已有查询串时用 {@code &} 追加 保持纯 URL 拼接以避免依赖 OSS SDK
 * </p>
 *
 * @author Bunny
 */
@Component
public class AliyunImageStyleUrlCapability implements ImageStyleUrlCapability {

	/**
	 * 阿里云 OSS 图片样式参数名
	 */
	private static final String STYLE_PARAM = "x-oss-process=style/";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildStyleUrl(String publicUrl, String style) {
		if (CharSequenceUtil.isBlank(publicUrl) || CharSequenceUtil.isBlank(style)) {
			return publicUrl;
		}
		String separator = publicUrl.indexOf('?') >= 0 ? "&" : "?";
		return publicUrl + separator + STYLE_PARAM + style;
	}

}
