package com.auth.service.auth.domain.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 在线用户统计摘要
 *
 * @author Bunny
 */
@Schema(name = "OnlineUserSummaryVO", title = "在线用户统计")
@Getter
@Setter
@ToString
public class OnlineUserSummaryVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "在线用户数")
	private Long onlineUserCount;

}
