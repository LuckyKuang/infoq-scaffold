# infoq-scaffold

- - -

## 平台简介

[![GitHub](https://img.shields.io/github/stars/luckykuang/infoq-scaffold.svg?style=social&label=Stars)](https://github.com/luckykuang/infoq-scaffold)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/luckykuang/infoq-scaffold/LICENSE)
[![使用IntelliJ IDEA开发维护](https://img.shields.io/badge/IntelliJ%20IDEA-提供支持-blue.svg)](https://www.jetbrains.com/?from=infoq-scaffold)
<br>
[![infoq-scaffold]( https://img.shields.io/badge/infoq%20scaffold-1.0.3-blue.svg)](https://github.com/luckykuang/infoq-scaffold)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-blue.svg)]()
[![JDK-17](https://img.shields.io/badge/JDK-17-green.svg)]()

- - -

> `infoq-scaffold` 是基于 [RuoYi-Vue-Plus](https://github.com/dromara/RuoYi-Vue-Plus) 进行魔改，比原版更纯净，不夹带任何广告

> 前端项目地址: [infoq-scaffold-ui](https://github.com/luckykuang/infoq-scaffold-ui)

## 项目结构

```text
|-infoq-scaffold
|-|--infoq-core
|-|--|--infoq-core-bom              统一依赖版本
|-|--|--infoq-core-common           公共模块
|-|--|--infoq-core-data             公共模块
|-|--infoq-plugin
|-|--|--infoq-plugin-bom
|-|--|--infoq-plugin-encrypt        数据加解密模块
|-|--|--infoq-plugin-excel          Excel模块
|-|--|--infoq-plugin-jackson        序列化模块
|-|--|--infoq-plugin-log            日志记录
|-|--|--infoq-plugin-mail           邮件模块
|-|--|--infoq-plugin-mybatis        数据库服务
|-|--|--infoq-plugin-oss            oss服务
|-|--|--infoq-plugin-redis          缓存服务
|-|--|--infoq-plugin-satoken        satoken安全模块
|-|--|--infoq-plugin-security       security安全模块
|-|--|--infoq-plugin-sensitive      脱敏模块
|-|--|--infoq-plugin-see            see模块
|-|--|--infoq-plugin-tenant         租户模块
|-|--|--infoq-plugin-translation    通用翻译功能
|-|--|--infoq-plugin-web            web服务
|-|--|--infoq-plugin-websocket      websocket模块
|-|--infoq-modules
|-|--|--infoq-system                系统服务
|-|--|-- ...                        后续直接这里拓展服务即可
```

