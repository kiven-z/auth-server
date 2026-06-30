package com.auth.service.system.admin.service.admin;

import java.util.List;

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
	 * 批量新增岗位
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysPostForm> forms);

	/**
	 * 更新岗位
	 * @param form 表单（须含主键；字段与新增一致）
	 */
	void update(SysPostForm form);

	/**
	 * 按主键逻辑删除岗位；user_post 或 grant_table 仍有引用时拒绝删除，成功后触发授权失效。
	 * @param id 岗位主键
	 */
	void deleteById(Long id);

}
