package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.admin.vo.SysUserPostProfileItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户档案聚合查询（部门、岗位）
 *
 * @author Bunny
 */
@Mapper
public interface SysUserProfileMapper {

    /**
     * 按用户ID查询部门关联列表
     *
     * @param userId 用户ID
     * @return 部门项
     */
    List<SysUserDeptProfileItemResponse> selectDeptProfileByUserId(@Param("userId") Long userId);

    /**
     * 按用户ID查询岗位关联列表
     *
     * @param userId 用户ID
     * @return 岗位项
     */
    List<SysUserPostProfileItemResponse> selectPostProfileByUserId(@Param("userId") Long userId);

}
