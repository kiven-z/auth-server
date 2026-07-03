package com.auth.service.system.file.convert;

import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.value.StoredFile;
import lombok.experimental.UtilityClass;

/**
 * 文件上传记录组装工具
 *
 * @author Bunny
 */
@UtilityClass
public class FileUploadRecordAssembler {

	/**
	 * 组装文件上传记录实体
	 * @param storedFile 存储结果
	 * @param form 上传表单
	 * @return 文件记录实体
	 */
	public static SysFileRecordEntity toEntity(StoredFile storedFile, FileUploadForm form) {
		SysFileRecordEntity entity = new SysFileRecordEntity();
		entity.setStoragePlatform(storedFile.getStoragePlatform());
		entity.setBucket(storedFile.getBucket());
		entity.setObjectKey(storedFile.getObjectKey());
		entity.setUrl(storedFile.getUrl());
		entity.setOriginalName(storedFile.getOriginalName());
		entity.setExtension(storedFile.getExtension());
		entity.setContentType(storedFile.getContentType());
		entity.setSize(storedFile.getSize());
		entity.setEtag(storedFile.getEtag());

		entity.setBizType(form.getBizType());
		entity.setBizId(form.getBizId());
		entity.setRemark(form.getRemark());

		entity.setUploadMode("SIMPLE");
		return entity;
	}

}