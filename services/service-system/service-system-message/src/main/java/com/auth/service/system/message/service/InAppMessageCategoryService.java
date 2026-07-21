package com.auth.service.system.message.service;

import com.auth.common.core.model.query.EnableStatusScope;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryStatusForm;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 站内信业务分类
 *
 * @author Bunny
 */
public interface InAppMessageCategoryService extends IService<InAppMessageCategoryEntity> {

	/**
	 * 管理端扁平全量列表
	 * @param query 筛选条件
	 * @return 扁平列表（含审计用户名）
	 */
	List<InAppMessageCategoryPageVO> listCategories(InAppMessageCategoryQuery query);

	/**
	 * 分类详情
	 * @param id 主键
	 * @return 详情（含父级展示字段与审计用户名）
	 */
	InAppMessageCategoryDetailVO getCategoryById(Long id);

	/**
	 * 新增分类
	 * @param form 新增表单
	 */
	void create(InAppMessageCategoryForm form);

	/**
	 * 更新分类
	 * @param form 更新表单
	 */
	void update(InAppMessageCategoryForm form);

	/**
	 * 批量启用/禁用分类
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(InAppMessageCategoryStatusForm form);

	/**
	 * 批量物理删除分类
	 * @param ids 主键列表
	 */
	void batchDelete(List<Long> ids);

	/**
	 * 大类列表
	 * @param statusScope 启停范围
	 * @return 大类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listMajors(EnableStatusScope statusScope);

	/**
	 * 指定大类下小类列表
	 * @param parentId 大类主键
	 * @param statusScope 启停范围
	 * @return 小类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listChildren(Long parentId, EnableStatusScope statusScope);

}
