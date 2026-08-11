package com.auth.module.file.export.datasource;

/**
 * 远程分页数据源（通过 HTTP 回调）
 *
 * @author Bunny
 */
public abstract class RemotePageDataSource implements PageDataSource {

	protected final String fetchService;

	protected final String fetchPath;

	protected final String bizType;

	protected final String querySnapshotJson;

	protected final String columnSnapshotJson;

	protected final String scopeSnapshotJson;

	/**
	 * @param fetchService 取数服务名
	 * @param fetchPath 取数路径
	 * @param bizType 业务类型
	 * @param querySnapshotJson 查询条件快照
	 * @param columnSnapshotJson 列快照
	 * @param scopeSnapshotJson 权限快照
	 */
	protected RemotePageDataSource(String fetchService, String fetchPath, String bizType, String querySnapshotJson,
			String columnSnapshotJson, String scopeSnapshotJson) {
		this.fetchService = fetchService;
		this.fetchPath = fetchPath;
		this.bizType = bizType;
		this.querySnapshotJson = querySnapshotJson;
		this.columnSnapshotJson = columnSnapshotJson;
		this.scopeSnapshotJson = scopeSnapshotJson;
	}

}
