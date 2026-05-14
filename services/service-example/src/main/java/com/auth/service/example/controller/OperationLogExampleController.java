package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import com.auth.module.security.contract.context.OperationLogContext;
import com.auth.service.example.audit.ExampleAuditBizModule;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志示例控制器
 * 用于演示操作日志的记录和查询
 *
 * @author Bunny
 */
@RequestMapping("/operation-log-example")
@RestController
public class OperationLogExampleController {

    @PublicApi
    @OperationLog(serviceDomain = AuditServiceDomain.EXAMPLE, bizModule = ExampleAuditBizModule.EXAMPLE_DEMO,
            operation = OperationLogKind.QUERY)
    @Operation(summary = "操作日志示例，设置目标ID")
    @DeleteMapping("/set-target-id/{id}")
    public Result<String> example(@PathVariable Long id) {
        OperationLogContext.setTargetId(id);
        return Result.success(null, "操作日志示例");
    }
}
