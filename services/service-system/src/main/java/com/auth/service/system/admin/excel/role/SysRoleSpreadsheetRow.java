package com.auth.service.system.admin.excel.role;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.export.converter.LongIdExcelConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色 Excel 表格行
 *
 * @author Bunny
 */
@Getter
@Setter
public class SysRoleSpreadsheetRow {

	@ExcelProperty(value = "角色编码", index = 0)
	private String roleCode;

	@ExcelProperty(value = "角色名称", index = 1)
	private String roleName;

	@ExcelProperty(value = "角色类型", index = 2)
	private String roleTypeLabel;

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
