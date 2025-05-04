# Yggdrasil Proxy

这是一个使用Spring Boot编写的Minecraft Yggdrasil验证代理服务器。该服务允许Minecraft服务器通过多个Yggdrasil验证服务进行身份验证，包括Mojang官方和第三方验证服务（如LittleSkin）。

## 项目功能

- 支持多个Yggdrasil验证服务器，按优先级查询
- 玩家验证信息缓存
- 自动垃圾回收机制
- 完整的日志记录系统
- 可配置的代理和超时设置

## 配置说明

配置文件位于`application.yml`：

```yaml
yggdrasil:
enable: true # 启用/禁用代理服务
log: true # 启用/禁用日志记录
ip: 0.0.0.0 # 监听的IP地址
servers: # 验证服务器列表
  - level: 0 # 服务器优先级（数字越小优先级越高）
    name: "Mojang" # 服务器名称
    url: "https://sessionserver.mojang.com" # 服务器URL
    timeout: 5 # 超时时间（秒）
  - level: 1
    name: "LittleSkin"
    url: "https://littleskin.cn/api/yggdrasil/sessionserver"
    timeout: 5
```
