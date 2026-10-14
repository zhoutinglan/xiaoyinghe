<div align="center">

# 小影盒

**一个开源的 Android 影视播放器**

[![Build and Release](https://github.com/zhoutinglan/xiaoyinghe/actions/workflows/build.yml/badge.svg)](https://github.com/zhoutinglan/xiaoyinghe/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/zhoutinglan/xiaoyinghe)](https://github.com/zhoutinglan/xiaoyinghe/releases)
[![License](https://img.shields.io/badge/license-AGPL--3.0-blue)](LICENSE)

</div>

| 项目 | 值 |
| --- | --- |
| 应用名 | 小影盒 |
| 包名 | `com.xiaoyinghe.tv` |
| 当前版本 | 1.0.0 |
| 系统要求 | Android 7.0 及以上（API 24+），arm64-v8a |
| 开源协议 | AGPL-3.0 |

> 本项目基于 [TVboxOSC](https://github.com/kukuqi666/TVboxOSC) 源码二次开发。与原版使用**不同的包名**，两者可以同时安装、互不影响。

## 📲 下载

- 小影盒：[下载 v1.0.0](https://github.com/zhoutinglan/xiaoyinghe/releases) · [所有版本](https://github.com/zhoutinglan/xiaoyinghe/releases)

## ✨ 功能说明

- **不内置任何订阅源**：首次启动不会自动写入任何第三方源，全部由用户自行导入（「我的 → 点播源 / 直播源」支持链接导入与本地文件导入）。
- 点播、直播、本地视频三大板块，底部导航直接切换。
- EPG 节目单、弹幕、字幕、倍速播放、投屏、远程控制。
- 历史记录与收藏在首页，设置集中在「我的」。
- 应用内检查更新：读取本仓库的 `update.json`，支持 Wi-Fi 后台下载，下载完成后由用户确认安装。

## 🚀 本地构建

**环境要求**：JDK 17 或更高、Android SDK（`platforms;android-34`、`build-tools;34.0.0`）。

```bash
# 1. 指定 Android SDK 路径
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 2. 构建 Debug 并运行单元测试
./gradlew :app:assembleDebug :app:testDebugUnitTest

# 3. 安装到已连接的设备
./gradlew :app:installDebug
```

### 项目地址配置

应用内的「检查更新」和「关于页 - 项目地址」由 `gradle.properties` 统一控制，改这两行即可：

```properties
project.owner=zhoutinglan
project.repo=xiaoyinghe
```

### 发布签名

Release 构建需要签名密钥。在仓库根目录创建 `keystore.properties`（已在 `.gitignore` 中，**请勿提交**）：

```properties
storeFile=xiaoyinghe.jks
keyAlias=xiaoyinghe
keyPassword=<你的密码>
storePassword=<你的密码>
```

CI 环境则通过仓库 Secrets 提供：`XYH_KEYSTORE_BASE64`、`XYH_KEY_ALIAS`、`XYH_KEY_PASSWORD`、`XYH_STORE_PASSWORD`。

## 🔄 自动发布

流水线定义在 [`.github/workflows/build.yml`](.github/workflows/build.yml)：

| 触发条件 | 自动行为 |
| --- | --- |
| PR / 普通提交到 main | 构建 Debug、运行单元测试，产物保留 14 天 |
| main 上 `versionCode` 有变化 | 构建签名 Release，创建 Tag 并发布 APK，同步 `update.json` 与本文档 |
| 手动推送 `v*` Tag | 校验 Tag 与 `versionName` 一致后发布 |

发新版本只需要：改 `app/build.gradle` 里的 `versionCode` 和 `versionName` → 合并到 main → 等待自动发布完成。

## 📄 开源协议

本项目基于 TVBox 开源项目二次开发，遵循 **GNU AGPL-3.0** 协议，详见 [LICENSE](LICENSE)。

- 二次开发、修改、自用都可以；
- 但**任何形式的分发**（发布 APK、上架应用商店、提供网络服务）都必须公开完整源代码并保留版权声明，**不可以闭源**。

**免责声明**：本应用仅提供播放器功能，不制作、不收集、不发布、不存储任何流媒体内容。应用内不内置任何订阅源，所有内容均由用户自行导入，使用风险由使用者自行承担。请勿用于任何商业用途。

## 𝟭. 更新记录

>* **小影盒 v1.0.0：** 首个版本。基于 TVboxOSC 3.0.3 源码二次开发：应用名与包名独立、不内置任何订阅源、使用独立签名密钥、更新检查与自动发布接入本仓库。
