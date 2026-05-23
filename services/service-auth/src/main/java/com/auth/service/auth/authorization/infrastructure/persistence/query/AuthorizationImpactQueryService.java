package com.auth.service.auth.authorization.infrastructure.persistence.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationImpactMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权影响面查询
 *
 * @author Bunny
 */
@Service
public class AuthorizationImpactQueryService implements AuthorizationImpactQueryPort {

	private final AuthorizationImpactMapper authorizationImpactMapper;

	public AuthorizationImpactQueryService(AuthorizationImpactMapper authorizationImpactMapper) {
		this.authorizationImpactMapper = authorizationImpactMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes) {
		if (CollUtil.isEmpty(roleCodes)) {
			return Set.of();
		}
		List<String> codes = roleCodes.stream().filter(CharSequenceUtil::isNotBlank).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}
		return authorizationImpactMapper.selectUserIdsByRoleCodes(codes)
			.stream()
			.collect(Collectors.toUnmodifiableSet());
	}

}
