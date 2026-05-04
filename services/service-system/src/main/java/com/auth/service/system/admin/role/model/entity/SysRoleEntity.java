package com.auth.service.system.admin.role.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色主表，对应 {@code sys_role}。
 *
 * @author Bunny
 */
@TableName("sys_role")
@Schema(name = "SysRoleEntity", title = "系统角色")
@Getter
@Setter
public class SysRoleEntity extends BaseEntity {

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "状态(0=正常，1=禁用)")
	private Integer status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)")
	private Integer roleType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@TableLogic(value = "0", delval = "1")
	@Schema(title = "逻辑删除(0=否,1=是)")
	private Integer isDeleted;

}
