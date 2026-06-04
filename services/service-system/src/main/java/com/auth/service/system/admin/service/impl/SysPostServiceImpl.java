package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.SysPostConverter;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.service.SysPostService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.common.support.FieldChangeSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统岗位服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	private final PostReferenceChecker postReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysPostPageResponse> getPage(Page<SysPostEntity> pageParams, SysPostQuery query) {
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPostPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPostPageResponse> voPage = page.convert(SysPostConverter.INSTANCE::toPageResponse);

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysPostForm form) {
		// 校验部门可挂载岗位
		Long deptId = form.getDeptId();
		postReferenceChecker.requireAssignableDept(deptId);

		// 同一部门内岗位编码唯一
		String postCode = form.getPostCode();
		postReferenceChecker.requirePostCodeUniqueInDept(deptId, postCode, null);

		SysPostEntity entity = SysPostConverter.INSTANCE.toEntity(form);
		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long id, SysPostForm form) {
		SysPostEntity existing = postReferenceChecker.getExistingActive(id);

		// 校验部门可挂载岗位
		Long deptId = form.getDeptId();
		postReferenceChecker.requireAssignableDept(deptId);

		// 同一部门内岗位编码唯一
		String postCode = form.getPostCode();
		postReferenceChecker.requirePostCodeUniqueInDept(deptId, postCode, id);

		// 校验状态是否发生变化
		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		SysPostConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		// 如果状态发生变化，则提交授权失效
		if (statusChanged) {
			postInvalidationTrigger.submitByPostIds(List.of(id), "update:" + id);
		}
	}

}
