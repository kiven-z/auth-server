package com.auth.service.system.schedule.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.service.SysJobDefinitionService;
import com.auth.service.system.schedule.service.SysJobOperationService;
import com.auth.service.system.schedule.service.SysJobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

/**
 * 定时任务与 Quartz 同步实现
 *
 * @author bunny
 */
@Service
public class SysJobServiceImpl implements SysJobService {

	private final SysJobDefinitionService definitionService;

	private final SysJobOperationService operationService;

	public SysJobServiceImpl(SysJobDefinitionService definitionService, SysJobOperationService operationService) {
		this.definitionService = definitionService;
		this.operationService = operationService;
	}

	@Override
	public void syncAllFromDatabase() {
		operationService.syncAllFromDatabase();
	}

	@Override
	public PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query) {
		return definitionService.getPage(pageParams, query);
	}

	@Override
	public SysJobDetailVO getDetail(Long id) {
		return definitionService.getDetail(id);
	}

	@Override
	public void create(SysJobCreateForm form) {
		definitionService.create(form);
	}

	@Override
	public void update(SysJobUpdateForm form) {
		definitionService.update(form);
	}

	@Override
	public void deleteById(Long id) {
		definitionService.deleteById(id);
	}

	@Override
	public void batchUpdateStatus(SysJobStatusForm form) {
		operationService.batchUpdateStatus(form);
	}

	@Override
	public void runOnce(Long id) {
		operationService.runOnce(id);
	}

	@Override
	public void pauseAllInGroup(String groupCode) {
		operationService.pauseAllInGroup(groupCode);
	}

	@Override
	public void resumeAllInGroup(String groupCode) {
		operationService.resumeAllInGroup(groupCode);
	}

}
