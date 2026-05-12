package com.auth.service.system.common.feign.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权主体（内部 Feign）：与 auth-service GrantSubjectRequest 字段对齐。
 *
 * @author Bunny
 */
@Getter
@Setter
public class GrantSubjectInnerRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String subjectType;

	private Long subjectId;

}
