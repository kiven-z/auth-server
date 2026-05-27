package com.auth.service.system.log.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户密码历史日志，对应表 {@code log_user_password_history}
 * <p>
 * 记录用户修改密码时的 Hash（BCrypt），仅用于历史密码比对，禁止通过 API 返回 {@code passwordHash} 删除接口为物理删除
 * </p>
 *
 * @author Bunny
 */
@TableName("log_user_password_history")
@Schema(name = "LogUserPasswordHistoryEntity", title = "用户密码历史日志")
@Getter
@Setter
@Accessors(chain = true)
public class LogUserPasswordHistoryEntity extends BaseEntity {

	@Schema(title = "用户 ID；未关联用户时为 null")
	private Long userId;

	@JsonIgnore
	@Schema(title = "密码 Hash（BCrypt）")
	private String passwordHash;

	@Schema(title = "修改密码时的客户端 IP")
	private String changeIp;

}
