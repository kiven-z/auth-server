package com.auth.service.system.common.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户档案-岗位关联查询行
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserPostProfilePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户Id")
	private Long id;

	@Schema(title = "岗位ID")
	private Long postId;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

	@Schema(title = "状态")
	private Integer status;

}
