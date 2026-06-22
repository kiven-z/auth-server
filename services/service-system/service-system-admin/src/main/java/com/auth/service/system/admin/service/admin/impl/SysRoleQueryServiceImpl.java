package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.grant.GrantTableBoundSubjectConverter;
import com.auth.service.system.admin.convert.sys.SysPermissionConverter;
import com.auth.service.system.admin.convert.sys.SysRoleConverter;
import com.auth.service.system.admin.convert.sys.SysRolePermissionConverter;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundDeptPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundPostPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundUserPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.auth.service.system.admin.service.admin.SysRoleQueryService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
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
 * 系统角色只读查询实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysRoleQueryServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final GrantTableMapper grantTableMapper;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final SysMenuRoleMapper sysMenuRoleMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysRolePageVO> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysRoleEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysRolePageVO> page = entityPage.convert(SysRoleConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(page);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysRoleDetailVO getDetail(Long id) {
		SysRoleEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, id);
		}

		SysRoleDetailVO detail = SysRoleConverter.INSTANCE.toDetailVo(entity);
		String roleCode = entity.getRoleCode();

		List<SysRoleAssignedPermissionPO> permissions = sysRolePermissionMapper.selectAssignedPermissionsByRoleId(id);
		detail.setBoundPermissions(SysRolePermissionConverter.INSTANCE.toBoundPermissionItemList(permissions));

		List<SysPermissionBoundMenuPO> permissionBoundMenus = sysMenuRoleMapper.selectBoundMenusByRoleId(id);
		detail.setBoundMenus(SysPermissionConverter.INSTANCE.toBoundMenuItemList(permissionBoundMenus));

		List<GrantTableBoundUserPO> grantTableBoundUsers = grantTableMapper.selectBoundUsersByRoleCode(roleCode);
		detail.setBoundUsers(GrantTableBoundSubjectConverter.INSTANCE.toBoundUserItemList(grantTableBoundUsers));

		List<GrantTableBoundDeptPO> grantTableBoundDeptList = grantTableMapper.selectBoundDeptsByRoleCode(roleCode);
		detail.setBoundDepts(GrantTableBoundSubjectConverter.INSTANCE.toBoundDeptItemList(grantTableBoundDeptList));

		List<GrantTableBoundPostPO> grantTableBoundPosts = grantTableMapper.selectBoundPostsByRoleCode(roleCode);
		detail.setBoundPosts(GrantTableBoundSubjectConverter.INSTANCE.toBoundPostItemList(grantTableBoundPosts));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail));
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
		List<SysRoleEntity> entities = baseMapper.selectRoleOptions(roleName, roleCode);
		return SysRoleConverter.INSTANCE.toOptionVoList(entities);
	}

}
