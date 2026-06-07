package com.auth.service.auth.authorization.infrastructure.persistence;

import com.auth.service.auth.authorization.domain.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.authorization.domain.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.authorization.domain.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.authorization.domain.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 授权失效幂等事件 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationEventMapper {

    /**
     * 按事件 ID 查询已处理记录
     *
     * @param eventId 业务事件 ID
     * @return 投影，不存在时为 null
     */
    AuthorizationInvalidationEventProjection selectByEventId(@Param("eventId") String eventId);

    /**
     * 插入处理中占位记录
     *
     * @param projection 事件投影
     * @return 插入行数
     */
    int insertProcessingClaim(AuthorizationInvalidationEventProjection projection);

    /**
     * 将占位行更新为最终处理结果
     *
     * @param projection 事件投影
     * @return 更新行数
     */
    int updateProcessedOutcome(AuthorizationInvalidationEventProjection projection);

    /**
     * 删除处理中占位行
     *
     * @param eventId 业务事件 ID
     * @return 删除行数
     */
    int deleteProcessingClaim(@Param("eventId") String eventId);

    /**
     * 分页查询幂等事件
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<AuthorizationInvalidationEventPageRowPO> selectListByPage(
            @Param("page") Page<AuthorizationInvalidationEventPageRowPO> page,
            @Param("query") AuthorizationInvalidationEventQuery query);

    /**
     * 按主键查询详情
     *
     * @param id 主键
     * @return 详情投影
     */
    AuthorizationInvalidationEventDetailRowPO selectDetailById(@Param("id") Long id);

    /**
     * 统计幂等事件处理状态分布
     *
     * @return 统计结果
     */
    AuthorizationInvalidationEventStatsPO selectEventStats();

    /**
     * 批量删除超时的 processing 占位行
     *
     * @param cutoffTime 占位 update_time 早于此时间视为超时
     * @param batchSize  本批删除上限
     * @return 实际删除行数
     */
    int deleteStaleProcessingClaims(@Param("cutoffTime") LocalDateTime cutoffTime,
                                    @Param("batchSize") int batchSize);

    /**
     * 批量删除已完成的过期幂等事件
     *
     * @param cutoffTime processed_at 早于此时间的已完成行可被删除
     * @param batchSize  本批删除上限
     * @return 实际删除行数
     */
    int deleteCompletedBefore(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("batchSize") int batchSize);

}
