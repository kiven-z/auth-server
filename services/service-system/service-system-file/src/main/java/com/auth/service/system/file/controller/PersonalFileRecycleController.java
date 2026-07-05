package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.common.model.SystemAuditBizModule.SYS_FILE;

/**
 * 当前用户文件回收站控制器
 *
 * @author Bunny
 */
@Tag(name = "个人文件回收站")
@AuthenticatedApi
@RequestMapping("/api/system/me/file/recycle")
@RestController
public class PersonalFileRecycleController {

	private final FileRecycleService fileRecycleService;

	public PersonalFileRecycleController(FileRecycleService fileRecycleService) {
		this.fileRecycleService = fileRecycleService;
	}

	@Operation(summary = "回收站分页查询")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		query.setOwnerUserId(ownerUserId);

		PageResponse<FileRecordPageVO> pageResult = fileRecycleService.getPage(query);
		return Result.success(pageResult);
	}

	@OperationLog(targetType = "FILE_RECYCLE_RESTORE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "回收站批量恢复")
	@PostMapping("/restore")
	public Result<Void> restore(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		fileRecycleService.restoreByIds(ids, ownerUserId, ownerUserId);
		return Result.success();
	}

	@OperationLog(targetType = "FILE_RECYCLE_PURGE", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "回收站批量彻底删除")
	@DeleteMapping("/purge")
	public Result<Void> purge(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		fileRecycleService.purgeByIds(ids, ownerUserId);
		return Result.success();
	}

}
