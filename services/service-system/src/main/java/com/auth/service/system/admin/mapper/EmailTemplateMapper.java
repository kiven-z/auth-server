package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.EmailTemplateEntity;
import com.auth.service.system.admin.model.po.emailtemplate.EmailTemplatePageRowPO;
import com.auth.service.system.admin.model.query.EmailTemplateQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 邮件模板表 Mapper 接口
 *
 * @author AuthoritySystem
 */
@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplateEntity> {

	/**
	 * 根据模板编号查询
	 * @param templateCode 模板编号
	 * @return 邮件模板
	 */
	EmailTemplateEntity selectTemplateByTemplateCode(@Param("templateCode") String templateCode);

	/**
	 * 分页查询邮件模板
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 邮件模板分页结果
	 */
	IPage<EmailTemplatePageRowPO> selectListByPage(@Param("page") Page<EmailTemplateEntity> pageParams,
			@Param("query") EmailTemplateQuery query);

}
