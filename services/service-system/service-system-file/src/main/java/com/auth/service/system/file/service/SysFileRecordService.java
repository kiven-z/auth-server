package com.auth.service.system.file.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

/**
 * 文件记录查询与治理服务。
 *
 * @author Bunny
 */
public interface SysFileRecordService extends IService<SysFileRecordEntity> {

	/**
	 * 批量下载 ZIP 文件名
	 */
	String BATCH_DOWNLOAD_FILE_NAME = "file-records.zip";

	/**
	 * 批量下载 ZIP MIME 类型
	 */
	String BATCH_DOWNLOAD_CONTENT_TYPE = "application/zip";

	/**
	 * 分页查询当前可见文件记录
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query);

	/**
	 * 查询文件详情
	 * @param id 文件记录主键
	 * @param ownerUserId 归属用户ID
	 * @return 文件详情
	 */
	FileRecordDetailVO getDetail(Long id, Long ownerUserId);

	/**
	 * 按主键集合删除文件记录：公开文件物理删除，私有文件逻辑删除
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户ID，非空表示用户自行删除
	 * @param operatorUserId 操作用户ID
	 */
	void deleteByIds(List<Long> ids, Long ownerUserId, Long operatorUserId);

	/**
	 * 批量下载文件记录并输出 ZIP 流
	 * @param ids 文件记录主键集合
	 * @param ownerUserId 归属用户ID
	 * @return ZIP 流式响应体
	 */
	StreamingResponseBody batchDownload(List<Long> ids, Long ownerUserId);

	/**
	 * 批量切换文件隐私属性（公开 ↔ 私有），并迁移存储对象前缀
	 * @param ids 文件记录主键集合
	 * @param targetPrivate 目标是否私有
	 * @param ownerUserId 归属用户ID，非空表示用户自行操作
	 * @param operatorUserId 操作用户ID
	 */
	void updatePrivacyByIds(List<Long> ids, boolean targetPrivate, Long ownerUserId, Long operatorUserId);

}
