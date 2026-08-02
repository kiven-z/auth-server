package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色授权对象种子项（勾选回显）
 *
 * @author Bunny
 */
@Schema(name = "GrantSubjectItemVO", title = "角色授权对象种子项")
@Getter
@Setter
@ToString
public class GrantSubjectItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主体 ID")
	private Long id;

	@Schema(title = "展示文案")
	private String label;

}
