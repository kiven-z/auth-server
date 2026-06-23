package com.auth.service.system.admin.model.vo.reference.ext;

import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 菜单权限分配回显（在权限基础字段上扩展是否已分配）
 *
 * @author Bunny
 */
@Schema(name = "MenuAssignedPermissionReferenceVO", title = "菜单已分配权限回显")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MenuAssignedPermissionReferenceVO extends PermissionReferenceVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否已关联当前菜单")
	private Boolean assigned;

}
