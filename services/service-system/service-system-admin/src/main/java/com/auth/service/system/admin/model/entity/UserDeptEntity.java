package com.auth.service.system.admin.model.entity;

import java.time.LocalDateTime;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户部门关联，对应 {@code user_dept}
 *
 * @author Bunny
 */
@TableName("user_dept")
@Getter
@Setter
public class UserDeptEntity extends BaseEntity {

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "部门 ID")
	private Long deptId;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "在部门内的职务")
	private String position;

	@Schema(title = "授权/绑定时间")
	private LocalDateTime grantTime;

	@Schema(title = "关联状态")
	private Integer status;

}
