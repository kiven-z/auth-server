package com.auth.service.system.message.model.form.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 站内信业务分类批量启用/禁用表单
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryStatusForm", title = "站内信业务分类批量启用/禁用表单")
@Getter
@Setter
public class InAppMessageCategoryStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "分类主键列表")
	@NotEmpty(message = "分类ID列表不能为空")
	private List<Long> ids;

	@Schema(title = "启用状态", description = "true=启用，false=停用")
	@NotNull(message = "启用状态不能为空")
	private Boolean status;

}
