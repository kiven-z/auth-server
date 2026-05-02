package com.auth.service.system.admin.user.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.user.feign.AuthenticationInnerFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Bunny
 */
@Slf4j
@Component
public class AuthenticationInnerFeignClientFallback implements AuthenticationInnerFeignClient {

    /**
     * 刷新用户缓存
     *
     * @param userIds 用户ID列表
     * @return 刷新结果
     */
    @Override
    public Result<String> refreshUserCache(List<Long> userIds) {
        log.error("AuthenticationInnerFeignClientFallback-刷新用户缓存 失败，用户IDs：{}", userIds);

        // 方案A：返回失败结果，让调用方处理
        return Result.error("刷新缓存服务暂不可用");
    }

}
