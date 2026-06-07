package com.auth.service.system.admin.model.query.user;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysUserPageQuery", title = "用户分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysUserPageQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户名（模糊）")
	private String username;

	@Schema(title = "昵称（模糊）")
	private String nickname;

	@Schema(title = "手机号（模糊）")
	private String phone;

	@Schema(title = "邮箱（模糊）")
	private String email;

	@Schema(title = "工号（模糊）")
	private String employeeNo;

	@Schema(title = "账号状态(0=禁用,1=正常,2=锁定)")
	private Integer status;

	@Schema(title = "部门ID（含子部门；不传表示全部）")
	private Long deptId;

}
