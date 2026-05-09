package com.auth.service.system.log.model.entity;

import java.io.Serial;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户密码历史日志，对应 {@code log_user_password_history}。
 * <p>
 * 每次用户修改密码时记录。password_hash 使用 BCrypt 加密存储，仅用于后续新旧密码比对， 严禁通过任何接口返回给前端。
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

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@Schema(title = "用户ID")
	private Long userId;

	/**
	 * 密码Hash值（BCrypt 加密）
	 */
	@JsonIgnore
	@Schema(title = "密码Hash值")
	private String passwordHash;

	/**
	 * 修改密码时的IP地址
	 */
	@Schema(title = "修改IP地址")
	private String changeIp;

}
