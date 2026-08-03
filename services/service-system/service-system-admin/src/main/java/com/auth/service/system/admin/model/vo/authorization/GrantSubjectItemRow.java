package com.auth.service.system.admin.model.vo.authorization;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色授权对象种子行（勾选回显）
 *
 * @author Bunny
 */
@Schema(name = "GrantSubjectItemRow", title = "角色授权对象种子行")
@Getter
@Setter
@ToString
public class GrantSubjectItemRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主体 ID")
	private Long id;

	@Schema(title = "展示文案")
	private String label;

}
