package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 菜单转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysMenuConverter {

	SysMenuConverter INSTANCE = Mappers.getMapper(SysMenuConverter.class);

	/**
	 * 详情 VO：继承列表 VO 的映射 + 额外字段 parentId、status 等继承自 toListVo 的映射规则
	 * 注意：如果字段名相同且无特殊转换，MapStruct 会自动映射
	 * @param entity 菜单实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysMenuDetailVO toDetailVo(SysMenuEntity entity);

	/**
	 * 列表行 VO
	 * @param entity 菜单实体
	 * @return 列表 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysMenuListVO toListVo(SysMenuEntity entity);

	/**
	 * 列表行 VO 批量
	 * @param entities 菜单实体列表
	 * @return 列表 VO
	 */
	List<SysMenuListVO> toListVoList(List<SysMenuEntity> entities);

	/**
	 * 映射基本字段
	 * @param form 菜单保存表单
	 * @return 菜单实体
	 */
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "extraMeta", ignore = true)
	@Mapping(target = "isDeleted", ignore = true)
	SysMenuEntity toMenuEntity(SysMenuSaveForm form);

}