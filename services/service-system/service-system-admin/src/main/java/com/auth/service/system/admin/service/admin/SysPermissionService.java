package com.auth.service.system.admin.service.admin;

import java.util.List;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统权限写服务
 *
 * @author Bunny
 */
public interface SysPermissionService extends IService<SysPermissionEntity> {

	/**
	 * 批量新增权限
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysPermissionForm> forms);

	/**
	 * 更新权限
	 * @param form 更新表单
	 */
	void update(SysPermissionForm form);

	/**
	 * 物理删除权限
	 * @param id 权限主键
	 */
	void deleteById(Long id);

}
