package com.auth.service.system.file.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 文件异步导出任务管理端分页查询
 *
 * @author Bunny
 */
@Schema(name = "FileExportTaskAdminPageQuery", title = "文件异步导出任务管理端分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FileExportTaskAdminPageQuery extends PageQueryRequest {

	@Schema(title = "业务类型")
	@Size(max = 64, message = "业务类型长度不能超过64个字符")
	private String bizType;

	@Schema(title = "任务状态")
	@Size(max = 20, message = "任务状态长度不能超过20个字符")
	private String status;

	@Schema(title = "导出格式")
	@Size(max = 16, message = "导出格式长度不能超过16个字符")
	private String exportFormat;

	@Schema(title = "创建用户 ID")
	private Long createdBy;

	@Schema(title = "错误码")
	@Size(max = 64, message = "错误码长度不能超过64个字符")
	private String errorCode;

	@Schema(title = "创建开始时间（与 endTime 成对使用）")
	private Instant startTime;

	@Schema(title = "创建结束时间（与 startTime 成对使用）")
	private Instant endTime;

	@AssertTrue(message = "创建时间起止必须成对指定")
	public boolean isTimeRangePaired() {
		return (startTime == null && endTime == null) || (startTime != null && endTime != null);
	}

	@AssertTrue(message = "创建开始时间不能晚于创建结束时间")
	public boolean isTimeRangeOrdered() {
		return startTime == null || endTime == null || !startTime.isAfter(endTime);
	}

}
