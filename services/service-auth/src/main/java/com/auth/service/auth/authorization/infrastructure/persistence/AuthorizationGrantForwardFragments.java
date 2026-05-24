package com.auth.service.auth.authorization.infrastructure.persistence;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 授权正向 SQL 片段（userId → 主体 / 角色 / 数据范围），仅供 {@code *Mapper.xml} include。
 * <p>
 * 与 {@link GrantTableSubjectType} 语义对齐；修改展开规则时只维护
 * {@code AuthorizationGrantForwardFragments.xml}。
 * </p>
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationGrantForwardFragments {

}
