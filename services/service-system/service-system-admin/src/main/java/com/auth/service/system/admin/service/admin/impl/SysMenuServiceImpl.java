package com.auth.service.system.admin.service.admin.impl;

import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.service.system.admin.convert.admin.menu.SysMenuConverter;
import com.auth.service.system.admin.mapper.menu.SysMenuMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.service.admin.SysMenuService;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 菜单写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

	private final MenuReferenceChecker menuReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long create(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(Objects.requireNonNullElse(entity.getParentId(), 0L));
		save(entity);

		return entity.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(Objects.requireNonNullElse(entity.getParentId(), 0L));
		updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Long menuId, SysMenuMoveForm form) {
		SysMenuEntity existing = menuReferenceChecker.getExisting(menuId);
		long newParentId = menuReferenceChecker.normalizeAndRequireParent(form.getParentId());
		Long rawParentId = existing.getParentId();

		if (TreeParentIdUtil.normalize(rawParentId) == newParentId) {
			return;
		}

		menuReferenceChecker.requireMoveTargetValid(menuId, newParentId);
		existing.setParentId(newParentId);
		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(SysMenuBatchStatusForm form) {
		Boolean status = form.getStatus();
		List<SysMenuEntity> list = form.getIds().stream().filter(Objects::nonNull).map(id -> {
			SysMenuEntity sysMenuEntity = new SysMenuEntity();
			sysMenuEntity.setId(id);
			sysMenuEntity.setStatus(status);
			return sysMenuEntity;
		}).toList();
		super.updateBatchById(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		Long blockedParentId = baseMapper.selectFirstBlockedParentId(idsToDelete);
		if (blockedParentId != null) {
			log.warn("Tree has active children: id={}", blockedParentId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN);
		}

		removeByIds(idsToDelete);
	}

}
