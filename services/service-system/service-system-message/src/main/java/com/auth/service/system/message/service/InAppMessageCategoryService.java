package com.auth.service.system.message.service;

import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 站内信业务分类只读查询
 *
 * @author Bunny
 */
public interface InAppMessageCategoryService extends IService<InAppMessageCategoryEntity> {

	/**
	 * 启用中的大类列表
	 * @return 大类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listMajors();

	/**
	 * 指定大类下启用中的小类列表
	 * @param parentCode 大类 code
	 * @return 小类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listChildren(String parentCode);

}
