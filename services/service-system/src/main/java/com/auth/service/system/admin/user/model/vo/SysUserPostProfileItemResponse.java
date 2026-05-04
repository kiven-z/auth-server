package com.auth.service.system.admin.user.model.vo;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.annotation.JsonStringFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户档案中的岗位关联行
 *
 * @author Bunny
 */
@Data
@Schema(name = "SysUserPostProfileItemResponse", title = "用户档案-岗位项")
public class SysUserPostProfileItemResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户-岗位关联主键")
	@JsonStringFormat
	private Long id;

	@Schema(title = "岗位ID")
	@JsonStringFormat
	private Long postId;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=正常,1=已调离,2=兼职)")
	private Integer status;

}
