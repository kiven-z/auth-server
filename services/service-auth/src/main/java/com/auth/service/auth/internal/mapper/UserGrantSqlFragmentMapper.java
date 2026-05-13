package com.auth.service.auth.internal.mapper;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联表 需要和 {@link GrantTableSubjectType} 对齐，不直接使用但不能漂移
 *
 * @author Bunny
 */
@Mapper
public interface UserGrantSqlFragmentMapper {

}
