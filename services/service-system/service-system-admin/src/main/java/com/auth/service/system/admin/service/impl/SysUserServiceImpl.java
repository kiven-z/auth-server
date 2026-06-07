package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.SysUserConverter;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.service.SysUserService;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统用户服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserService {

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysUserPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysUserPageVO> voPage = page.convert(SysUserConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

}
