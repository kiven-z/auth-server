package com.auth.service.auth.mapper;

import com.auth.service.auth.model.po.scope.RoleScopePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色级数据范围只读 Mapper（role_scope）
 *
 * @author Bunny
 */
@Mapper
public interface RoleScopeMapper {

	/**
	 * 按角色码列表查询数据范围（每角色至多一行）
	 * @param roleCodes 角色码列表；空列表时返回空结果
	 * @return 角色范围列表
	 */
	List<RoleScopePO> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

}
