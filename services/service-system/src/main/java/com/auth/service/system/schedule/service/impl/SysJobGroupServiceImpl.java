package com.auth.service.system.schedule.service.impl;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.SysJobGroupConverter;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobGroupPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;

/**
 * 任务分组实现
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobGroupServiceImpl extends ServiceImpl<SysJobGroupMapper, SysJobGroupEntity>
		implements SysJobGroupService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobMapper sysJobMapper;

	public SysJobGroupServiceImpl(AuditUserDisplayService auditUserDisplayService, SysJobMapper sysJobMapper) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.sysJobMapper = sysJobMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobGroupPageResponse> getPage(Page<SysJobGroupEntity> pageParams, SysJobGroupQuery query) {
		IPage<SysJobGroupPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<SysJobGroupPageResponse> voPage = page
			.convert(po -> BeanUtil.copyProperties(po, SysJobGroupPageResponse.class));
		auditUserDisplayService.enrichAuditUsernames(voPage);

		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysJobGroupPageResponse> listEnabledOptions(String keyword, int limit) {
		String searchKeyword = CharSequenceUtil.trim(keyword);
		List<SysJobGroupPageRowPO> list = baseMapper.selectEnabledOptions(searchKeyword, limit);
		List<SysJobGroupPageResponse> voList = list.stream()
			.map(po -> BeanUtil.copyProperties(po, SysJobGroupPageResponse.class))
			.toList();
		auditUserDisplayService.enrichAuditUsernames(voList);

		return voList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SysJobGroupForm form) {
		// 验证分组编码是否已存在
		long exists = count(
				Wrappers.<SysJobGroupEntity>lambdaQuery().eq(SysJobGroupEntity::getGroupCode, form.getGroupCode()));
		if (exists > 0) {
			throw new SystemBusinessException(JOB_GROUP_CODE_DUPLICATE);
		}

		SysJobGroupEntity jobGroup = SysJobGroupConverter.INSTANCE.toEntity(form);

		// 设置默认值
		if (jobGroup.getIsSystem() == null) {
			jobGroup.setIsSystem(false);
		}
		if (jobGroup.getOrderNum() == null) {
			jobGroup.setOrderNum(0);
		}

		save(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(SysJobGroupUpdateForm form) {
		SysJobGroupEntity jobGroup = getById(form.getId());
		if (jobGroup == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		SysJobGroupConverter.INSTANCE.updateEntity(form, jobGroup);
		updateById(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long id) {
		SysJobGroupEntity jobGroup = getById(id);

		// 查询该分组是否存在
		if (jobGroup == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 查询当前是否为系统任务，如果系统任务则不允许删除
		Boolean isSystem = jobGroup.getIsSystem();
		if (BooleanUtil.isTrue(isSystem)) {
			throw new SystemBusinessException(JOB_GROUP_SYSTEM_PROTECTED);
		}

		// 验证分组下无任务，如果存在则不允许删除
		long cnt = sysJobMapper
			.selectCount(Wrappers.<SysJobEntity>lambdaQuery().eq(SysJobEntity::getJobGroup, jobGroup.getGroupCode()));
		if (cnt > 0) {
			throw new SystemBusinessException(JOB_GROUP_HAS_ACTIVE_JOBS, cnt);
		}

		removeById(id);
	}

}
