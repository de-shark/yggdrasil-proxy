
# Yggdrasil Proxy

能让你的服务器同时兼容 **Mojang正版登录** 和 **第三方皮肤站外置登录** 。


## 快速开始

### 通用第一步

克隆本仓库代码

```
git clone https://github.com/de-shark/yggdrasil-proxy.git
```

### Docker 运行



```
# 1. 获取配置
cp config.example.json config.json
# 按需修改config.json

# 2. 启动容器
docker compose up -d
```

### Minecraft服务器 连接 Yggdrasil Proxy

在启动参数中加入:
```
-javaagent:authlib-injector-1.x.x.jar=http://localhost:32217
# "http://localhost:32217" 修改为 Yggdrasil Proxy 地址
```

### config.json 示例

```
port: 32217  # 使用端口
authServers:
- priority: 0  # 优先级，数字越小优先级越高
  name: "Mojang"  # 认证服务器名字（自定义）
  url: "https://sessionserver.mojang.com"
  timeout: 5
- priority: 1
  name: "LittleSkin"
  # Yggdrasil认证链接，一般需要在url末尾加入“/sessionserver”
  url: "https://littleskin.cn/api/yggdrasil/sessionserver"
  timeout: 5
```