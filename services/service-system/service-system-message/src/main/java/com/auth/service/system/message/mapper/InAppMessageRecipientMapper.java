package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信收件箱 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageRecipientMapper extends BaseMapper<InAppMessageRecipientEntity> {

	/**
	 * 批量 INSERT IGNORE，撞唯一键时跳过
	 * @param rows 待插入行
	 * @return 实际插入行数
	 */
	int insertIgnoreBatch(@Param("rows") List<InAppMessageRecipientEntity> rows);

}
