package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统岗位写服务。
 *
 * @author Bunny
 */
public interface SysPostService extends IService<SysPostEntity> {

	/**
	 * 新增岗位
	 * @param form 新增表单
	 */
	void create(SysPostForm form);

	/**
	 * 更新岗位
	 * @param id 岗位主键
	 * @param form 表单（与新增字段一致；所属部门不可变更）
	 */
	void update(Long id, SysPostForm form);

	/**
	 * 按主键逻辑删除岗位；user_post 或 grant_table 仍有引用时拒绝删除，成功后触发授权失效。
	 * @param id 岗位主键
	 */
	void deleteById(Long id);

}
