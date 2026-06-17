package com.auth.service.auth.infrastructure.persistence.mapper;

import com.auth.service.auth.infrastructure.persistence.projection.UserScopeProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户级数据范围只读 Mapper（user_scope）
 *
 * @author Bunny
 */
@Mapper
public interface UserScopeMapper {

	/**
	 * 按用户 ID 查询数据范围配置（至多一行）
	 * @param userId 用户 ID
	 * @return 配置行；不存在时为 {@code null}
	 */
	UserScopeProjection selectByUserId(@Param("userId") Long userId);

}
