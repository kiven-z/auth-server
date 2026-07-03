package com.auth.service.system.file.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件记录分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "FileRecordPageQuery", title = "文件记录分页查询条件")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FileRecordPageQuery extends PageQueryRequest {

	/**
	 * 归属用户ID
	 */
	@Schema(title = "归属用户ID")
	private Long ownerUserId;

	/**
	 * 业务类型
	 */
	@Schema(title = "业务类型")
	@Size(max = 64, message = "bizType 长度不能超过64个字符")
	private String bizType;

	/**
	 * 业务ID
	 */
	@Schema(title = "业务ID")
	@Size(max = 64, message = "bizId 长度不能超过64个字符")
	private String bizId;

	/**
	 * 原始文件名
	 */
	@Schema(title = "原始文件名（模糊匹配）")
	@Size(max = 255, message = "originalName 长度不能超过255个字符")
	private String originalName;

}
