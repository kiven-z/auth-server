package com.auth.service.system.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.common.mapper.AuditUserDisplayMapper;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 审计用户展示名
 *
 * @author Bunny
 */
@Service
public class AuditUserDisplayServiceImpl implements AuditUserDisplayService {

	private final AuditUserDisplayMapper auditUserDisplayMapper;

	public AuditUserDisplayServiceImpl(AuditUserDisplayMapper auditUserDisplayMapper) {
		this.auditUserDisplayMapper = auditUserDisplayMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Long, String> mapUsernamesByIds(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Collections.emptyMap();
		}
		List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(distinctIds)) {
			return Collections.emptyMap();
		}
		return auditUserDisplayMapper.selectBatchIds(distinctIds)
			.stream()
			.collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends BaseResponse> void enrichAuditUsernames(IPage<T> page) {
		if (page == null) {
			return;
		}
		enrichAuditUsernames(page.getRecords());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enrichAuditUsernames(List<? extends BaseResponse> records) {
		if (CollUtil.isEmpty(records)) {
			return;
		}

		List<Long> list = records.stream()
			.flatMap(vo -> Stream.of(vo.getCreateUser(), vo.getUpdateUser()))
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Map<Long, String> usernameMap = mapUsernamesByIds(list);

		for (BaseResponse vo : records) {
			String createUsername = usernameMap.get(vo.getCreateUser());
			vo.setCreateUsername(createUsername);

			String updateUsername = usernameMap.get(vo.getUpdateUser());
			vo.setUpdateUsername(updateUsername);
		}
	}

}
