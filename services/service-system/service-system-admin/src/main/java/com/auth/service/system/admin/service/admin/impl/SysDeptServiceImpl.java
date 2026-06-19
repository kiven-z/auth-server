package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.service.system.admin.convert.sys.SysDeptConverter;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门写服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptClosureMaintainer deptClosureMaintainer;

	private final DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysDeptForm form) {
		Long parentId = form.getParentId();
		parentId = deptReferenceChecker.normalizeAndRequireActiveParent(parentId);

		String deptCode = form.getDeptCode();
		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysDeptEntity>lambdaQuery().eq(SysDeptEntity::getDeptCode, deptCode),
				SystemCommonResultCode.DATA_CODE_DUPLICATE, deptCode);

		SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
		entity.setParentId(parentId);
		save(entity);

		deptClosureMaintainer.insertNode(entity.getId(), parentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMeta(SysDeptForm form) {
		Long id = form.getId();
		SysDeptEntity existing = deptReferenceChecker.getExistingActive(id);

		String newDeptCode = form.getDeptCode();
		String rawDeptCode = existing.getDeptCode();
		boolean codeChanged = FieldChangeSupport.codeChanged(rawDeptCode, newDeptCode);
		if (codeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysDeptEntity>lambdaQuery()
						.eq(SysDeptEntity::getDeptCode, newDeptCode)
						.ne(SysDeptEntity::getId, id),
					SystemCommonResultCode.DATA_CODE_DUPLICATE, newDeptCode);
		}

		long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());
		boolean parentChanged = TreeParentIdUtil.normalize(existing.getParentId()) != newParentId;
		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		if (parentChanged) {
			deptReferenceChecker.requireMoveTargetValid(id, newParentId);
			existing.setParentId(newParentId);
		}

		SysDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		if (parentChanged) {
			deptClosureMaintainer.moveNode(id, newParentId);
		}

		if (FieldChangeSupport.anyChanged(parentChanged, statusChanged)) {
			String invalidationOp = parentChanged ? "move" : "update";
			deptInvalidationTrigger.submitByDeptIds(List.of(id), invalidationOp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Long deptId, SysDeptMoveForm form) {
		SysDeptEntity existing = deptReferenceChecker.getExistingActive(deptId);
		long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());

		if (TreeParentIdUtil.normalize(existing.getParentId()) == newParentId) {
			return;
		}

		deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);
		existing.setParentId(newParentId);
		updateById(existing);

		deptClosureMaintainer.moveNode(deptId, newParentId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long deptId) {
		deptReferenceChecker.getExistingActive(deptId);

		if (baseMapper.countActiveDirectChildren(deptId) > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN, deptId);
		}
		deptReferenceChecker.requireSubtreeUnused(deptId);

		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "delete");

		deptClosureMaintainer.removeClosurePaths(deptId);

		removeById(deptId);
	}

}
