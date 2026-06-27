package com.auth.service.system.admin.convert.admin.menu;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * MapStruct 转换上下文：承载角色 Map，避免 {@code @Context} 同类型冲突
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class MenuConvertContext {

	/**
	 * 菜单 ID -> 角色编码列表
	 */
	Map<Long, List<String>> roleMap;

}
