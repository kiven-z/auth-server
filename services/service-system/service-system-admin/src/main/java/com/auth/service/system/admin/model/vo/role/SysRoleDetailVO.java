package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.DeptReferenceVO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.PostReferenceVO;
import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 角色详情 VO
 *
 * @author Bunny
 */
@Schema(name = "SysRoleDetailVO", title = "角色详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysRoleDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "已分配权限列表（sys_role_permission 正查）")
	private List<PermissionReferenceVO> boundPermissions;

	@Schema(title = "已绑定菜单列表（sys_menu_role 反查）")
	private List<SysPermissionBoundMenuItemVO> boundMenus;

	@Schema(title = "已授权用户列表（grant_table 反查）")
	private List<UserReferenceVO> boundUsers;

	@Schema(title = "已授权部门列表（grant_table 反查）")
	private List<DeptReferenceVO> boundDepts;

	@Schema(title = "已授权岗位列表（grant_table 反查）")
	private List<PostReferenceVO> boundPosts;

}
