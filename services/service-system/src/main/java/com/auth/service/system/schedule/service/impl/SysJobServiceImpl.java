package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.SysJobGroupMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.model.entity.SysJobGroupEntity;
import com.auth.service.system.log.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobService;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.auth.service.system.common.exception.SystemResultCodeEnum.*;
import static com.auth.service.system.schedule.model.constans.SysJobQuartzDataKeys.JOB_ZONE;

/**
 * 定时任务与 Quartz 同步实现
 *
 * @author bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJobEntity> implements SysJobService {

	private final SysJobSchedulerManager quartzAdmin;

	private final QuartzTaskRegistry quartzTaskRegistry;

	private final ApplicationContext applicationContext;

	private final AuditUserDisplayService auditUserDisplayService;

	private final SysJobGroupMapper sysJobGroupMapper;

	private final JobLogMapper jobLogMapper;

	private final Scheduler scheduler;

	public SysJobServiceImpl(SysJobSchedulerManager quartzAdmin, QuartzTaskRegistry quartzTaskRegistry,
			ApplicationContext applicationContext, AuditUserDisplayService auditUserDisplayService,
			SysJobGroupMapper sysJobGroupMapper, JobLogMapper jobLogMapper, Scheduler scheduler) {
		this.quartzAdmin = quartzAdmin;
		this.quartzTaskRegistry = quartzTaskRegistry;
		this.applicationContext = applicationContext;
		this.auditUserDisplayService = auditUserDisplayService;
		this.sysJobGroupMapper = sysJobGroupMapper;
		this.jobLogMapper = jobLogMapper;
		this.scheduler = scheduler;
	}

	private static LocalDateTime toLocal(Date date) {
		if (date == null) {
			return null;
		}
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), JOB_ZONE);
	}

	/**
	 * 从数据库同步所有定时任务
	 */
	@Override
	public void syncAllFromDatabase() {
		List<SysJobEntity> all = list();
		for (SysJobEntity job : all) {
			try {
				quartzAdmin.scheduleOrReplace(job);
			}
			catch (Exception e) {
				log.error("启动同步定时任务失败: {} / {}", job.getJobGroup(), job.getJobName(), e);
			}
		}
	}

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	@Override
	public PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query) {
		IPage<SysJobPageRowPO> page = baseMapper.selectListByPage(pageParams, query);
		// PO 转 VO
		IPage<SysJobPageResponse> voPage = page.convert(po -> {
			SysJobPageResponse vo = new SysJobPageResponse();
			BeanUtils.copyProperties(po, vo);
			enrichFireTimes(vo);
			return vo;
		});
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	@Override
	public SysJobDetailVO getDetail(Long id) {
		// 获取任务
		SysJobEntity sysJob = getById(id);
		if (sysJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 创建VO
		SysJobDetailVO vo = new SysJobDetailVO();
		BeanUtils.copyProperties(sysJob, vo);

		// 创建分页响应
		SysJobPageResponse tmp = new SysJobPageResponse();
		tmp.setJobName(sysJob.getJobName());
		tmp.setJobGroup(sysJob.getJobGroup());
		enrichFireTimes(tmp);

		// 设置触发时间
		vo.setPreviousFireTime(tmp.getPreviousFireTime());
		vo.setNextFireTime(tmp.getNextFireTime());
		return vo;
	}

	/**
	 * 创建
	 * @param form 表单
	 */
	@Override
	public void create(SysJobCreateForm form) {
		// 验证任务分组是否启用
		validateGroupEnabled(form.getJobGroup());

		// 验证任务类和执行目标
		validateModeAndSecurity(form.getJobClass(), form.getInvokeTarget());

		SysJobEntity sysJob = BeanUtil.copyProperties(form, SysJobEntity.class);
		save(sysJob);
		try {
			quartzAdmin.scheduleOrReplace(sysJob);
		}
		catch (SchedulerException ex) {
			log.error("注册 Quartz 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_REGISTER_FAILED, ex.getMessage());
		}
	}

	/**
	 * 修改并必要时重建触发器/Job
	 * @param form 表单
	 */
	@Override
	public void update(SysJobUpdateForm form) {
		SysJobEntity sysJob = getById(Long.parseLong(form.getId()));
		if (sysJob == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		// 模式不可切换：自定义 Job 模式 invoke_target 必须保持为空；Bean 模式 invoke_target 不允许改为空
		boolean customMode = (sysJob.getInvokeTarget() == null || sysJob.getInvokeTarget().isBlank());
		if (customMode) {
			if (form.getInvokeTarget() != null && !form.getInvokeTarget().isBlank()) {
				throw new SystemBusinessException(JOB_MODE_CUSTOM_FORBIDS_INVOKE_TARGET);
			}
		}
		else {
			if (form.getInvokeTarget() != null && form.getInvokeTarget().isBlank()) {
				throw new SystemBusinessException(JOB_MODE_BEAN_REQUIRES_INVOKE_TARGET);
			}
			// Bean 模式允许修改 invoke_target
			if (form.getInvokeTarget() != null) {
				validateModeAndSecurity(sysJob.getJobClass(), form.getInvokeTarget());
				sysJob.setInvokeTarget(form.getInvokeTarget());
			}
		}
		sysJob.setCronExpression(form.getCronExpression());
		sysJob.setMisfirePolicy(form.getMisfirePolicy());
		sysJob.setConcurrent(form.getConcurrent());
		sysJob.setStartTime(form.getStartTime());
		sysJob.setEndTime(form.getEndTime());
		sysJob.setStatus(form.getStatus());
		sysJob.setJobParams(form.getJobParams());
		sysJob.setRemark(form.getRemark());
		updateById(sysJob);
		try {
			quartzAdmin.scheduleOrReplace(sysJob);
		}
		catch (SchedulerException ex) {
			log.error("更新 Quartz 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_UPDATE_FAILED, ex.getMessage());
		}
	}

	/**
	 * 删除
	 * @param id 主键
	 */
	@Override
	public void deleteById(Long id) {
		SysJobEntity db = getById(id);
		if (db == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		String jobName = db.getJobName();
		String jobGroup = db.getJobGroup();

		try {
			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
			quartzAdmin.deleteJobIfExists(jobKey);
		}
		catch (SchedulerException ex) {
			log.error("删除 Quartz Job 失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_DELETE_FAILED, ex.getMessage());
		}

		// 级联物理删除日志（日志可丢失，不强制与主表同事务强一致）
		try {
			jobLogMapper.deleteByJobNameAndGroup(jobName, jobGroup);
		}
		catch (Exception exception) {
			log.error("级联物理删除日志失败", exception);
		}
		removeById(id);
	}

	/**
	 * 批量更新状态
	 * @param form 表单
	 */
	@Override
	public void batchUpdateStatus(SysJobStatusForm form) {
		for (Long id : form.getIds()) {
			SysJobEntity db = getById(id);
			if (db == null) {
				continue;
			}
			db.setStatus(form.getStatus());
			updateById(db);
			try {
				if (form.getStatus()) {
					quartzAdmin.resumeJob(db.getJobName(), db.getJobGroup());
				}
				else {
					quartzAdmin.pauseJob(db.getJobName(), db.getJobGroup());
				}
			}
			catch (SchedulerException ex) {
				log.warn("批量更新任务状态同步 Quartz 失败 id={} : {}", id, ex.getMessage());
			}
		}
	}

	/**
	 * 立即执行一次
	 * @param id 主键
	 */
	@Override
	public void runOnce(Long id) {
		SysJobEntity db = getById(id);
		if (db == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		validateModeAndSecurity(db.getJobClass(), db.getInvokeTarget());
		try {
			// 立即执行不应改变任务的暂停/启用状态，也不应重建 Cron Trigger；仅确保 JobDetail 存在即可
			quartzAdmin.ensureJobDetail(db);
			quartzAdmin.triggerJob(db.getJobName(), db.getJobGroup());
		}
		catch (SchedulerException ex) {
			log.error("立即执行失败", ex);
			throw new SystemBusinessException(JOB_QUARTZ_RUN_ONCE_FAILED, ex.getMessage());
		}
	}

	/**
	 * 暂停分组所有任务
	 * @param groupCode 分组编码
	 */
	@Override
	public void pauseAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = list(
				Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		for (SysJobEntity job : jobs) {
			job.setStatus(false);
			updateById(job);
			try {
				quartzAdmin.pauseJob(job.getJobName(), job.getJobGroup());
			}
			catch (SchedulerException ex) {
				log.warn("分组暂停失败 {} : {}", job.getJobName(), ex.getMessage());
			}
		}
	}

	/**
	 * 恢复分组所有任务
	 * @param groupCode 分组编码
	 */
	@Override
	public void resumeAllInGroup(String groupCode) {
		List<SysJobEntity> jobs = list(
				Wrappers.lambdaQuery(SysJobEntity.class).eq(SysJobEntity::getJobGroup, groupCode));
		List<Long> ids = jobs.stream().map(SysJobEntity::getId).toList();
		if (!ids.isEmpty()) {
			lambdaUpdate().set(SysJobEntity::getStatus, true).in(SysJobEntity::getId, ids).update();
		}
		try {
			quartzAdmin.resumeGroup(groupCode);
		}
		catch (SchedulerException ex) {
			log.warn("分组恢复失败 groupCode={} : {}", groupCode, ex.getMessage());
		}
	}

	/**
	 * 验证分组是否启用
	 * @param groupCode 分组编码
	 */
	private void validateGroupEnabled(String groupCode) {
		// 获取任务分组
		LambdaQueryWrapper<SysJobGroupEntity> lambdaQueryWrapper = new LambdaQueryWrapper<SysJobGroupEntity>()
			.eq(SysJobGroupEntity::getGroupCode, groupCode);
		SysJobGroupEntity sysJobGroup = sysJobGroupMapper.selectOne(lambdaQueryWrapper);

		if (sysJobGroup == null || !BooleanUtil.isTrue(sysJobGroup.getStatus())) {
			throw new SystemBusinessException(JOB_GROUP_NOT_AVAILABLE);
		}
	}

	/**
	 * 验证任务类和执行目标
	 * @param jobClassName 任务类名
	 * @param invokeTarget 执行目标
	 */
	private void validateModeAndSecurity(String jobClassName, String invokeTarget) {
		if (jobClassName == null || jobClassName.isBlank()) {
			throw new SystemBusinessException(JOB_CLASS_EMPTY);
		}
		if (!quartzTaskRegistry.isAllowed(jobClassName)) {
			throw new SystemBusinessException(JOB_CLASS_NOT_WHITELISTED);
		}
		boolean customMode = (invokeTarget == null || invokeTarget.isBlank());
		if (!customMode) {
			String it = Objects.requireNonNull(invokeTarget, "invokeTarget");
			// Bean 调用模式：校验 invoke_target 能找到 Bean 且类型匹配所选 jobClass
			int dot = it.indexOf('.');
			if (dot <= 0) {
				throw new SystemBusinessException(JOB_INVOKE_TARGET_MALFORMED);
			}
			String beanName = it.substring(0, dot);
			String tail = it.substring(dot + 1).trim();
			if (tail.isBlank()) {
				throw new SystemBusinessException(JOB_INVOKE_TARGET_MALFORMED);
			}
			Object bean;
			try {
				bean = applicationContext.getBean(java.util.Objects.requireNonNull(beanName, "beanName"));
			}
			catch (Exception e) {
				log.error("未找到 Spring Bean: {}", beanName, e);
				throw new SystemBusinessException(JOB_SPRING_BEAN_NOT_FOUND, beanName);
			}
			try {
				String nonNullJobClassName = java.util.Objects.requireNonNull(jobClassName, "jobClassName");
				Class<?> expect = Class.forName(nonNullJobClassName);
				Class<?> targetClass = AopUtils.getTargetClass(bean);
				// 反射白名单：invoke_target 目标 Bean 必须在白名单内（类级 @QuartzTask）
				if (!quartzTaskRegistry.isAllowed(targetClass.getName())) {
					throw new SystemBusinessException(JOB_BEAN_NOT_WHITELISTED);
				}
				if (!expect.isAssignableFrom(targetClass)) {
					throw new SystemBusinessException(JOB_BEAN_TYPE_MISMATCH);
				}

				// 校验目标方法存在且签名符合当前约束（无参或单 String）
				int lp = tail.indexOf('(');
				String methodName;
				String argsInner = null;
				if (lp < 0) {
					methodName = tail.trim();
				}
				else {
					if (!tail.endsWith(")")) {
						throw new SystemBusinessException(JOB_INVOKE_TARGET_PAREN_MISMATCH);
					}
					methodName = tail.substring(0, lp).trim();
					argsInner = tail.substring(lp + 1, tail.lastIndexOf(')')).trim();
				}
				if (methodName.isBlank()) {
					throw new SystemBusinessException(JOB_METHOD_NAME_EMPTY);
				}
				int argCount = 0;
				if (argsInner != null) {
					if (argsInner.isBlank()) {
						argCount = 0;
					}
					else if ((argsInner.startsWith("'") && argsInner.endsWith("'"))
							|| (argsInner.startsWith("\"") && argsInner.endsWith("\""))) {
						argCount = 1;
					}
					else {
						throw new SystemBusinessException(JOB_INVOKE_PARAM_NOT_SUPPORTED);
					}
				}
				Method m = (argCount == 0) ? targetClass.getMethod(methodName)
						: targetClass.getMethod(methodName, String.class);
				if (m == null) {
					throw new SystemBusinessException(JOB_TARGET_METHOD_NOT_FOUND, methodName);
				}
			}
			catch (ClassNotFoundException e) {
				log.error("任务类无法加载", e);
				throw new SystemBusinessException(JOB_CLASS_LOAD_FAILED);
			}
			catch (NoSuchMethodException e) {
				log.error("未找到目标方法或参数不匹配", e);
				throw new SystemBusinessException(JOB_METHOD_OR_PARAM_MISMATCH);
			}
			return;
		}
		// 自定义 Job 模式：校验 jobClassName 可加载且实现 Quartz Job
		try {
			Class<?> clazz = Class.forName(java.util.Objects.requireNonNull(jobClassName, "jobClassName"));
			if (!org.quartz.Job.class.isAssignableFrom(clazz)) {
				throw new SystemBusinessException(JOB_CUSTOM_MUST_IMPLEMENT_QUARTZ_JOB);
			}
		}
		catch (ClassNotFoundException e) {
			log.error("任务类无法加载", e);
			throw new SystemBusinessException(JOB_CLASS_LOAD_FAILED);
		}
	}

	/**
	 * 填充触发时间
	 * @param row 行数据
	 */
	private void enrichFireTimes(SysJobPageResponse row) {
		try {
			// 检查任务是否存在
			JobKey jobKey = JobKey.jobKey(row.getJobName(), row.getJobGroup());
			if (!scheduler.checkExists(jobKey)) {
				return;
			}

			// 获取任务的触发器
			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
			if (triggers == null || triggers.isEmpty()) {
				return;
			}

			// 获取任务的下一个触发时间和上一个触发时间
			Trigger trigger = triggers.get(0);
			// 将触发时间转换为本地时间
			row.setNextFireTime(toLocal(trigger.getNextFireTime()));
			// 将上一个触发时间转换为本地时间
			row.setPreviousFireTime(toLocal(trigger.getPreviousFireTime()));
		}
		catch (SchedulerException exception) {
			log.error("填充触发时间失败: {}", exception.getMessage());
		}
	}

}
