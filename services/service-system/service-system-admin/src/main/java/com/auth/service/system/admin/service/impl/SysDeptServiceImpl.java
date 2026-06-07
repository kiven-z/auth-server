package com.auth.service.system.admin.service.impl;

import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.GrantTableSubjectRoleConverter;
import com.auth.service.system.admin.convert.SysDeptConverter;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundPostPO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
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

/**
 * 部门服务实现
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

	private final GrantTableMapper grantTableMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptListVO> listFlat(SysDeptQuery query) {
		List<SysDeptEntity> entities = baseMapper.selectListByQuery(query);
		List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoList(entities);

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(list);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptQuery query) {
		IPage<SysDeptPageRowPO> poPage = baseMapper.selectListByPage(pageParams, query);
		IPage<SysDeptListVO> result = poPage.convert(SysDeptConverter.INSTANCE::toListVoFromPageRow);

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(result);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysDeptDetailVO getDetail(Long id) {
		SysDeptEntity entity = deptReferenceChecker.getExistingActive(id);

		SysDeptDetailVO detail = SysDeptConverter.INSTANCE.toDetailVo(entity);
		List<SysDeptBoundUserPO> boundUsers = sysDeptDetailMapper.selectBoundUsersByDeptId(id);
		detail.setBoundUsers(SysDeptConverter.INSTANCE.toBoundUserItemList(boundUsers));

		List<SysDeptBoundPostPO> boundPosts = sysDeptDetailMapper.selectBoundPostsByDeptId(id);
		detail.setBoundPosts(SysDeptConverter.INSTANCE.toBoundPostItemList(boundPosts));

		List<GrantTableBoundRolePO> poList = grantTableMapper
			.selectBoundRolesBySubject(GrantTableSubjectType.DEPT.name(), id, RoleTypeEnum.ORG_GRANT_TYPES);
		detail.setBoundRoles(GrantTableSubjectRoleConverter.INSTANCE.toBoundRoleItemList(poList));

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysDeptForm form) {
		// 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
		long parentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());

		// 校验部门编码唯一性
		String deptCode = form.getDeptCode();
		BusinessKeyAssert.requireAbsent(this,
				Wrappers.<SysDeptEntity>lambdaQuery().eq(SysDeptEntity::getDeptCode, deptCode),
				SystemCommonResultCode.DATA_CODE_DUPLICATE, deptCode);

		SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
		entity.setParentId(parentId);
		save(entity);

		// 维护部门闭包
		deptClosureMaintainer.insertNode(entity.getId(), parentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMeta(SysDeptForm form) {
		Long id = form.getId();
		SysDeptEntity existing = deptReferenceChecker.getExistingActive(id);

		// 校验部门编码唯一性
		String newDeptCode = form.getDeptCode();
		boolean codeChanged = FieldChangeSupport.codeChanged(existing.getDeptCode(), newDeptCode);
		if (codeChanged) {
			BusinessKeyAssert.requireAbsent(this,
					Wrappers.<SysDeptEntity>lambdaQuery()
						.eq(SysDeptEntity::getDeptCode, newDeptCode)
						.ne(SysDeptEntity::getId, id),
					SystemCommonResultCode.DATA_CODE_DUPLICATE, newDeptCode);
		}

		// 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
		long newParentId = deptReferenceChecker.normalizeAndRequireActiveParent(form.getParentId());
		boolean parentChanged = TreeParentIdUtil.normalize(existing.getParentId()) != newParentId;
		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		if (parentChanged) {
			deptReferenceChecker.requireMoveTargetValid(id, newParentId);
			existing.setParentId(newParentId);
		}

		SysDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		// 维护部门闭包
		if (parentChanged) {
			deptClosureMaintainer.moveNode(id, newParentId);
		}

		// 父级或启用状态影响授权展开与数据范围，其余元数据变更不刷新画像
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

		// 校验移动目标是否有效
		deptReferenceChecker.requireMoveTargetValid(deptId, newParentId);
		existing.setParentId(newParentId);
		updateById(existing);

		// 维护部门闭包
		deptClosureMaintainer.moveNode(deptId, newParentId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long deptId) {
		// 校验部门是否存在
		deptReferenceChecker.getExistingActive(deptId);

		// 校验部门是否有子部门
		deptReferenceChecker.requireNoActiveChildren(deptId);
		// 校验子树是否仍被用户、授权或岗位引用
		deptReferenceChecker.requireSubtreeUnused(deptId);

		// 先提交失效（反查依赖 dept_closure）再清理闭包与主表
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "delete");

		// 维护部门闭包
		deptClosureMaintainer.removeClosurePaths(deptId);

		removeById(deptId);
	}

}
