package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.convert.SysDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.form.dept.SysDeptUpdateForm;
import com.auth.service.system.admin.service.SysDeptCommandService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.dept.DeptTreeConstants;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 部门写服务：编排主表、闭包存储过程与授权失效。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptCommandServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity>
		implements SysDeptCommandService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptClosureMaintainer deptClosureMaintainer;

	private final DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	public SysDeptCommandServiceImpl(DeptReferenceChecker deptReferenceChecker,
			DeptClosureMaintainer deptClosureMaintainer, DeptAuthorizationInvalidationTrigger deptInvalidationTrigger) {
		this.deptReferenceChecker = deptReferenceChecker;
		this.deptClosureMaintainer = deptClosureMaintainer;
		this.deptInvalidationTrigger = deptInvalidationTrigger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysDeptCreateForm form) {
		long parentId = normalizeParentId(form.getParentId());
		deptReferenceChecker.requireActiveParent(parentId > DeptTreeConstants.ROOT_PARENT_ID ? parentId : null);

		String deptCode = form.getDeptCode();
		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysDeptEntity>lambdaQuery().eq(SysDeptEntity::getDeptCode, deptCode),
				SystemAdminResultCode.DEPT_CODE_DUPLICATE, deptCode);

		SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
		entity.setDeptCode(deptCode);
		entity.setParentId(parentId);
		if (entity.getOrderNum() == null) {
			entity.setOrderNum(0);
		}

		save(entity);
		deptClosureMaintainer.insertNode(entity.getId(), parentId);
		log.info("Department created: id={}, parentId={}, deptCode={}", entity.getId(), parentId, deptCode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMeta(SysDeptUpdateForm form) {
		Long id = form.getId();
		SysDeptEntity existing = getExistingActive(id);

		String newDeptCode = form.getDeptCode();
		boolean codeChanged = FieldChangeSupport.codeChanged(existing.getDeptCode(), newDeptCode);
		if (codeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysDeptEntity>lambdaQuery()
						.eq(SysDeptEntity::getDeptCode, newDeptCode)
						.ne(SysDeptEntity::getId, id),
					SystemAdminResultCode.DEPT_CODE_DUPLICATE, newDeptCode);
		}

		SysDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setDeptCode(newDeptCode);
		updateById(existing);
		log.info("Department meta updated: id={}, deptCode={}", id, newDeptCode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Long deptId, SysDeptMoveForm form) {
		SysDeptEntity existing = getExistingActive(deptId);
		long newParentId = normalizeParentId(form.getParentId());
		long oldParentId = existing.getParentId() != null ? existing.getParentId() : DeptTreeConstants.ROOT_PARENT_ID;

		if (oldParentId == newParentId) {
			return;
		}

		deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);
		deptReferenceChecker.requireActiveParent(newParentId > DeptTreeConstants.ROOT_PARENT_ID ? newParentId : null);

		existing.setParentId(newParentId);
		updateById(existing);
		deptClosureMaintainer.moveNode(deptId, newParentId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move:" + deptId);
		log.info("Department moved: id={}, oldParentId={}, newParentId={}", deptId, oldParentId, newParentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long deptId) {
		getExistingActive(deptId);
		deptReferenceChecker.requireNoActiveChildren(deptId);
		deptReferenceChecker.requireSubtreeUnused(deptId);

		removeById(deptId);
		log.info("Department logically deleted: id={}", deptId);
	}

	private SysDeptEntity getExistingActive(Long deptId) {
		SysDeptEntity existing = getById(deptId);
		if (existing == null || Objects.equals(existing.getIsDeleted(), 1)) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, deptId);
		}
		return existing;
	}

	private static long normalizeParentId(Long parentId) {
		if (parentId == null || parentId < DeptTreeConstants.ROOT_PARENT_ID) {
			return DeptTreeConstants.ROOT_PARENT_ID;
		}
		return parentId;
	}

}
