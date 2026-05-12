package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门实体与列表 VO 转换。
 *
 * @author Bunny
 */
@Mapper
public interface SysDeptConverter {

	SysDeptConverter INSTANCE = Mappers.getMapper(SysDeptConverter.class);

	/**
	 * 列表行 VO
	 * @param entity 部门实体
	 * @return 列表 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysDeptListVO toListVo(SysDeptEntity entity);

	/**
	 * 列表行 VO 批量
	 * @param entities 部门实体列表
	 * @return 列表 VO
	 */
	List<SysDeptListVO> toListVoList(List<SysDeptEntity> entities);

}
