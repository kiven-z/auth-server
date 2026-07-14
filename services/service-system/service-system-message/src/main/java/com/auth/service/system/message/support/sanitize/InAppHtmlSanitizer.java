package com.auth.service.system.message.support.sanitize;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 站内信 HTML 白名单消毒
 *
 * @author Bunny
 */
@UtilityClass
public class InAppHtmlSanitizer {

	/**
	 * 站内信允许的文字样式属性（对齐 wangEditor color）
	 */
	private static final Set<String> ALLOWED_STYLE_PROPERTIES = Set.of("color", "background-color");

	/**
	 * 安全色值：具名色 / hex / rgb / rgba
	 */
	private static final Pattern SAFE_COLOR_VALUE = Pattern.compile(
			"^(#[0-9a-fA-F]{3,8}|rgba?\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}\\s*,\\s*\\d{1,3}(?:\\s*,\\s*[\\d.]+)?\\s*\\)|[a-zA-Z]+)$");

	/**
	 * 站内信 HTML 白名单
	 */
	private static final Safelist IN_APP_SAFELIST = Safelist.none()
		.addTags("p", "br", "ul", "ol", "li", "a", "strong", "em", "img", "span", "h1", "h2", "h3", "h4", "h5", "h6")
		.addAttributes("a", "href", "title", "rel", "target")
		.addAttributes("img", "src", "alt")
		.addAttributes("span", "style")
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
		String cleaned = Jsoup.clean(html, "", IN_APP_SAFELIST, OUTPUT_SETTINGS);
		return filterSpanStyles(cleaned);
	}

	/**
	 * 仅保留 span.style 中的安全 color / background-color
	 * @param html 白名单清洁后的 HTML
	 * @return 样式二次过滤后的 HTML
	 */
	private static String filterSpanStyles(String html) {
		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings(OUTPUT_SETTINGS);
		for (Element span : document.select("span[style]")) {
			String safeStyle = keepSafeColorStyles(span.attr("style"));
			if (CharSequenceUtil.isBlank(safeStyle)) {
				span.removeAttr("style");
			}
			else {
				span.attr("style", safeStyle);
			}
		}
		return document.body().html();
	}

	/**
	 * 从 style 声明中筛出安全文字色
	 * @param style 原始 style 字符串
	 * @return 仅含允许属性的 style；无可保留项时返回空串
	 */
	private static String keepSafeColorStyles(String style) {
		if (CharSequenceUtil.isBlank(style)) {
			return "";
		}
		List<String> kept = new ArrayList<>(2);
		for (String declaration : style.split(";")) {
			appendSafeColorDeclaration(kept, declaration);
		}
		return String.join("; ", kept);
	}

	/**
	 * 解析单条 style 声明，通过则写入列表
	 * @param kept 已通过的声明
	 * @param declaration 单条 prop:value
	 */
	private static void appendSafeColorDeclaration(List<String> kept, String declaration) {
		String trimmed = declaration.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		int colonIndex = trimmed.indexOf(':');
		if (colonIndex <= 0) {
			return;
		}
		String property = trimmed.substring(0, colonIndex).trim().toLowerCase(Locale.ROOT);
		String value = trimmed.substring(colonIndex + 1).trim();
		if (!ALLOWED_STYLE_PROPERTIES.contains(property) || !SAFE_COLOR_VALUE.matcher(value).matches()) {
			return;
		}
		kept.add(property + ": " + value);
	}

}
