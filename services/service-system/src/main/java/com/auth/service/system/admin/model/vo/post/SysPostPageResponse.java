package com.auth.service.system.admin.model.vo.post;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位分页行
 *
 * @author Bunny
 */
@Schema(name = "SysPostPageResponse", title = "岗位分页行")
@Data
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

	@Schema(title = "状态(0=禁用,1=启用)")
	private Integer status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
