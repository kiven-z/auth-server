package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 部门详情 VO。
 */
@Schema(name = "SysDeptDetailVO", title = "部门详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysDeptDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "父部门ID，0 表示顶级")
	@JsonStringFormat
	private Long parentId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "启用状态")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "关联用户列表（user_dept 反查，仅本部门）")
	private List<SysDeptBoundUserItemVO> boundUsers;

	@Schema(title = "下属岗位列表（sys_post 反查，仅本部门）")
	private List<SysDeptBoundPostItemVO> boundPosts;

	@Schema(title = "已授权角色列表（grant_table DEPT 主体反查）")
	private List<SysDeptBoundRoleItemVO> boundRoles;

}
