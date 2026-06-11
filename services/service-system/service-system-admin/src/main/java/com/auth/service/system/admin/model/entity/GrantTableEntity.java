package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一授权表行，对应 {@code grant_table}。
 *
 * @author Bunny
 */
@TableName("grant_table")
@Schema(name = "GrantTableEntity", title = "grant_table 授权边")
@Getter
@Setter
public class GrantTableEntity extends BaseEntity {

	@Schema(title = "授权主体类型")
	private String subjectType;

	@Schema(title = "授权主体 ID")
	private Long subjectId;

	@Schema(title = "角色编码")
	private String roleCode;

}
