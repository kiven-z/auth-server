package com.auth.service.system.common.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户档案中的岗位关联行
 *
 * @author Bunny
 */
@Schema(name = "SysUserPostProfileItemResponse", title = "用户档案-岗位项")
@Getter
@Setter
public class SysUserPostProfileItemResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户-岗位关联主键")
	private Long id;

	@JsonStringFormat
	@Schema(title = "岗位ID")
	private Long postId;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=已调离,1=正常,2=兼职)")
	private Integer status;

}
