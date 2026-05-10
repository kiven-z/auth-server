package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

    /**
     * 分页查询角色（含创建人、更新人用户名展示列）
     *
     * @param page       分页参数
     * @param query      筛选条件
     * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
     * @return 分页结果
     */
    IPage<SysRolePageResponse> selectListByPage(@Param("page") Page<SysRoleEntity> page,
                                                @Param("query") SysRoleQuery query, @Param("orderBySql") String orderBySql);

    /**
     * 查询角色下拉选项
     *
     * @param roleName 角色名称（可选模糊）
     * @param roleCode 角色编码（可选模糊）
     * @return 角色选项列表
     */
    List<SysRoleOptionVO> selectRoleOptions(@Param("roleName") String roleName, @Param("roleCode") String roleCode);

}
