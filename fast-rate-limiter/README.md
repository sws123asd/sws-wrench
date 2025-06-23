# Fast DCC RateLimiter

基于ZooKeeper和Redis的动态配置中心和分布式限流组件。

## 功能特性

- 基于ZooKeeper的动态配置中心，支持实时更新配置
- 基于Redis的分布式限流功能，支持QPS控制
- 支持黑名单机制，防止恶意请求
- 支持降级方法，优雅处理限流情况
- 提供REST API和Java API两种方式更新配置

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.sws.wrench</groupId>
    <artifactId>fast-dcc-ratelimiter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置ZooKeeper和Redis

在`application.yml`中添加以下配置：

```yaml
sws:
  wrench:
    zookeeper:
      connect-string: localhost:2181
      session-timeout-ms: 60000
      connection-timeout-ms: 15000
      namespace: sws-dcc
      retry:
        base-sleep-time-ms: 1000
        max-retries: 3
        max-sleep-time-ms: 5000
    redis:
      host: localhost
      port: 6379
      password: # 如果有密码，请设置
      database: 0
      connect-timeout: 10000
      timeout: 3000
      pool:
        min-idle: 8
        max-idle: 50
        max-active: 200
        max-wait: 3000
```

### 3. 使用动态配置

使用`@DCCValue`注解标记需要动态配置的字段：

```java
@Component
public class ConfigService {

    @DCCValue("myConfig:defaultValue")
    private String myConfig;

    public String getMyConfig() {
        return myConfig;
    }
}
```

### 4. 使用限流功能

使用`@RateLimiterAccessInterceptor`注解标记需要限流的方法：

```java
@Service
public class UserService {

    @RateLimiterAccessInterceptor(key = "getUserInfo", permitsPerSecond = 10, blacklistCount = 100, fallbackMethod = "getUserInfoFallback")
    public Response<UserInfo> getUserInfo(Long userId) {
        // 业务逻辑
        return Response.success(userInfo);
    }

    public Response<UserInfo> getUserInfoFallback(Long userId) {
        // 降级逻辑
        return Response.degrade();
    }
}
```

### 5. 更新配置

#### 通过REST API更新

```
POST /api/dcc/update?key=myConfig&value=newValue
```

#### 通过Java API更新

```java
@Autowired
private IDCCService dccService;

public void updateConfig() {
    dccService.updateConfig("myConfig", "newValue");
}
```

## 高级用法

### 使用SpEL表达式

限流注解支持SpEL表达式，可以根据方法参数动态生成限流key：

```java
@RateLimiterAccessInterceptor(key = "getUserInfo:#userId", permitsPerSecond = 10)
public Response<UserInfo> getUserInfo(Long userId) {
    // 业务逻辑
}
```

### 自定义黑名单过期时间

可以通过动态配置修改黑名单过期时间：

```
POST /api/dcc/update?key=blacklistExpireSeconds&value=7200
```

### 关闭限流功能

可以通过动态配置关闭限流功能：

```
POST /api/dcc/update?key=rateLimiterSwitch&value=close
```

## 注意事项

- ZooKeeper和Redis必须可用，否则功能将无法正常工作
- 限流功能依赖于Redis的RateLimiter功能，请确保Redis版本支持
- 动态配置的更新是实时的，但可能存在短暂的延迟

## 许可证

MIT
