package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.UserDeptConverter;
import com.auth.service.system.admin.mapper.UserDeptMapper;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.po.userdept.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.auth.service.system.admin.service.SysUserDeptService;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户部门关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDeptEntity> implements SysUserDeptService {

	private final UserReferenceChecker userReferenceChecker;

	private final UserOperationChecker userOperationChecker;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query) {
		// 检查用户是否存在
		userReferenceChecker.getExistingActive(userId);

		Page<UserDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserDeptPageRowPO> page = baseMapper.selectListByPage(pageParams, userId, query);
		IPage<UserDeptPageVO> voPage = page.convert(UserDeptConverter.INSTANCE::toPageVo);

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

}
