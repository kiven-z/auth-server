package com.auth.service.system.admin.model.vo.reference.ext;

import com.auth.service.system.admin.model.vo.reference.DeptReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 用户-部门关联回显（在部门基础字段上扩展主部门与关联状态）
 *
 * @author Bunny
 */
@Schema(name = "UserDeptReferenceVO", title = "用户部门关联回显")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserDeptReferenceVO extends DeptReferenceVO {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=已调离,1=正常,2=兼职)")
	private Integer relationStatus;

}
