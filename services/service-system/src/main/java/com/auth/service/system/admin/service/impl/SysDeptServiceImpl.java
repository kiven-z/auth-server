package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
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

import java.util.Collections;
import java.util.List;

import static com.auth.service.system.admin.support.dept.DeptReferenceChecker.ROOT_PARENT_ID;

/**
 * 部门服务实现。
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

	private final SysDeptDetailMapper sysDeptDetailMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptListVO> listFlat(SysDeptQuery query) {
		List<SysDeptEntity> entities = baseMapper.selectListByQuery(query);
		List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoList(entities);

		// 补充审计用户名。
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

		// 补充审计用户名。
		auditUserDisplayService.enrichAuditUsernames(result);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysDeptDetailVO getDetail(Long id) {
		SysDeptEntity entity = getExistingActive(id);

		// 转换为详情 VO。
		SysDeptDetailVO detail = SysDeptConverter.INSTANCE.toDetailVo(entity);
		detail.setBoundUsers(
				SysDeptConverter.INSTANCE.toBoundUserItemList(sysDeptDetailMapper.selectBoundUsersByDeptId(id)));
		detail.setBoundPosts(
				SysDeptConverter.INSTANCE.toBoundPostItemList(sysDeptDetailMapper.selectBoundPostsByDeptId(id)));
		detail.setBoundRoles(
				SysDeptConverter.INSTANCE.toBoundRoleItemList(sysDeptDetailMapper.selectBoundRolesByDeptId(id)));

		// 补充审计用户名。
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysDeptCreateForm form) {
		// 规范化父部门 ID 并校验父部门存在（顶级跳过校验）。
		long parentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());

		// 校验部门编码唯一性。
		String deptCode = form.getDeptCode();
		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysDeptEntity>lambdaQuery().eq(SysDeptEntity::getDeptCode, deptCode),
				SystemAdminResultCode.DEPT_CODE_DUPLICATE, deptCode);

		// 转换为实体对象。
		SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
		entity.setDeptCode(deptCode);
		entity.setParentId(parentId);

		// 保存部门。
		save(entity);
		// 维护部门闭包。
		deptClosureMaintainer.insertNode(entity.getId(), parentId);
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(Long deptId, SysDeptMoveForm form) {
		SysDeptEntity existing = getExistingActive(deptId);
		long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());
		long oldParentId = existing.getParentId() != null ? existing.getParentId() : ROOT_PARENT_ID;

		if (oldParentId == newParentId) {
			return;
		}

		deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);

		existing.setParentId(newParentId);
		updateById(existing);
		deptClosureMaintainer.moveNode(deptId, newParentId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move:" + deptId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long deptId) {
		getExistingActive(deptId);
		deptReferenceChecker.requireNoActiveChildren(deptId);
		deptReferenceChecker.requireSubtreeUnused(deptId);

		deptClosureMaintainer.removeClosurePaths(deptId);
		removeById(deptId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "delete:" + deptId);
		log.info("Department deleted (sys_dept logical, dept_closure physical): id={}", deptId);
	}

	/**
	 * 加载未删除部门，不存在则抛业务异常。
	 * @param deptId 部门 ID
	 * @return 部门实体
	 */
	private SysDeptEntity getExistingActive(Long deptId) {
		SysDeptEntity existing = getById(deptId);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, deptId);
		}
		return existing;
	}

}
