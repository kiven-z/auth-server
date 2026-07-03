package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.SysFileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static com.auth.module.security.contract.api.audit.AuditServiceDomain.SYSTEM;
import static com.auth.service.system.common.model.SystemAuditBizModule.SYS_FILE;

/**
 * 文件查询与治理控制器
 *
 * @author Bunny
 */
@Tag(name = "文件查询与治理")
@RequestMapping("/api/system/file")
@RestController
public class FileRecordController {

	private final SysFileRecordService sysFileRecordService;

	public FileRecordController(SysFileRecordService sysFileRecordService) {
		this.sysFileRecordService = sysFileRecordService;
	}

	@Operation(summary = "文件分页查询")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/page")
	public Result<PageResponse<FileRecordPageVO>> page(@Valid FileRecordPageQuery query) {
		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getPage(query);
		return Result.success(pageResult);
	}

	@Operation(summary = "回收站文件分页查询")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/recycle/page")
	public Result<PageResponse<FileRecordPageVO>> recyclePage(@Valid FileRecordPageQuery query) {
		PageResponse<FileRecordPageVO> pageResult = sysFileRecordService.getRecyclePage(query);
		return Result.success(pageResult);
	}

	@Operation(summary = "文件详情")
	@PreAuthorize("@auth.decide('sys:file:query')")
	@GetMapping("/{id}")
	public Result<FileRecordDetailVO> detail(@PathVariable("id") Long id) {
		FileRecordDetailVO detail = sysFileRecordService.getDetail(id, null);
		return Result.success(detail);
	}

	@OperationLog(targetType = "FILE_RECORD_DOWNLOAD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.EXPORT)
	@Operation(summary = "文件批量下载")
	@PreAuthorize("@auth.decide('sys:file:download')")
	@PostMapping("/download")
	public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<Long> ids) {
		StreamingResponseBody body = sysFileRecordService.batchDownload(ids, null);
		return FileDelivery.deliver(body, SysFileRecordService.BATCH_DOWNLOAD_FILE_NAME,
				SysFileRecordService.BATCH_DOWNLOAD_CONTENT_TYPE);
	}

	@OperationLog(targetType = "FILE_RECORD", serviceDomain = SYSTEM, bizModule = SYS_FILE,
			operation = OperationLogKind.DELETE)
	@Operation(summary = "文件批量逻辑删除")
	@PreAuthorize("@auth.decide('sys:file:delete')")
	@DeleteMapping()
	public Result<Void> delete(@RequestBody List<Long> ids) {
		sysFileRecordService.deleteByIds(ids, null);
		return Result.success();
	}

}
