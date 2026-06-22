package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.application.port.authorization.DataScopeQueryPort;
import com.auth.service.auth.application.port.authorization.model.RoleScopeRow;
import com.auth.service.auth.application.port.authorization.model.UserScopeRow;
import com.auth.service.auth.infrastructure.persistence.mapper.DeptClosureMapper;
import com.auth.service.auth.infrastructure.persistence.mapper.RoleScopeMapper;
import com.auth.service.auth.infrastructure.persistence.mapper.UserScopeMapper;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<UserScopeRow> findUserScopeByUserId(Long userId) {
		UserScopeProjection projection = userScopeMapper.selectByUserId(userId);
		if (projection == null) {
			return Optional.empty();
		}

		String scopeType = projection.getScopeType();
		String scopeDeptIds = projection.getScopeDeptIds();
		return Optional.of(new UserScopeRow(scopeType, scopeDeptIds));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleScopeRow> findRoleScopesByRoleCodes(List<String> roleCodes) {
		return roleScopeMapper.selectByRoleCodes(roleCodes).stream().map(projection -> {
			String scopeType = projection.getScopeType();
			String scopeDeptIds = projection.getScopeDeptIds();
			return new RoleScopeRow(scopeType, scopeDeptIds);
		}).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> expandDescendantDeptIds(List<Long> ancestorDeptIds) {
		return deptClosureMapper.selectDescendantDeptIdsByAncestorIds(ancestorDeptIds);
	}

}
