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

		assertThat(cleaned).contains("<p>", "<strong>", "<em>", "<ul>", "<ol>", "<li>", "<br>", "<a ", "<img ");
		assertThat(cleaned).contains("href=\"https://example.com\"");
		assertThat(cleaned).contains("href=\"mailto:a@b.com\"");
		assertThat(cleaned).contains("src=\"https://cdn.example.com/a.png\"");
		assertThat(cleaned).contains("alt=\"pic\"");
	}

	@Test
	@DisplayName("剥离脚本、事件属性与非法协议")
	void sanitize_shouldStripScriptEventAndUnsafeProtocols() {
		// XSS：script、onclick、javascript:、data:
		String dirty = "<p onclick=\"alert(1)\">x</p>" + "<script>alert(1)</script>"
				+ "<a href=\"javascript:alert(1)\">bad</a>"
				+ "<img src=\"data:text/html;base64,xxx\" onerror=\"alert(1)\">"
				+ "<div class=\"x\" style=\"color:red\"><strong>ok</strong></div>";

		String cleaned = InAppHtmlSanitizer.sanitize(dirty);

		assertThat(cleaned).doesNotContain("<script");
		assertThat(cleaned).doesNotContain("onclick");
		assertThat(cleaned).doesNotContain("javascript:");
		assertThat(cleaned).doesNotContain("onerror");
		assertThat(cleaned).doesNotContain("data:");
		assertThat(cleaned).doesNotContain("<div");
		assertThat(cleaned).doesNotContain("style=");
		assertThat(cleaned).doesNotContain("class=");
		assertThat(cleaned).contains("<strong>ok</strong>");
		assertThat(cleaned).contains("x");
	}

	@Test
	@DisplayName("iframe/svg/form 等非白名单标签被剥离")
	void sanitize_shouldStripForbiddenTags() {
		// 常见危险或多余标签
		String dirty = "<iframe src=\"https://evil\"></iframe><svg onload=\"x\"></svg>"
				+ "<form action=\"/x\"><input></form><p>keep</p>";

		String cleaned = InAppHtmlSanitizer.sanitize(dirty);

		assertThat(cleaned).doesNotContain("<iframe");
		assertThat(cleaned).doesNotContain("<svg");
		assertThat(cleaned).doesNotContain("<form");
		assertThat(cleaned).doesNotContain("<input");
		assertThat(cleaned).isEqualTo("<p>keep</p>");
	}

}
