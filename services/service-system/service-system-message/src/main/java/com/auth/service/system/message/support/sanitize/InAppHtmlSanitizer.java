package com.auth.service.system.message.support.sanitize;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 站内信 HTML 白名单消毒
 *
 * @author Bunny
 */
@UtilityClass
public class InAppHtmlSanitizer {

	/**
	 * 站内信 HTML 白名单
	 */
	private static final Safelist IN_APP_SAFELIST = Safelist.none()
		.addTags("p", "br", "ul", "ol", "li", "a", "strong", "em", "img")
		.addAttributes("a", "href", "title", "rel", "target")
		.addAttributes("img", "src", "alt")
		.addProtocols("a", "href", "http", "https", "mailto")
		.addProtocols("img", "src", "http", "https");

	private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

	/**
	 * 按白名单消毒 HTML；null 原样返回，空白返回空串
	 * @param html 原始 HTML
	 * @return 消毒后的 HTML
	 */
	public static String sanitize(String html) {
		if (html == null) {
			return null;
		}
		if (CharSequenceUtil.isBlank(html)) {
			return "";
		}
		return Jsoup.clean(html, "", IN_APP_SAFELIST, OUTPUT_SETTINGS);
	}

}
