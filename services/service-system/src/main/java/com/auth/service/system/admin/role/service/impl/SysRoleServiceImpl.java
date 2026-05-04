package com.auth.service.system.admin.role.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.mapper.SysRoleMapper;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.auth.service.system.admin.role.service.SysRoleService;
import com.auth.service.system.admin.role.support.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	/**
	 * 分页查询角色
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页结果
	 */
	@Override
	public PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysRolePageResponse> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		return PageResponse.of(page);
	}

}
