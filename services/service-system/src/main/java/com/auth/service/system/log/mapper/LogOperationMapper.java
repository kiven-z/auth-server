package com.auth.service.system.log.mapper;

import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogOperationMapper extends BaseMapper<LogOperationEntity> {

}
