package com.auth.service.system.admin.excel.permission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.export.converter.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 权限 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysPermissionSpreadsheetRow {

	@ExcelProperty(value = "权限编码", index = 0)
	private String permissionCode;

	@ExcelProperty(value = "权限名称", index = 1)
	private String permissionName;

	@ExcelProperty(value = "权限类型", index = 2)
	private String permissionTypeLabel;

	@ExcelProperty(value = "状态", index = 3)
	private String statusLabel;

	@ExcelProperty(value = "显示顺序", index = 4)
	private Integer orderNum;

	@ExcelProperty(value = "备注", index = 5)
	private String remark;

	@ExcelProperty(value = "创建时间", index = 6)
	private String createTimeText;

	@ExcelProperty(value = "更新时间", index = 7)
	private String updateTimeText;

	@ExcelProperty(value = "主键ID", index = 8, converter = LongIdExcelConverter.class)
	private Long id;

}
