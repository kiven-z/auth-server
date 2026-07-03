package com.auth.service.system.file.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import com.auth.service.system.file.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件上传控制器
 *
 * @author Bunny
 */
@Tag(name = "文件上传")
@RequiredArgsConstructor
@RequestMapping("/api/system/file")
@RestController
public class FileUploadController {

	private final FileUploadService fileUploadService;

	@Operation(summary = "单文件上传")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result<FileUploadResponseVO> upload(@Valid FileUploadForm form) {
		FileUploadResponseVO response = fileUploadService.upload(form);
		return Result.success(response);
	}

}
