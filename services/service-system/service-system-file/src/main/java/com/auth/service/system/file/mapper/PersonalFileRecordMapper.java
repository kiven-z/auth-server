package com.auth.service.system.file.mapper;

import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人文件记录 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface PersonalFileRecordMapper extends BaseMapper<FileRecordEntity> {

}
