package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门服务（查询能力）。
 *
 * @author Bunny
 */
public interface SysDeptService extends IService<SysDeptEntity> {

	/**
	 * 条件查询全部部门（扁平，供前端建树）。
	 * @param query 筛选条件
	 * @return 部门行
	 */
	List<SysDeptListVO> listFlat(SysDeptQuery query);

	/**
	 * 分页查询部门（表格视图）。
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptQuery query);

	/**
	 * 部门详情。
	 * @param id 部门主键
	 * @return 详情 VO
	 */
	SysDeptDetailVO getDetail(Long id);

}
