package com.auth.module.file.export.model;

/**
 * 导出页码区间（1-based，含首尾页）
 *
 * @param fromPage 起始页
 * @param toPage 结束页
 * @param pageSize 每页条数
 * @author Bunny
 */
public record ExportPageRange(int fromPage, int toPage, int pageSize) {
}
