package com.auth.module.file.importer.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ImportRowError} Jackson 序列化测试
 *
 * @author Bunny
 */
@DisplayName("ImportRowError JSON 序列化")
class ImportRowErrorSerializationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("单行错误序列化包含 rowIndex、message、code、args")
	void serializesImportRowErrorFields() throws Exception {
		ImportRowError error = ImportErrors.required(3, "roleCode");

		String json = objectMapper.writeValueAsString(error);
		JsonNode node = objectMapper.readTree(json);

		assertThat(node.get("rowIndex").asInt()).isEqualTo(3);
		assertThat(node.get("message").asText()).isEqualTo("Row 3: roleCode is required");
		assertThat(node.get("code").asText()).isEqualTo("REQUIRED");
		assertThat(node.get("args")).isNotNull();
		assertThat(node.get("args").get(0).asText()).isEqualTo("roleCode");
	}

	@Test
	@DisplayName("SpreadsheetImportResult.errors 可正常序列化")
	void serializesSpreadsheetImportResultWithErrors() throws Exception {
		SpreadsheetImportResult result = SpreadsheetImportResult
			.withErrors(List.of(ImportErrors.duplicateInFile(2, "username", "admin")));

		String json = objectMapper.writeValueAsString(result);
		JsonNode node = objectMapper.readTree(json);

		assertThat(node.get("success").asBoolean()).isFalse();
		assertThat(node.get("importedCount").asInt()).isZero();
		assertThat(node.get("errors")).isNotNull();
		assertThat(node.get("errors")).hasSize(1);
		assertThat(node.get("errors").get(0).get("rowIndex").asInt()).isEqualTo(2);
		assertThat(node.get("errors").get(0).get("message").asText())
			.isEqualTo("Row 2: username 'admin' duplicated in file");
		assertThat(node.get("errors").get(0).get("code").asText()).isEqualTo("DUPLICATE_IN_FILE");
		assertThat(node.get("errors").get(0).get("args")).hasSize(2);
	}

}
