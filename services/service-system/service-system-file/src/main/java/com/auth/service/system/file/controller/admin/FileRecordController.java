package com.auth.service.system.file.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.form.FilePrivacyUpdateForm;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordQueryService;
import com.auth.service.system.file.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.file.model.constants.FileAuditBizModule.SYS_FILE;

/**
 * 文件查询与治理控制器
 *
 * @author Bunny
 */
@Tag(name = "文件查询与治理")
@RequestMapping("/api/system/file")
@RestController
public class FileRecordController {

	private final FileRecordQueryService fileRecordQueryService;

	private final FileRecordService fileRecordService;

	public FileRecordController(FileRecordQueryService fileRecordQueryService, FileRecordService fileRecordService) {
		this.fileRecordQueryService = fileRecordQueryService;
		this.fileRecordService = fileRecordService;
	}

	@Operation(summary = "文件分页查询")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		PageResponse<FileRecordPageVO> pageResult = fileRecordQueryService.getPage(query, false);
		return Result.success(pageResult);
	}

	@Operation(summary = "文件详情")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		FileRecordDetailVO detail = fileRecordQueryService.getDetail(id, null, null, false, null);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECORD_DOWNLOAD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.EXPORT)
	@Operation(summary = "文件批量下载")
	@PreAuthorize("@auth.decide('sys:file:download')")
	@PostMapping("/download")
	public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<Long> ids) {
		StreamingResponseBody body = fileRecordService.batchDownload(ids, null);
		return FileDelivery.deliver(body, FileRecordService.BATCH_DOWNLOAD_FILE_NAME,
				FileRecordService.BATCH_DOWNLOAD_CONTENT_TYPE);
	}

	@OperationLog(targetType = "FILE_RECORD_PRIVACY", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.UPDATE)
	@Operation(summary = "文件私有切换")
	@PreAuthorize("@auth.decide('sys:file:privacy')")
	@PostMapping("/privacy")
	public Result<Void> updatePrivacy(@Valid @RequestBody FilePrivacyUpdateForm form) {
		fileRecordService.updatePrivacyByIds(form.getIds(), form.getIsPrivate(), null);
		return Result.success();
	}

	@OperationLog(targetType = "FILE_RECORD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "文件批量删除")
	@PreAuthorize("@auth.decide('sys:file:delete')")
	@DeleteMapping()
	public Result<Void> delete(@RequestBody List<Long> ids) {
		fileRecordService.deleteByIds(ids, null, FileDeleteSource.SYSTEM_ACTION.getCode());
		return Result.success();
	}

}
