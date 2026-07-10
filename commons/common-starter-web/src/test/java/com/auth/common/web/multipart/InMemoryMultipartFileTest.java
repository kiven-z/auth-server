package com.auth.common.web.multipart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InMemoryMultipartFile} unit tests.
 */
@DisplayName("InMemoryMultipartFile")
class InMemoryMultipartFileTest {

	@Test
	@DisplayName("builder: name equals originalFilename")
	void builder_shouldUseSameNameAndOriginalFilename() {
		// name 与 originalFilename 一致时应完整保留
		byte[] content = "demo".getBytes();
		InMemoryMultipartFile file = InMemoryMultipartFile.builder()
			.name("report.xlsx")
			.originalFilename("report.xlsx")
			.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
			.content(content)
			.build();

		assertThat(file.getName()).isEqualTo("report.xlsx");
		assertThat(file.getOriginalFilename()).isEqualTo("report.xlsx");
		assertThat(file.getContentType())
			.isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(file.getBytes()).isEqualTo(content);
		assertThat(file.getSize()).isEqualTo(content.length);
		assertThat(file.isEmpty()).isFalse();
	}

	@Test
	@DisplayName("builder: keeps distinct name and originalFilename")
	void builder_shouldKeepDistinctNames() {
		// 应保留独立的表单字段名与原始文件名
		InMemoryMultipartFile file = InMemoryMultipartFile.builder()
			.name("file")
			.originalFilename("report.xlsx")
			.contentType("text/plain")
			.content("x".getBytes())
			.build();

		assertThat(file.getName()).isEqualTo("file");
		assertThat(file.getOriginalFilename()).isEqualTo("report.xlsx");
	}

	@Test
	@DisplayName("null content: treated as empty byte array")
	void nullContent_shouldBeEmpty() {
		// null 内容应归一化为空数组
		InMemoryMultipartFile file = InMemoryMultipartFile.builder()
			.name("empty.txt")
			.originalFilename("empty.txt")
			.contentType("text/plain")
			.content(null)
			.build();

		assertThat(file.isEmpty()).isTrue();
		assertThat(file.getSize()).isZero();
		assertThat(file.getBytes()).isEmpty();
	}

	@Test
	@DisplayName("default content: treated as empty byte array")
	void defaultContent_shouldBeEmpty() {
		// 未设置 content 时应默认为空数组
		InMemoryMultipartFile file = InMemoryMultipartFile.builder()
			.name("empty.txt")
			.originalFilename("empty.txt")
			.contentType("text/plain")
			.build();

		assertThat(file.isEmpty()).isTrue();
		assertThat(file.getSize()).isZero();
		assertThat(file.getBytes()).isEmpty();
	}

	@Test
	@DisplayName("equals and hashCode: compare byte array content, not reference")
	void equalsAndHashCode_shouldCompareByteArrayContent() {
		// 相同内容的字节数组应视为相等
		byte[] left = { 1, 2, 3 };
		byte[] right = { 1, 2, 3 };
		InMemoryMultipartFile first = InMemoryMultipartFile.builder()
			.name("avatar")
			.originalFilename("avatar.png")
			.contentType("image/png")
			.content(left)
			.build();
		InMemoryMultipartFile second = InMemoryMultipartFile.builder()
			.name("avatar")
			.originalFilename("avatar.png")
			.contentType("image/png")
			.content(right)
			.build();

		assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
	}

}
