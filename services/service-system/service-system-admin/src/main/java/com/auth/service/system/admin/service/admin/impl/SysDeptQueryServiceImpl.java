package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.grant.GrantTableSubjectRoleConverter;
import com.auth.service.system.admin.convert.sys.SysDeptConverter;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.dept.SysDeptQueryMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundPostPO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.admin.SysDeptQueryService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 部门只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysDeptQueryServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final DeptReferenceChecker deptReferenceChecker;

	private final SysDeptQueryMapper sysDeptQueryMapper;

	private final GrantTableMapper grantTableMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptListVO> listFlat(SysDeptListQuery query) {
		List<SysDeptEntity> entities = baseMapper.selectListByQuery(query);
		List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoList(entities);

		auditUserDisplayService.enrichAuditUsernames(list);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptPageQuery query) {
		IPage<SysDeptPageRowPO> poPage = baseMapper.selectListByPage(pageParams, query);
		IPage<SysDeptListVO> result = poPage.convert(SysDeptConverter.INSTANCE::toListVoFromPageRow);

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
		List<SysDeptBoundUserPO> boundUsers = sysDeptQueryMapper.selectBoundUsersByDeptId(id);
		detail.setBoundUsers(SysDeptConverter.INSTANCE.toBoundUserItemList(boundUsers));

		List<SysDeptBoundPostPO> boundPosts = sysDeptQueryMapper.selectBoundPostsByDeptId(id);
		detail.setBoundPosts(SysDeptConverter.INSTANCE.toBoundPostItemList(boundPosts));

		List<GrantTableBoundRolePO> poList = grantTableMapper
			.selectBoundRolesBySubject(GrantTableSubjectType.DEPT.name(), id);
		detail.setBoundRoles(GrantTableSubjectRoleConverter.INSTANCE.toBoundRoleItemList(poList));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
		return detail;
	}

}
