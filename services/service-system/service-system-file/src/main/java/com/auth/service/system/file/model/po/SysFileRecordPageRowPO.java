package com.auth.service.system.file.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.module.file.api.enums.StoragePlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件记录分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "SysFileRecordPageRowPO", title = "文件记录分页行（PO）")
@Getter
@Setter
@ToString
public class SysFileRecordPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "存储平台")
	private StoragePlatformEnum storagePlatform;

	@Schema(title = "上传模式")
	private String uploadMode;

	@Schema(title = "存储桶")
	private String bucket;

	@Schema(title = "对象键")
	private String objectKey;

	@Schema(title = "访问URL")
	private String url;

	@Schema(title = "原始文件名")
	private String originalName;

	@Schema(title = "MIME 类型")
	private String contentType;

	@Schema(title = "文件大小（字节）")
	private Long size;

	@Schema(title = "是否私有文件")
	private Boolean isPrivate;

	@Schema(title = "业务类型")
	private String bizType;

	@Schema(title = "业务ID")
	private String bizId;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

	@Schema(title = "删除来源")
	private String deleteSource;

	@Schema(title = "删除人")
	private Long deletedBy;

	@Schema(title = "删除时间")
	private LocalDateTime deletedAt;

}
