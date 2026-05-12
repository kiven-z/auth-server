package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 任务日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface SysJobLogConvert {

    SysJobLogConvert INSTANCE = Mappers.getMapper(SysJobLogConvert.class);

    /**
     * 任务日志分页行 PO → 分页返回 VO
     *
     * @param po 持久层查询投影
     * @return 分页返回对象
     */
    LogJobPageResponse toPageResponse(SysJobLogPageRowPO po);

}
