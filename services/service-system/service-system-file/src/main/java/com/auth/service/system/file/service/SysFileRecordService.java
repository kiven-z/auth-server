package com.auth.service.system.file.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文件记录查询与治理服务。
 *
 * @author Bunny
 */
public interface SysFileRecordService extends IService<SysFileRecordEntity> {

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
	 * 按主键逻辑删除文件记录
	 * @param id 文件记录主键
	 * @param ownerUserId 归属用户ID
	 */
	void deleteById(Long id, Long ownerUserId);

}
