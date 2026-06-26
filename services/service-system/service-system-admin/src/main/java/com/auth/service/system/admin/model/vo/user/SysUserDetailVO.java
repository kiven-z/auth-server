package com.auth.service.system.admin.model.vo.user;

import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.model.vo.reference.RoleReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情 VO（档案 + 生效授权）
 *
 * @author Bunny
 */
@Schema(name = "SysUserDetailVO", title = "用户详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysUserDetailVO extends SysUserProfileVO {

	@Schema(title = "生效角色列表（与登录授权语义一致）")
	private List<RoleReferenceVO> effectiveRoles = new ArrayList<>();

	@Schema(title = "生效权限列表（与登录授权语义一致）")
	private List<PermissionReferenceVO> effectivePermissions = new ArrayList<>();

}
