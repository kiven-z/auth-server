package com.auth.service.system.file.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件异步导出任务下载链接
 *
 * @author Bunny
 */
@Schema(name = "FileExportTaskDownloadLinkVO", title = "文件异步导出任务下载链接")
@Getter
@Setter
public class FileExportTaskDownloadLinkVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "下载链接")
	private String url;

	@Schema(title = "文件名")
	private String fileName;

	@Schema(title = "链接有效秒数")
	private Integer expireSeconds;

}
