package com.auth.service.system.admin.service.admin.impl;

import java.util.Collections;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.SysPostConverter;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.auth.service.system.admin.service.admin.SysPostQueryService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统岗位只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysPostQueryServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PostReferenceChecker postReferenceChecker;

	private final GrantTableMapper grantTableMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysPostPageVO> getPage(Page<SysPostEntity> pageParams, SysPostQuery query) {
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPostPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPostPageVO> voPage = page.convert(SysPostConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysPostSearchItemVO> search(String keyword, Boolean status, Integer limit) {
		int cappedLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));

		List<SysPostSearchItemPO> poList = baseMapper.search(keyword, status, cappedLimit);
		return SysPostConverter.INSTANCE.toSearchItemVoList(poList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysPostDetailVO getDetail(Long id) {
		SysPostEntity entity = postReferenceChecker.getExistingActive(id);

		SysPostDetailVO detail = SysPostConverter.INSTANCE.toDetailVo(entity);
		DeptReferencePO deptList = baseMapper.selectBoundDeptByPostId(id);
		detail.setBoundDept(ReferenceConverter.INSTANCE.toDeptReference(deptList));

		List<SysPostBoundUserPO> userList = baseMapper.selectBoundUsersByPostId(id);
		detail.setBoundUsers(ReferenceConverter.INSTANCE.toPostBoundUserReferenceList(userList));

		List<RoleReferencePO> roleList = grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(),
				id);
		detail.setBoundRoles(ReferenceConverter.INSTANCE.toRoleReferenceList(roleList));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
