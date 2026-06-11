package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.UserPostConverter;
import com.auth.service.system.admin.mapper.UserPostMapper;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.po.userpost.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import com.auth.service.system.admin.service.SysUserPostService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户岗位关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class, readOnly = true)
public class SysUserPostServiceImpl extends ServiceImpl<UserPostMapper, UserPostEntity> implements SysUserPostService {

	private final UserReferenceChecker userReferenceChecker;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<UserPostPageVO> getPage(Long userId, UserPostPageQuery query) {
		userReferenceChecker.getExistingActive(userId);

		Page<UserPostEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserPostPageRowPO> page = baseMapper.selectListByPage(pageParams, userId, query);
		IPage<UserPostPageVO> voPage = page.convert(UserPostConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

}
