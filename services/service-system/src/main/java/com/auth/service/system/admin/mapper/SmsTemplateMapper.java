package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SmsTemplateEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 短信模板 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SmsTemplateMapper extends BaseMapper<SmsTemplateEntity> {

	/**
	 * 按业务模板编码查询已启用的最高优先级模板
	 * @param templateCode 业务模板编码
	 * @return 短信模板
	 */
	SmsTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

}
