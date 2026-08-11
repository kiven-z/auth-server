package com.auth.service.system.file.export.datasource;

import com.auth.module.file.export.datasource.RemotePageDataSource;
import com.auth.module.file.export.definition.ExportColumns;
import com.auth.module.file.export.definition.ExportSheetMeta;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 文件服务远程分页数据源实现
 *
 * @author Bunny
 */
public class FileRemotePageDataSource extends RemotePageDataSource {

	private final RemotePageFetcher remotePageFetcher;

	/**
	 * @param remotePageFetcher 远程取数器
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @param bizType 业务类型
	 * @param querySnapshotJson 查询条件快照
	 * @param columnSnapshotJson 列快照
	 * @param scopeSnapshotJson 权限快照
	 */
	public FileRemotePageDataSource(RemotePageFetcher remotePageFetcher, String fetchService, String fetchPath,
			String bizType, String querySnapshotJson, String columnSnapshotJson, String scopeSnapshotJson) {
		super(fetchService, fetchPath, bizType, querySnapshotJson, columnSnapshotJson, scopeSnapshotJson);
		this.remotePageFetcher = remotePageFetcher;
	}

	@Override
	public IPage<Map<String, Object>> fetchPage(int pageNum, int pageSize) {
		return remotePageFetcher.fetchPage(fetchService, fetchPath, bizType, querySnapshotJson, scopeSnapshotJson,
				pageNum, pageSize);
	}

	@Override
	public ExportSheetMeta sheetMeta() {
		String sheetName = "导出数据";
		String filePrefix = bizType.trim().toLowerCase() + "_export";
		return ExportSheetMeta.builder()
			.sheetName(sheetName)
			.taskFilePrefix(filePrefix)
			.columns(ExportColumns.fromSnapshotJson(columnSnapshotJson))
			.writeHandlers(List.of(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH)))
			.build();
	}

}
