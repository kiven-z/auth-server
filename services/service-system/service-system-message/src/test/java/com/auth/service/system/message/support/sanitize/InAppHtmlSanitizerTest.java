package com.auth.service.system.message.support.sanitize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InAppHtmlSanitizer} 白名单消毒单元测试
 */
@DisplayName("InAppHtmlSanitizer 站内信 HTML 白名单消毒")
class InAppHtmlSanitizerTest {

	@Test
	@DisplayName("null 返回 null；空白返回空串")
	void sanitize_shouldHandleNullAndBlank() {
		// null 与空白输入边界
		assertThat(InAppHtmlSanitizer.sanitize(null)).isNull();
		assertThat(InAppHtmlSanitizer.sanitize("")).isEmpty();
		assertThat(InAppHtmlSanitizer.sanitize("   ")).isEmpty();
	}

	@Test
	@DisplayName("保留允许标签与安全协议属性")
	void sanitize_shouldKeepAllowedTagsAndSafeAttrs() {
		// 允许的结构与 http(s)/mailto 链接、图片
		String html = "<p>hello <strong>bold</strong> <em>i</em></p>" + "<ul><li>a</li></ul><ol><li>b</li></ol><br>"
				+ "<a href=\"https://example.com\" title=\"t\">link</a>" + "<a href=\"mailto:a@b.com\">mail</a>"
				+ "<img src=\"https://cdn.example.com/a.png\" alt=\"pic\">";

		String cleaned = InAppHtmlSanitizer.sanitize(html);

		assertThat(cleaned).contains("<p>", "<strong>", "<em>", "<ul>", "<ol>", "<li>", "<br>", "<a ", "<img ")
			.contains("href=\"https://example.com\"")
			.contains("href=\"mailto:a@b.com\"")
			.contains("src=\"https://cdn.example.com/a.png\"")
			.contains("alt=\"pic\"");
	}

	@Test
	@DisplayName("保留 h1-h6 标题标签")
	void sanitize_shouldKeepHeadingTags() {
		// 标题层级全文保留
		String html = "<h1>t1</h1><h2>t2</h2><h3>t3</h3><h4>t4</h4><h5>t5</h5><h6>t6</h6>";

		String cleaned = InAppHtmlSanitizer.sanitize(html);

		assertThat(cleaned).contains("<h1>t1</h1>", "<h2>t2</h2>", "<h3>t3</h3>", "<h4>t4</h4>", "<h5>t5</h5>",
				"<h6>t6</h6>");
	}

	@Test
	@DisplayName("保留 span 上安全的文字颜色与背景色")
	void sanitize_shouldKeepSafeSpanColorStyles() {
		// wangEditor 输出的 color / background-color
		String html = "<p><span style=\"color: rgb(225, 60, 57);\">red</span>"
				+ "<span style=\"background-color: #ffff00; color: blue\">hl</span></p>";

		String cleaned = InAppHtmlSanitizer.sanitize(html);

		assertThat(cleaned).contains("<span")
			.contains("color: rgb(225, 60, 57)")
			.contains("background-color: #ffff00")
			.contains("color: blue")
			.contains("red")
			.contains("hl");
	}

	@Test
	@DisplayName("剥离脚本、事件属性与非法协议")
	void sanitize_shouldStripScriptEventAndUnsafeProtocols() {
		// XSS：script、onclick、javascript:、data:；div 上的 style 随标签剥离
		String dirty = "<p onclick=\"alert(1)\">x</p>" + "<script>alert(1)</script>"
				+ "<a href=\"javascript:alert(1)\">bad</a>"
				+ "<img src=\"data:text/html;base64,xxx\" onerror=\"alert(1)\">"
				+ "<div class=\"x\" style=\"color:red\"><strong>ok</strong></div>";

		String cleaned = InAppHtmlSanitizer.sanitize(dirty);

		assertThat(cleaned).doesNotContain("<script")
			.doesNotContain("onclick")
			.doesNotContain("javascript:")
			.doesNotContain("onerror")
			.doesNotContain("data:")
			.doesNotContain("<div")
			.doesNotContain("class=")
			.contains("<strong>ok</strong>")
			.contains("x");
	}

	@Test
	@DisplayName("剥离 span 上的危险或不允许的 style")
	void sanitize_shouldStripUnsafeSpanStyles() {
		// expression / url / 非法属性不得保留
		String dirty = "<p><span style=\"color:red; expression(alert(1)); background:url(javascript:alert(1));"
				+ " font-size:20px\">t</span></p>";

		String cleaned = InAppHtmlSanitizer.sanitize(dirty);

		assertThat(cleaned).contains("color: red")
			.doesNotContain("expression")
			.doesNotContain("javascript")
			.doesNotContain("font-size")
			.doesNotContain("url(")
			.contains("t");
	}

	@Test
	@DisplayName("iframe/svg/form 等非白名单标签被剥离")
	void sanitize_shouldStripForbiddenTags() {
		// 常见危险或多余标签
		String dirty = "<iframe src=\"https://evil\"></iframe><svg onload=\"x\"></svg>"
				+ "<form action=\"/x\"><input></form><p>keep</p>";

		String cleaned = InAppHtmlSanitizer.sanitize(dirty);

		assertThat(cleaned).doesNotContain("<iframe")
			.doesNotContain("<svg")
			.doesNotContain("<form")
			.doesNotContain("<input")
			.isEqualTo("<p>keep</p>");
	}

}
