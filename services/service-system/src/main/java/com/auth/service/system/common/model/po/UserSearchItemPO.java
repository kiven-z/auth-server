package com.auth.service.system.common.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户关键词搜索查询行（Mapper 投影，非 API 出参）
 *
 * @author Bunny
 */
@Data
public class UserSearchItemPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

}
