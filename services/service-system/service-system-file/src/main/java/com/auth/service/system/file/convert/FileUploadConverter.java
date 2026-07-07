package com.auth.service.system.file.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.vo.FileUploadResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文件上传映射转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface FileUploadConverter {

	FileUploadConverter INSTANCE = Mappers.getMapper(FileUploadConverter.class);

	/**
	 * 文件实体映射为上传响应
	 * @param entity 文件实体
	 * @return 上传响应
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	FileUploadResponseVO toUploadResponse(FileRecordEntity entity);

}
