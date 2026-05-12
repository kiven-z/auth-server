package com.auth.service.system.log.model.entity;

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

	@Schema(title = "用户ID")
	private Long userId;

	@JsonIgnore
	@Schema(title = "密码Hash值")
	private String passwordHash;

	@Schema(title = "修改IP地址")
	private String changeIp;

	/**
	 * 逻辑删除标记（查询 SQL 显式带 {@code is_deleted = 0}；未使用 {@code @TableLogic}，便于后续一键启用）
	 */
	@Schema(title = "逻辑删除")
	private Boolean isDeleted;

}
