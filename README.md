
# Yggdrasil Proxy

一个能够兼容`Mojang正版登录`和`第三方外置皮肤站登录`的`验证代理服务器`。


## HOW TO USE

### 克隆本仓库代码

```
git clone https://github.com/de-shark/yggdrasil-proxy.git
```

### 打包可执行文件
在仓库目录输入命令：

windows
```
./gradlew.bat build
```

linux
```
./gradlew build
```

在`build/libs`目录可找到服务端`yggdrasil-proxy-1.0.0.jar`

### 启动代理服务器

输入命令启动服务器，并开放32217端口
```
java -jar yggdrasil-proxy-1.0.0.jar
```

### Minecraft服务器 连接 Yggdrasil Proxy

在启动参数中加入:
```
-javaagent:authlib-injector-1.x.x.jar=http://localhost:32217
```