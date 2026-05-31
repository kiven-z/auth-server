package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.form.dept.SysDeptUpdateForm;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.auth.service.system.admin.support.dept.DeptReferenceChecker.ROOT_PARENT_ID;

/**
 * 部门服务实现（查询）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptClosureMaintainer deptClosureMaintainer;

	private final DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	private static long normalizeParentId(Long parentId) {
		if (parentId == null || parentId < ROOT_PARENT_ID) {
			return ROOT_PARENT_ID;
		}
		return parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptListVO> listFlat(SysDeptQuery query) {
		List<SysDeptEntity> entities = baseMapper.selectListByQuery(query);
		List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoList(entities);
		auditUserDisplayService.enrichAuditUsernames(list);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptQuery query) {
		IPage<SysDeptEntity> entityPage = baseMapper.selectPageByQuery(pageParams, query);
		IPage<SysDeptListVO> result = entityPage.convert(SysDeptConverter.INSTANCE::toListVo);
		auditUserDisplayService.enrichAuditUsernames(result);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysDeptDetailVO getDetail(Long id) {
		SysDeptEntity entity = baseMapper.selectById(id);
		if (entity == null || Integer.valueOf(1).equals(entity.getIsDeleted())) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, id);
		}
		return SysDeptConverter.INSTANCE.toDetailVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysDeptCreateForm form) {
		long parentId = normalizeParentId(form.getParentId());
		deptReferenceChecker.requireActiveParent(parentId > ROOT_PARENT_ID ? parentId : null);

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
		long oldParentId = existing.getParentId() != null ? existing.getParentId() : ROOT_PARENT_ID;

		if (oldParentId == newParentId) {
			return;
		}

		deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);
		deptReferenceChecker.requireActiveParent(newParentId > ROOT_PARENT_ID ? newParentId : null);

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

}
