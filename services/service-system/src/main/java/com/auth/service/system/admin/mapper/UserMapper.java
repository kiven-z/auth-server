package com.auth.service.system.admin.mapper;

import com.auth.module.security.starter.model.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（认证专用）
 *
 * @author Bunny
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
