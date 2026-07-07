package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.form.FilePrivacyUpdateForm;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordCommonService;
import com.auth.service.system.file.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.common.model.SystemAuditBizModule.SYS_FILE;

/**
 * 当前用户文件查询与治理控制器
 *
 * @author Bunny
 */
@AuthenticatedApi
@RequestMapping("/api/system/me/file")
@RestController
public class PersonalFileRecordController {

	private final FileRecordCommonService fileRecordCommonService;

	private final FileRecordService fileRecordService;

	public PersonalFileRecordController(FileRecordCommonService fileRecordCommonService,
			FileRecordService fileRecordService) {
		this.fileRecordCommonService = fileRecordCommonService;
		this.fileRecordService = fileRecordService;
	}

	@Operation(summary = "文件分页查询")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		// 固定查询人
		Long ownerUserId = SecurityUserUtils.getUserId();
		query.setOwnerUserId(ownerUserId);
		query.setDeleteSource(null);

		PageResponse<FileRecordPageVO> pageResult = fileRecordCommonService.getPage(query, false);
		return Result.success(pageResult);
	}

	@Operation(summary = "文件详情")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		FileRecordDetailVO detail = fileRecordCommonService.getDetail(id, ownerUserId, null, false, null);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECORD_DOWNLOAD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.EXPORT)
	@Operation(summary = "文件批量下载")
	@PostMapping("/download")
	public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<Long> ids) {
		Long ownerUserId = SecurityUserUtils.getUserId();
		StreamingResponseBody body = fileRecordService.batchDownload(ids, ownerUserId);
		return FileDelivery.deliver(body, FileRecordService.BATCH_DOWNLOAD_FILE_NAME,
				FileRecordService.BATCH_DOWNLOAD_CONTENT_TYPE);
	}

	@OperationLog(targetType = "FILE_RECORD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "文件批量删除")
	@DeleteMapping()
	public Result<Void> delete(@RequestBody List<Long> ids) {
		Long operatorUserId = SecurityUserUtils.getUserId();
		fileRecordService.deleteByIds(ids, operatorUserId, operatorUserId);
		return Result.success();
	}

	@OperationLog(targetType = "FILE_RECORD_PRIVACY", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "文件隐私切换")
	@PostMapping("/privacy")
	public Result<Void> updatePrivacy(@Valid @RequestBody FilePrivacyUpdateForm form) {
		Long operatorUserId = SecurityUserUtils.getUserId();
		fileRecordService.updatePrivacyByIds(form.getIds(), form.getIsPrivate(), operatorUserId, operatorUserId);
		return Result.success();
	}

}
