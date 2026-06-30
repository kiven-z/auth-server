package com.auth.service.system.admin.model.vo.user;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户部门关联分页行
 *
 * @author Bunny
 */
@Schema(name = "UserDeptPageVO", title = "用户部门关联分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserDeptPageVO extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户 ID")
	private Long userId;

	@JsonStringFormat
	@Schema(title = "部门 ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=已调离,1=正常,2=兼职)")
	private Integer status;

	@Schema(title = "备注")
	private String remark;

}
