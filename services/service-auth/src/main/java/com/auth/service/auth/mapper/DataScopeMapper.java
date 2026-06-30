package com.auth.service.auth.mapper;

import java.util.List;

import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据范围
 *
 * @author Bunny
 */
@Mapper
public interface DataScopeMapper {

	/**
	 * 按用户 ID 查询数据范围配置（至多一行）
	 * @param userId 用户 ID
	 * @return 配置行；不存在时为 {@code null}
	 */
	UserScopePO selectByUserId(@Param("userId") Long userId);

	/**
	 * 按角色码列表查询数据范围（每角色至多一行）
	 * @param roleCodes 角色码列表；空列表时返回空结果
	 * @return 角色范围列表
	 */
	List<RoleScopePO> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

}
