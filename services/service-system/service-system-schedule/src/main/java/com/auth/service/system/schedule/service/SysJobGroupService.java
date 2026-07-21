package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.JobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 任务分组维护
 *
 * @author Bunny
 */
public interface SysJobGroupService {

	/**
	 * 分页查询
	 * @param pageParams 分页参数
	 * @param query 查询参数
	 * @return 分页数据
	 */
	PageResponse<SysJobGroupPageVO> getPage(Page<JobGroupEntity> pageParams, SysJobGroupQuery query);

	/**
	 * 启用状态分组下拉/远程搜索（仅分组已启用 {@code status == true}）
	 * @param keyword 关键字（可空）
	 * @param limit 最大条数
	 * @return 列表
	 */
	List<SysJobGroupPageVO> listEnabledOptions(String keyword, int limit);

	/**
	 * 分组详情
	 * @param id 主键
	 * @return 详情
	 */
	SysJobGroupDetailVO getDetail(Long id);

	/**
	 * 新增分组
	 * @param form 表单
	 */
	void create(SysJobGroupForm form);

	/**
	 * 修改分组（不含 group_code）
	 * @param form 表单
	 */
	void update(SysJobGroupUpdateForm form);

	/**
	 * 删除分组（非内置、且无关联任务）
	 * @param id 主键
	 */
	void deleteById(Long id);

}
