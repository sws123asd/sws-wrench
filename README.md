# SWS Wrench

一套实用的Java开发工具集，旨在提高开发效率和代码质量。

## 项目介绍

SWS Wrench是一个工具集合，包含多个独立的子项目，每个子项目都专注于解决特定的问题。目前包含以下子项目：

- **fast-dcc-ratelimiter**: 基于ZooKeeper和Redis的动态配置中心和分布式限流组件

## 子项目列表

### Fast DCC RateLimiter

基于ZooKeeper和Redis的动态配置中心和分布式限流组件。

**主要功能**：
- 基于ZooKeeper的动态配置中心，支持实时更新配置
- 基于Redis的分布式限流功能，支持QPS控制
- 支持黑名单机制，防止恶意请求
- 支持降级方法，优雅处理限流情况

[查看详细文档](./fast-dcc-ratelimiter/README.md)

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/sws-wrench.git
cd sws-wrench
```

### 2. 构建项目

```bash
mvn clean install
```

### 3. 使用子项目

在你的项目中添加相应的依赖：

```xml
<dependency>
    <groupId>org.sws.wrench</groupId>
    <artifactId>fast-dcc-ratelimiter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 贡献指南

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 许可证

MIT
