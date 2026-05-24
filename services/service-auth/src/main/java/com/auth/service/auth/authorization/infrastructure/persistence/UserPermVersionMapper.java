package com.auth.service.auth.authorization.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户权限版本号批量更新 Mapper。
 *
 * @author Bunny
 */
@Mapper
public interface UserPermVersionMapper {

	/**
	 * 批量递增 {@code perm_version}（仅正常且未删除用户，防御性条件与分桶策略一致）。
	 * @param userIds 用户 ID 列表，非空
	 * @return 更新行数
	 */
	int incrementPermVersionByUserIds(@Param("userIds") List<Long> userIds);

}
