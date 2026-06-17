package com.auth.service.system.admin.model.vo.post;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位分页行
 *
 * @author Bunny
 */
@Schema(name = "SysPostPageResponse", title = "岗位分页行")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPostPageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
