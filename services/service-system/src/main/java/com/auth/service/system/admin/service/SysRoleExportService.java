package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * 角色 Excel 导出
 *
 * @author Bunny
 */
public interface SysRoleExportService {

	/**
	 * 按筛选条件与页码区间导出角色 Excel
	 * @param query 导出查询（含 fromPage、toPage）
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	ResponseEntity<byte[]> export(SysRoleExportQuery query) throws IOException;

}
