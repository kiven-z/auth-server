package com.auth.module.file.importer.model;

/**
 * 单行导入失败信息
 *
 * @param rowIndex 行号（1-based，不含表头）
 * @param message 失败原因
 * @author Bunny
 */
public record ImportRowError(int rowIndex, String message) {

}
