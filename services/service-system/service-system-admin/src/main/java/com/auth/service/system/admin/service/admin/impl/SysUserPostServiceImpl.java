package com.auth.service.system.admin.service.admin.impl;

import java.util.List;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.user.UserPostConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.user.UserPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.enums.UserOrgRelationStatusEnum;
import com.auth.service.system.admin.model.form.user.UserPostAssignForm;
import com.auth.service.system.admin.model.form.user.UserPostRelationUpdateForm;
import com.auth.service.system.admin.model.po.user.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.user.UserPostPageVO;
import com.auth.service.system.admin.service.admin.SysUserPostService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.user.UserOrgRelationBatchRemoveSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户岗位关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserPostServiceImpl extends ServiceImpl<UserPostMapper, UserPostEntity> implements SysUserPostService {

	private final UserReferenceChecker userReferenceChecker;

	private final PostReferenceChecker postReferenceChecker;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserOrgRelationBatchRemoveSupport userOrgRelationBatchRemoveSupport;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<UserPostPageVO> getPage(Long userId, UserPostPageQuery query) {
		userReferenceChecker.getExistingActive(userId);

		Page<UserPostEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserPostPageRowPO> page = baseMapper.selectListByPage(pageParams, userId, query);
		IPage<UserPostPageVO> voPage = page.convert(UserPostConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(Long userId, UserPostAssignForm form) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		SysPostEntity post = postReferenceChecker.getExistingActive(form.getPostId());
		if (BooleanUtil.isFalse(post.getStatus())) {
			log.warn("post unavailable: id={}", form.getPostId());
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE);
		}

		if (baseMapper.countByUserIdAndPostId(userId, form.getPostId()) > 0) {
			log.warn("User post relation duplicate: userId={}, postId={}", userId, form.getPostId());
			throw new SystemBusinessException(SystemAdminResultCode.USER_POST_DUPLICATE);
		}

		applyPrimaryPostRules(userId, form.getStatus(), form.getIsPrimary());

		UserPostEntity entity = UserPostConverter.INSTANCE.toEntity(form);
		entity.setUserId(userId);
		save(entity);

		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "create-post");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long userId, Long id, UserPostRelationUpdateForm form) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		UserPostEntity existing = baseMapper.selectByIdAndUserId(id, userId);
		if (existing == null) {
			log.warn("user post relation not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		applyPrimaryPostRules(userId, form.getStatus(), form.getIsPrimary());

		UserPostConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "update-post");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeBatch(Long userId, List<Long> ids) {
		userOrgRelationBatchRemoveSupport.removeBatch(userId, ids, this::removeByIds, "delete-post");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAll(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		if (baseMapper.deleteByUserId(userId) > 0) {
			userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "clear-post");
		}
	}

	/**
	 * 主岗位规则：非正常状态不可设为主岗位；设为主岗位时降级该用户其他主岗位。
	 * @param userId 用户 ID
	 * @param status 关联状态
	 * @param isPrimary 是否主岗位
	 */
	private void applyPrimaryPostRules(Long userId, Integer status, Boolean isPrimary) {
		boolean primary = BooleanUtil.isTrue(isPrimary);

		if (primary && !UserOrgRelationStatusEnum.canBePrimary(status)) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_POST_PRIMARY_NOT_ELIGIBLE);
		}

		if (primary) {
			baseMapper.demotePrimaryByUserId(userId);
		}
	}

}
