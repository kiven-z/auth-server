package com.auth.service.system.admin.service;

import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.query.SysDeptListQuery;
import com.auth.service.system.admin.model.query.SysDeptPageQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 部门服务（查询能力）
 *
 * @author Bunny
 */
public interface SysDeptService extends IService<SysDeptEntity> {

	/**
	 * 条件查询全部部门（扁平，供前端建树）
	 * @param query 筛选条件
	 * @return 部门行
	 */
	List<SysDeptListVO> listFlat(SysDeptListQuery query);

	/**
	 * 分页查询部门（表格视图）
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	PageResponse<SysDeptListVO> pageFlat(Page<SysDeptEntity> pageParams, SysDeptPageQuery query);

	/**
	 * 部门详情
	 * @param id 部门主键
	 * @return 详情 VO
	 */
	SysDeptDetailVO getDetail(Long id);

	/**
	 * 新增部门并写入闭包表
	 * @param form 新增表单
	 */
	void create(SysDeptForm form);

	/**
	 * 更新部门元数据
	 * @param form 保存表单
	 */
	void updateMeta(SysDeptForm form);

	/**
	 * 变更父部门并重算闭包
	 * @param deptId 部门 ID
	 * @param form 移动表单
	 */
	void move(Long deptId, SysDeptMoveForm form);

	/**
	 * 逻辑删除部门
	 * @param deptId 部门 ID
	 */
	void deleteById(Long deptId);

}
