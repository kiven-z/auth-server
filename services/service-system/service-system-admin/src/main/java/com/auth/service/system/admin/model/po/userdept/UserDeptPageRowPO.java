package com.auth.service.system.admin.model.po.userdept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户部门关联分页行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserDeptPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键 ID")
	private Long id;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "部门 ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "关联状态")
	private Integer status;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

	@Schema(title = "备注")
	private String remark;

}
