package com.auth.common.core.model.query;

import java.io.Serializable;

import lombok.Data;

/**
 * 排序规则
 * 
 * @author Bunny
 */
@Data
public class SortSpec implements Serializable {
    /**
     * 业务字段 key，如 createTime
     */
    private String field;
    
    /**
     * 排序方向
     */
    private SortDirection direction = SortDirection.DESC;
}