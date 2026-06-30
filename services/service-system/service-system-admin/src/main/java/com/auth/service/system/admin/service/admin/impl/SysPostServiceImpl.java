package com.auth.service.system.admin.service.admin.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.service.system.admin.convert.admin.SysPostConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.service.admin.SysPostService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.post.PostWriteGuard;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;

/**
 * 系统岗位写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {

	/**
	 * Excel 导入与批量新增时的持久化批次大小
	 */
	private static final int IMPORT_SAVE_BATCH_SIZE = 500;

	private final PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	private final PostReferenceChecker postReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createBatchFromImport(List<SysPostForm> forms) {
		PostWriteGuard.requireBatchCreatable(baseMapper, forms);
		if (CollUtil.isEmpty(forms)) {
			return;
		}
		List<SysPostEntity> entities = forms.stream().map(SysPostConverter.INSTANCE::toEntity).toList();
		super.saveBatch(entities, IMPORT_SAVE_BATCH_SIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysPostForm form) {
		// 1. 检查当前岗位是否存在
		Long id = form.getId();
		SysPostEntity existing = postReferenceChecker.getExistingActive(id);

		// 2. 检查目标部门是否可挂载
		Long deptId = form.getDeptId();
		List<Long> assignableDeptIds = baseMapper.selectAssignableDeptIds(List.of(deptId));
		if (assignableDeptIds.isEmpty()) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE, deptId);
		}

		// 3. 检查变更后的复合键是否与其它岗位冲突（排除自己所在岗位，是自己放过）
		String postCode = form.getPostCode();
		long otherCount = baseMapper.selectCount(Wrappers.<SysPostEntity>lambdaQuery()
			.eq(SysPostEntity::getDeptId, deptId)
			.eq(SysPostEntity::getPostCode, postCode)
			.ne(SysPostEntity::getId, id));
		if (otherCount > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT, deptId, postCode);
		}

		// 4. 更新岗位
		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		SysPostConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		if (statusChanged) {
			postInvalidationTrigger.submitByPostIds(List.of(id), "update");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		postReferenceChecker.getExistingActive(id);

		long countUserPostByPostId = baseMapper.countUserPostByPostId(id);
		long countGrantPostSubjectByPostId = baseMapper.countGrantPostSubjectByPostId(id);
		if (countUserPostByPostId > 0 || countGrantPostSubjectByPostId > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, id);
		}

		postInvalidationTrigger.submitByPostIds(List.of(id), "delete");
		removeById(id);
	}

}
