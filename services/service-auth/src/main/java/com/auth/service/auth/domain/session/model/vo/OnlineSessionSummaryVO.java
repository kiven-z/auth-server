package com.auth.service.auth.domain.session.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局在线会话统计摘要
 *
 * @author Bunny
 */
@Schema(name = "OnlineSessionSummaryVO", title = "全局在线会话统计")
@Getter
@Setter
@ToString
public class OnlineSessionSummaryVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * global ZSet 中的会话索引条数（reconcile 前可能含少量僵尸成员）
	 */
	@Schema(title = "在线会话索引条数")
	private Long sessionCount;

}
