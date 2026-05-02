package com.auth.service.system.admin.menu.controller;

import com.auth.common.core.model.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/system/menu/web")
@RestController
public class MenuController {
    @GetMapping("web-routes")
    public Result<List<Object>> getWebRoutes() {

        return Result.success();
    }
}
