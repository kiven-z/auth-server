package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.convert.SysJobGroupConverter;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobGroupPageRowPO;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageResponse;
import com.auth.service.system.schedule.service.SysJobGroupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_CODE_DUPLICATE;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_SYSTEM_PROTECTED;

/**
 * 任务分组实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysJobGroupServiceImpl extends ServiceImpl<SysJobGroupMapper, SysJobGroupEntity>
		implements SysJobGroupService {

	private final SysJobMapper sysJobMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobGroupPageResponse> getPage(Page<SysJobGroupEntity> pageParams, SysJobGroupQuery query) {
		IPage<SysJobGroupPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobGroupPageResponse> voPage = page.convert(SysJobGroupConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysJobGroupPageResponse> listEnabledOptions(String keyword, int limit) {
		List<SysJobGroupPageRowPO> list = baseMapper.selectEnabledOptions(keyword, limit);
		List<SysJobGroupPageResponse> voList = list.stream()
			.map(SysJobGroupConverter.INSTANCE::toPageResponse)
			.toList();

		auditUserDisplayService.enrichAuditUsernames(voList, null, null);
		return voList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysJobGroupDetailVO getDetail(Long id) {
		SysJobGroupEntity entity = getById(id);
		if (entity == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobGroupDetailVO detail = SysJobGroupConverter.INSTANCE.toDetailVo(entity);
		auditUserDisplayService.enrichAuditUsernames(List.of(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void create(SysJobGroupForm form) {
		long exists = count(
				Wrappers.<SysJobGroupEntity>lambdaQuery().eq(SysJobGroupEntity::getGroupCode, form.getGroupCode()));
		if (exists > 0) {
			throw new SysJobException(DATA_CODE_DUPLICATE, form.getGroupCode());
		}

		SysJobGroupEntity jobGroup = SysJobGroupConverter.INSTANCE.toEntity(form);
		save(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysJobGroupUpdateForm form) {
		SysJobGroupEntity jobGroup = getById(form.getId());
		if (jobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		SysJobGroupConverter.INSTANCE.updateEntity(form, jobGroup);
		updateById(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		SysJobGroupEntity jobGroup = getById(id);
		if (jobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		// 系统内置的不能删除
		if (BooleanUtil.isTrue(jobGroup.getIsSystem())) {
			throw new SysJobException(JOB_GROUP_SYSTEM_PROTECTED);
		}

		// 分组下还有绑定任务不能删
		long cnt = sysJobMapper
			.selectCount(Wrappers.<SysJobEntity>lambdaQuery().eq(SysJobEntity::getJobGroup, jobGroup.getGroupCode()));
		if (cnt > 0) {
			throw new SysJobException(JOB_GROUP_HAS_ACTIVE_JOBS, cnt);
		}

		removeById(id);
	}

}
