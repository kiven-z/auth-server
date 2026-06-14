package com.auth.service.auth.session.domain.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局在线会话分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "OnlineSessionPageQuery", title = "全局在线会话查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OnlineSessionPageQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户名（模糊匹配）")
	private String username;

	@Schema(title = "IP 地址（包含匹配）")
	private String ipAddress;

}
