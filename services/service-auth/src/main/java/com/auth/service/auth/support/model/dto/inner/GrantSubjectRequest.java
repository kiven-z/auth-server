package com.auth.service.auth.support.model.dto.inner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权主体（内部接口）：与 grant_table.subject 对应，用于批量反查受影响用户。
 *
 * @author Bunny
 */
@Schema(name = "GrantSubjectRequest", title = "授权主体")
@Getter
@Setter
public class GrantSubjectRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主体类型：USER / DEPT / POST", example = "USER")
	private String subjectType;

	@Schema(title = "主体 ID（用户/部门/岗位主键）")
	private Long subjectId;

}
