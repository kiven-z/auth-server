package com.auth.service.system.admin.service.me;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 当前登录用户个人资料维护
 *
 * @author Bunny
 */
public interface MeProfileService extends IService<UserEntity> {

	/**
	 * 更新当前登录用户可编辑的个人资料字段
	 * @param form 资料表单
	 */
	void updateMyProfile(MeProfileUpdateForm form);

	/**
	 * 更新当前登录用户头像
	 * @param form 头像
	 */
	void updateMyAvatar(MeAvatarUpdateForm form);

}
