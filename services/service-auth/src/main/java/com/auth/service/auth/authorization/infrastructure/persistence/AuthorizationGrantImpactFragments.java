package com.auth.service.auth.authorization.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;

/**
 * 授权影响面反向 SQL 片段（业务键 → userId），仅供 {@code *Mapper.xml} include。
 * <p>
 * 部门路径须与 {@link AuthorizationGrantForwardFragments} 中 DEPT 臂同源。
 * </p>
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationGrantImpactFragments {

}
