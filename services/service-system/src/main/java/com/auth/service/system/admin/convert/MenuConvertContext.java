package com.auth.service.system.admin.convert;

import java.util.List;
import java.util.Map;

/**
 * MapStruct 转换上下文：承载角色 Map 与权限 Map，避免 {@code @Context} 同类型冲突
 *
 * @param roleMap 菜单 ID -> 角色编码列表
 * @param permissionMap 菜单 ID -> 权限编码列表
 * @author Bunny
 */
public record MenuConvertContext(Map<Long, List<String>> roleMap, Map<Long, List<String>> permissionMap) {
}
