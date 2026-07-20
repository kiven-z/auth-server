package com.auth.service.system.message.model.vo.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 站内信业务分类只读节点（大类 / 小类）
 *
 * @author Bunny
 */
@Schema(name = "InAppMessageCategoryVO", title = "站内信业务分类")
@Getter
@Builder
@ToString
public class InAppMessageCategoryVO {

	@Schema(title = "分类码")
	private String code;

	@Schema(title = "展示名")
	private String name;

	@Schema(title = "同级排序")
	private Integer sortOrder;

	@Schema(title = "子分类", description = "扁平接口固定为 null；预留树形扩展")
	private List<InAppMessageCategoryVO> children;

}
