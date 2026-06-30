package com.auth.common.core.model.response;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础返回数据
 *
 * @author Bunny
 */
@Getter
@Setter
public class BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 唯一标识
	 */
	@JsonStringFormat
	private Long id;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

	/**
	 * 创建用户ID
	 */
	@JsonStringFormat
	private Long createUser;

	/**
	 * 更新用户ID
	 */
	@JsonStringFormat
	private Long updateUser;

	/**
	 * 创建用户名
	 */
	private String createUsername;

	/**
	 * 更新用户名
	 */
	private String updateUsername;

}
