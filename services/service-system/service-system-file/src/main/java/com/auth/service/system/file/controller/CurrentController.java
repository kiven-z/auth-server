package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.common.model.SystemAuditBizModule;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.SysFileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 当前用户文件查询与治理控制器
 *
 * @author Bunny
 */
@RequestMapping("/api/system/me/file")
@RestController
public class CurrentController {

	private final SysFileRecordService sysFileRecordService;

	public CurrentController(SysFileRecordService sysFileRecordService) {
		this.sysFileRecordService = sysFileRecordService;
	}

	@Operation(summary = "文件分页查询")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		query.setOwnerUserId(ownerUserId);

		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);
		return Result.success(pageResult);
	}

	@Operation(summary = "文件详情")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		FileRecordDetailVO detail = sysFileRecordService.getDetail(id, ownerUserId);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECORD", serviceDomain = AuditServiceDomain.SYSTEM,
			bizModule = SystemAuditBizModule.SYS_FILE, operation = OperationLogKind.DELETE)
	@Operation(summary = "文件逻辑删除")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable("id") Long id) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		sysFileRecordService.deleteById(id, ownerUserId);
		return Result.success();
	}

}
