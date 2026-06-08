package com.auth.service.system.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.SysUserConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.service.SysUserService;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysUserPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysUserPageVO> voPage = page.convert(SysUserConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		// 不能删除管理员账号和自己账号
		Long currentUserId = SecurityUserUtils.getUserId();
		for (Long userId : idsToDelete) {
			if (PermissionConstant.isSuperAdmin(userId)) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_DELETE_SUPER_ADMIN_FORBIDDEN, userId);
			}
			if (currentUserId != null && currentUserId.equals(userId)) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_DELETE_SELF_FORBIDDEN);
			}
		}

		removeByIds(idsToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysUserBatchStatusForm form) {
		Integer status = form.getStatus();
		if (UserStatus.of(status) == null) {
			throw new SystemBusinessException(SystemAdminResultCode.USER_STATUS_INVALID);
		}

		List<Long> idsToUpdate = form.getIds().stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToUpdate)) {
			return;
		}

		Long currentUserId = SecurityUserUtils.getUserId();
		boolean restrictSelf = status == UserStatus.DISABLED.getCode() || status == UserStatus.LOCKED.getCode();
		for (Long userId : idsToUpdate) {
			if (PermissionConstant.isSuperAdmin(userId)) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_STATUS_SUPER_ADMIN_FORBIDDEN, userId);
			}
			if (restrictSelf && currentUserId != null && currentUserId.equals(userId)) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_STATUS_SELF_FORBIDDEN);
			}
		}

		List<UserEntity> updates = idsToUpdate.stream().map(userId -> {
			UserEntity entity = new UserEntity();
			entity.setId(userId);
			entity.setStatus(status);
			return entity;
		}).toList();
		updateBatchById(updates);
	}

}
