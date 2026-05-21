package com.auth.service.system.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysRoleConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.auth.service.system.admin.service.SysRoleService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	private final AuditUserDisplayService auditUserDisplayService;

	public SysRoleServiceImpl(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysRoleEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysRolePageResponse> page = entityPage.convert(SysRoleConverter.INSTANCE::toPageResponse);

		// 审计用户名
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
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_NOT_FOUND, id);
		}

		return SysRoleConverter.INSTANCE.toDetailVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysRoleCreateForm form) {
		String roleCode = CharSequenceUtil.trim(form.getRoleCode());

		// 检查角色编码是否重复
		long exists = count(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode));
		if (exists > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.ROLE_CODE_DUPLICATE, roleCode);
		}

		// 转换为实体并保存
		SysRoleEntity entity = SysRoleConverter.INSTANCE.toEntity(form);
		entity.setRoleCode(roleCode);
		if (entity.getOrderNum() == null) {
			entity.setOrderNum(0);
		}
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
		return baseMapper.selectRoleOptions(roleName, roleCode).stream().map(entity -> {
			SysRoleOptionVO vo = new SysRoleOptionVO();
			vo.setId(entity.getId());
			vo.setRoleCode(entity.getRoleCode());
			vo.setRoleName(entity.getRoleName());
			return vo;
		}).toList();
	}

}
