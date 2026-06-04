package com.auth.service.system.admin.model.vo.post;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.admin.model.vo.dept.SysDeptBoundRoleItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 岗位详情 VO
 *
 * @author Bunny
 */
@Schema(name = "SysPostDetailVO", title = "岗位详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysPostDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "所属部门快照")
	private SysPostBoundDeptItemVO boundDept;

	@Schema(title = "关联用户列表（user_post 反查）")
	private List<SysPostBoundUserItemVO> boundUsers;

	@Schema(title = "已授权角色列表（grant_table POST 主体反查）")
	private List<SysDeptBoundRoleItemVO> boundRoles;

}
