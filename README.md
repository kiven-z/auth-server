# auth-server-pro

Auth 项目的后端服务集合，基于 Spring Boot 3.5 / Spring Cloud 2025 / Spring Cloud Alibaba 构建，提供鉴权、网关限流与系统业务能力。

> 详细设计、环境搭建、排障记录见姊妹仓库 [auth-docs](https://github.com/kiven-z/auth-docs)。

## 技术栈

| 类别      | 选型                                                                      |
|---------|-------------------------------------------------------------------------|
| 运行时     | JDK 17                                                                  |
| 框架      | Spring Boot 3.5.x、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0 |
| 构建      | Apache Maven 3.9.x（CI-friendly `${revision}`）                           |
| 持久层     | MyBatis-Plus 3.5、HikariCP、dynamic-datasource                            |
| 数据库     | MySQL 9.x                                                               |
| 缓存/会话   | Redis（验证码、限流）                                                           |
| 注册/配置中心 | Nacos                                                                   |
| 对象存储    | AWS SDK v2（S3 协议），本地用 MinIO                                             |
| 鉴权      | JJWT 0.12、自研 Security 二次封装                                              |
| 文档      | springdoc-openapi 2.8                                                   |
| 工具      | Hutool、MapStruct、EasyExcel、Quartz、ip2region、yauaa                       |

## 模块结构

```  
auth-server-pro  
├── commons/                 # 公共基础库  
│   ├── common-core  
│   ├── common-data  
│   ├── common-mapstruct  
│   └── common-starter-*     # cache / ip / jwt / restTemplate / web / webdoc  
├── modules/                 # 业务无关的可复用业务模块  
│   ├── module-file / module-file-api  
│   ├── module-message-api  
│   ├── module-platform-persistence  
│   └── module-security-*    # core / data-permission / web-starter / contract / autoconfigure  
├── services/                # 可独立部署的业务服务  
│   ├── service-auth         # 鉴权 / JWT 签发  
│   ├── service-system       # 系统业务（用户、部门、消息、文件、调度等）  
│   └── service-example      # 示例工程  
└── auth-gateway/            # 网关 / 限流  
```

`service-system` 内部按 `admin / authorization / bootstrap / common / file / message / schedule`

二级拆分，对应控制器、鉴权、启动、公共、文件、消息、调度域。

## 快速开始

环境前置与中间件清单见 [`auth-docs/开发环境搭建/1-环境依赖清单`](../auth-docs/开发环境搭建/1-环境依赖清单.md)。

本地起前后端的完整步骤见 [`auth-docs/开发环境搭建/8-本地启动前后端`](../auth-docs/开发环境搭建/8-本地启动前后端.md)。

```bash  
# 编译（默认 dev profile）  
./mvnw -T 1C clean install -DskipTests  
  
# 单服务运行（以 service-system 为例）  
cd services/service-system/service-system-bootstrap  
../../mvnw spring-boot:run  
  
# 切换 profile./mvnw -P test clean package  
./mvnw -P prod clean package  
```

服务端口（默认）：

| 服务             | 端口    |
|----------------|-------|
| auth-gateway   | 8080  |
| service-auth   | 20001 |
| service-system | 20002 |

## 容器化部署

`docker/` 下提供运行时 compose 与本地构建 override：

```bash  
cd docker./dockerctl.sh dev up        # 本地构建并起  
./dockerctl.sh prod up       # 服务器：先 login ACR，再 export IMAGE_PREFIX / IMAGE_TAG```  
  
变量与挂载说明见 `docker/compose.yml` 顶部注释。  
```

## 代码规范

- 遵循《阿里巴巴 Java 开发手册（泰山版）》与 SonarQube Quality Gate。
- 代码格式：`./spring-format.sh`（spring-javaformat + Spotless）。
- 提交信息规范见仓库内 `.cursor/rules/commit-message.mdc`。

## 赞助

如果这个项目对你有帮助，欢迎打赏支持。

<table>
  <tr>
    <td align="center">
      <img src="./donate/WeChatPay.jpg" width="220" alt="微信" /><br/>
      微信
    </td>
    <td align="center">
      <img src="./donate/AliPay.jpg" width="220" alt="支付宝" /><br/>
      支付宝
    </td>
  </tr>
</table>

## 许可证

Copyright 2024-2026 Bunny。

源码基于 [PolyForm Noncommercial License 1.0.0](./LICENSE) 发布，**商业使用须另行购买授权**

详见 [COMMERCIAL.md](./COMMERCIAL.md)。
