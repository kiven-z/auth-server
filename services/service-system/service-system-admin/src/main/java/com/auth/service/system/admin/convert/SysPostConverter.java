package com.auth.service.system.admin.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostBoundDeptPO;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.vo.post.SysPostBoundDeptItemVO;
import com.auth.service.system.admin.model.vo.post.SysPostBoundUserItemVO;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 岗位转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface SysPostConverter {

	SysPostConverter INSTANCE = Mappers.getMapper(SysPostConverter.class);

	/**
	 * 新增表单 → 实体
	 * @param form 表单
	 * @return 实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "isDeleted", ignore = true)
	SysPostEntity toEntity(SysPostForm form);

	/**
	 * 表单字段合并到已有实体（更新；不覆盖主键与审计字段）
	 * @param form 表单
	 * @param entity 已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "isDeleted", ignore = true)
	void applyUpdateForm(SysPostForm form, @org.mapstruct.MappingTarget SysPostEntity entity);

	/**
	 * 分页行 PO → 分页 VO
	 * @param po 持久层投影
	 * @return 分页行
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	SysPostPageResponse toPageResponse(SysPostPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 岗位实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	@Mapping(target = "boundDept", ignore = true)
	@Mapping(target = "boundUsers", ignore = true)
	@Mapping(target = "boundRoles", ignore = true)
	SysPostDetailVO toDetailVo(SysPostEntity entity);

	/**
	 * 所属部门 PO → 详情项 VO
	 * @param po 持久层投影
	 * @return 部门项，po 为 null 时返回 null
	 */
	SysPostBoundDeptItemVO toBoundDeptItem(SysPostBoundDeptPO po);

	/**
	 * 关联用户 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysPostBoundUserItemVO> toBoundUserItemList(List<SysPostBoundUserPO> poList);

}
