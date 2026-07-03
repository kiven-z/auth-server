package com.auth.service.system.file.service;

import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;

/**
 * 文件上传服务。
 *
 * @author Bunny
 */
public interface FileUploadService {

	/**
	 * 上传文件并落库
	 * @param form 上传表单
	 * @return 上传结果
	 */
	FileUploadResponseVO upload(FileUploadForm form);

}
