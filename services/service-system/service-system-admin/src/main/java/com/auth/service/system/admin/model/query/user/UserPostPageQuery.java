package com.auth.service.system.admin.model.query.user;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关联分页查询
 *
 * @author Bunny
 */
@Schema(name = "UserPostPageQuery", title = "用户岗位关联分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserPostPageQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "关联状态")
	private Integer status;

}
