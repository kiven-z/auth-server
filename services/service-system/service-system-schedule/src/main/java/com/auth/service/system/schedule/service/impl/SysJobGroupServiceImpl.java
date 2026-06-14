package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobGroupServiceImpl extends ServiceImpl<SysJobGroupMapper, SysJobGroupEntity>
		implements SysJobGroupService {

	private final SysJobMapper sysJobMapper;

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobSchedulerManager sysJobSchedulerManager;

	public SysJobGroupServiceImpl(AuditUserDisplayService auditUserDisplayService, SysJobMapper sysJobMapper,
			SysJobSchedulerManager sysJobSchedulerManager) {
		this.auditUserDisplayService = auditUserDisplayService;
		this.sysJobMapper = sysJobMapper;
		this.sysJobSchedulerManager = sysJobSchedulerManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysJobGroupPageResponse> getPage(Page<SysJobGroupEntity> pageParams, SysJobGroupQuery query) {
		IPage<SysJobGroupPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		IPage<SysJobGroupPageResponse> voPage = page.convert(SysJobGroupConverter.INSTANCE::toPageResponse);

		auditUserDisplayService.enrichAuditUsernames(voPage);
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

		auditUserDisplayService.enrichAuditUsernames(voList);
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
		auditUserDisplayService.enrichAuditUsernames(List.of(detail));
		return detail;
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
			throw new SysJobException(DATA_CODE_DUPLICATE, form.getGroupCode());
		}

		SysJobGroupEntity jobGroup = SysJobGroupConverter.INSTANCE.toEntity(form);
		save(jobGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	public void deleteById(Long id) {
		SysJobGroupEntity jobGroup = getById(id);

		// 查询该分组是否存在
		if (jobGroup == null) {
			throw new SysJobException(DATA_NOT_EXIST);
		}

		// 查询当前是否为系统任务，如果系统任务则不允许删除
		Boolean isSystem = jobGroup.getIsSystem();
		if (BooleanUtil.isTrue(isSystem)) {
			throw new SysJobException(JOB_GROUP_SYSTEM_PROTECTED);
		}

		// 验证分组下无任务，如果存在则不允许删除
		long cnt = sysJobMapper
			.selectCount(Wrappers.<SysJobEntity>lambdaQuery().eq(SysJobEntity::getJobGroup, jobGroup.getGroupCode()));
		if (cnt > 0) {
			throw new SysJobException(JOB_GROUP_HAS_ACTIVE_JOBS, cnt);
		}

		removeById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pauseAllInGroup(String groupCode) {
		try {
			List<SysJobEntity> jobs = sysJobMapper
				.selectList(Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
			List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
			if (CollUtil.isNotEmpty(ids)) {
				sysJobMapper.update(null,
						Wrappers.lambdaUpdate(SysJobEntity.class)
							.set(SysJobEntity::getStatus, false)
							.set(SysJobEntity::getUpdateTime, LocalDateTime.now())
							.in(SysJobEntity::getId, ids));
			}
			sysJobSchedulerManager.pauseGroup(groupCode);
		}
		catch (SchedulerException ex) {
			log.warn("分组暂停失败 groupCode={} : {}", groupCode, ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resumeAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = sysJobMapper
			.selectList(Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		if (jobs.isEmpty()) {
			return;
		}
		List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
		sysJobMapper.update(null,
				Wrappers.lambdaUpdate(SysJobEntity.class)
					.set(SysJobEntity::getStatus, true)
					.set(SysJobEntity::getUpdateTime, LocalDateTime.now())
					.in(SysJobEntity::getId, ids));
		for (SysJobEntity job : jobs) {
			job.setStatus(true);
			try {
				sysJobSchedulerManager.scheduleOrReplace(job);
			}
			catch (SchedulerException ex) {
				log.warn("分组恢复失败 {} / {} : {}", job.getJobGroup(), job.getJobName(), ex.getMessage());
			}
		}
	}

}
