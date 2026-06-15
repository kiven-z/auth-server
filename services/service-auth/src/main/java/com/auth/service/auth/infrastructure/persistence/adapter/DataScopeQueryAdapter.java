package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.application.authorization.datascope.RoleScopeRow;
import com.auth.service.auth.application.authorization.datascope.UserScopeRow;
import com.auth.service.auth.application.port.authorization.DataScopeQueryPort;
import com.auth.service.auth.infrastructure.persistence.DeptClosureMapper;
import com.auth.service.auth.infrastructure.persistence.RoleScopeMapper;
import com.auth.service.auth.infrastructure.persistence.UserScopeMapper;
import com.auth.service.auth.infrastructure.persistence.projection.RoleScopeProjection;
import com.auth.service.auth.infrastructure.persistence.projection.UserScopeProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link DataScopeQueryPort} 适配器：projection 映射为 application 读模型
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class DataScopeQueryAdapter implements DataScopeQueryPort {

	private final UserScopeMapper userScopeMapper;

	private final RoleScopeMapper roleScopeMapper;

	private final DeptClosureMapper deptClosureMapper;

	@Override
	public Optional<UserScopeRow> findUserScopeByUserId(Long userId) {
		UserScopeProjection projection = userScopeMapper.selectByUserId(userId);
		if (projection == null) {
			return Optional.empty();
		}
		return Optional.of(new UserScopeRow(projection.getScopeType(), projection.getScopeDeptIds()));
	}

	@Override
	public List<RoleScopeRow> findRoleScopesByRoleCodes(List<String> roleCodes) {
		return roleScopeMapper.selectByRoleCodes(roleCodes).stream().map(this::toRoleScopeRow).toList();
	}

	@Override
	public List<Long> expandDescendantDeptIds(List<Long> ancestorDeptIds) {
		return deptClosureMapper.selectDescendantDeptIdsByAncestorIds(ancestorDeptIds);
	}

	private RoleScopeRow toRoleScopeRow(RoleScopeProjection projection) {
		return new RoleScopeRow(projection.getRoleCode(), projection.getScopeType(), projection.getScopeDeptIds());
	}

}
