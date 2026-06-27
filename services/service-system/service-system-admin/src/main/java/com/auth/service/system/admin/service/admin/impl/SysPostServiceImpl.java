package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.service.system.admin.convert.admin.SysPostConverter;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.service.admin.SysPostService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统岗位写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {

	private final PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	private final PostReferenceChecker postReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysPostForm form) {
		Long deptId = form.getDeptId();
		postReferenceChecker.requireAssignableDept(deptId);

		String postCode = form.getPostCode();
		postReferenceChecker.requirePostCodeUniqueInDept(deptId, postCode, null);

		SysPostEntity entity = SysPostConverter.INSTANCE.toEntity(form);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long id, SysPostForm form) {
		SysPostEntity existing = postReferenceChecker.getExistingActive(id);

		Long deptId = form.getDeptId();
		postReferenceChecker.requireAssignableDept(deptId);

		String postCode = form.getPostCode();
		postReferenceChecker.requirePostCodeUniqueInDept(deptId, postCode, id);

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
		postReferenceChecker.requireDeletable(id);
		postInvalidationTrigger.submitByPostIds(List.of(id), "delete");

		removeById(id);
	}

}
