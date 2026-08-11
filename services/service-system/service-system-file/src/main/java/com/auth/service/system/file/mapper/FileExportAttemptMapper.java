package com.auth.service.system.file.mapper;

import com.auth.service.system.file.model.entity.FileExportAttemptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件异步导出执行尝试 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface FileExportAttemptMapper extends BaseMapper<FileExportAttemptEntity> {

}
