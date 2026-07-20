package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信业务分类字典
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageCategoryMapper extends BaseMapper<InAppMessageCategoryEntity> {

	/**
	 * 启用中的大类
	 * @return 大类列表
	 */
	List<InAppMessageCategoryEntity> selectEnabledMajors();

	/**
	 * 按 code 查启用中的大类
	 * @param code 大类编码
	 * @return 不存在或非大类时为 null
	 */
	InAppMessageCategoryEntity selectEnabledMajorByCode(@Param("code") String code);

	/**
	 * 指定大类下启用中的小类
	 * @param parentId 大类主键
	 * @return 小类列表
	 */
	List<InAppMessageCategoryEntity> selectEnabledChildrenByParentId(@Param("parentId") Long parentId);

}
