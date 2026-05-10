package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {

	/**
	 * 条件查询全部菜单（扁平，供前端建树）
	 * @param query 查询条件
	 * @return 菜单列表
	 */
	List<SysMenuListVO> selectListByQuery(@Param("query") SysMenuQuery query);

	/**
	 * 分页查询菜单
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页菜单列表
	 */
	IPage<SysMenuListVO> selectPageByQuery(@Param("page") IPage<SysMenuEntity> page,
			@Param("query") SysMenuQuery query);

}
