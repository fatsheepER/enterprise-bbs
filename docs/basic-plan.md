# 企业 BBS 系统基础方案 v2

## 1. 项目定位

本项目用于软件工程课程设计，目标是在较短周期内完成一个功能完整、界面简洁、便于答辩演示的企业 BBS 系统。

优先级如下：

1. 覆盖题目要求的核心功能：用户注册、登录、修改信息、浏览帖子、发表帖子、回复帖子、删除帖子；管理员登录、帖子管理、版块管理。
2. 复用上一学期电子相册项目中已经验证过的前后端分离结构。
3. 保持代码结构清晰，便于分工、调试、写报告和演示。
4. 前台界面参考 Apple Developer Forums 的信息组织方式，追求简洁、留白充足、层级清楚。
5. 优先完成可演示 demo，报告和细节优化后置。

参考界面：https://developer.apple.com/forums/

## 2. 技术方案

### 后端

- Java 17
- Spring Boot
- Spring MVC
- MyBatis
- PostgreSQL
- Lombok
- Maven

后端沿用电子相册项目的分层方式：

```text
controller      接收请求，返回统一 Result
service         定义业务接口
service.impl    实现业务逻辑
mapper          MyBatis 数据访问
entity          数据库实体
dto             请求参数对象
vo              页面展示对象
common          通用响应、工具类
config          跨域、静态资源、Web 配置
```

说明：题目 PDF 中提到 MySQL 5.5 以上，但现有项目使用 PostgreSQL。若老师不强制检查数据库类型，建议继续使用 PostgreSQL 以节省迁移成本；若需要严格贴合题目，再将 SQL 方言调整为 MySQL。

### 前端

- Vue 3
- Vite
- Vue Router
- Axios
- Element Plus
- Pinia 可选

前端建议继续采用：

```text
src/router          路由
src/views           页面
src/views/admin     管理员页面
src/components      可复用组件
src/api             Axios 接口封装
src/assets          全局样式
```

## 3. 角色与权限

系统保留两类角色：

- 普通用户：注册、登录、修改资料、浏览版块、浏览帖子、发表帖子、回复帖子、删除自己的帖子。
- 管理员：登录、浏览版块、浏览帖子、发表帖子、回复帖子、管理帖子、管理版块。

快速实现阶段可以沿用旧项目方式：

- 用户登录成功后将用户信息保存到 `localStorage`。
- 使用 `role` 字段区分普通用户和管理员。
- 前端根据角色控制入口和页面显示。
- 后端接口暂不引入完整 JWT 鉴权，优先保证课程设计功能闭环。

若后续时间充足，可以补充：

- BCrypt 密码加密。
- JWT 登录令牌。
- 管理员接口权限校验。

## 4. 功能范围

### 前台登录用户功能

必须实现：

- 用户注册
- 用户登录
- 修改用户信息
- 浏览版块
- 浏览帖子列表
- 查看帖子详情
- 发表帖子
- 回复帖子
- 删除自己发表的帖子

说明：发表帖子和回复帖子面向所有已登录账号开放，普通用户和管理员都可以使用。

建议实现：

- 首页展示版块总览和最新帖子预览
- 按版块筛选帖子
- 帖子标题关键词搜索
- 帖子浏览量
- 回复数量展示
- 最近更新时间展示
- 回复支持 `parent_reply_id`，用于实现轻量的引用式回复

暂不优先实现：

- 私信
- 点赞
- 收藏
- 富文本编辑器
- 图片附件
- 复杂审核流
- 复杂嵌套回复 UI

### 管理员功能

必须实现：

- 管理员登录
- 版块管理：新增、修改、删除、启用/停用
- 帖子管理：查看、隐藏/恢复帖子
- 回复状态管理：隐藏/恢复回复

建议实现：

- 管理端统计卡片：用户数、启用版块数、正常帖子数、正常回复数
- 管理端按 ID、名称关键词、标题关键词、版块、状态筛选内容

暂不优先实现：

- 管理员权限分级
- 操作日志
- 用户封禁
- 内容敏感词审核

## 5. 页面规划

### 公共页面

- `/login`：登录页
- `/register`：注册页

登录页可以比旧项目更精致，但不应占用过多开发时间。

### 前台页面

- `/`：BBS 首页，展示版块总览、最新帖子预览、搜索入口和发帖入口
- `/posts`：全站帖子列表，支持搜索、排序和按版块筛选
- `/boards/:id`：版块详情页，展示该版块下的帖子
- `/posts/:id`：帖子详情页，展示帖子正文和回复流
- `/create-post`：发表帖子
- `/profile`：个人资料

说明：普通用户前台不整体挂在 `/user` 下面。`/user` 更适合作为用户中心或用户个人主页的路径，而论坛公共内容区应直接使用 `/`、`/posts`、`/boards/:id` 等路由。

### 管理员页面

- `/admin`：管理员概览页，展示用户数、启用版块数、正常帖子数、正常回复数，展示最新帖子和最新回复，并提供进入版块管理、帖子管理和回复管理的入口
- `/admin/boards`：版块管理，列表中显示版块 ID，支持按 ID 精确搜索或按名称关键词搜索；正常版块在上方，停用版块移动到底部单独 section，并支持恢复启用
- `/admin/posts`：帖子管理，列表中显示帖子 ID，支持按 ID 精确搜索、按帖子标题关键词搜索、按版块筛选、按状态筛选；正常帖子在上方，隐藏帖子移动到底部单独 section，并支持恢复显示
- `/admin/replies`：回复管理，列表中显示回复 ID、帖子 ID/标题、作者和内容摘要，支持按 ID 精确搜索、按帖子标题关键词搜索、按状态筛选；正常回复在上方，隐藏回复移动到底部单独 section，并支持恢复显示

管理端保持传统后台布局：左侧菜单、顶部用户信息、主区域表格与表单。

## 6. 数据库设计

建议使用 4 张核心表完成题目要求。

### users

用户表。

字段建议：

- `id`
- `username`
- `password`
- `nickname`
- `avatar`
- `email`
- `bio`
- `role`
- `created_at`
- `updated_at`

### boards

版块表。

字段建议：

- `id`
- `name`
- `description`
- `color_hex`
- `sort_order`
- `status`
- `created_at`
- `updated_at`

说明：`color_hex` 用于前台版块卡片或侧边栏的彩色色条，参考 Apple Developer Forums 的版块视觉提示。该字段只承担展示作用，不参与复杂业务逻辑。

### posts

帖子表。

字段建议：

- `id`
- `board_id`
- `user_id`
- `title`
- `content`
- `status`
- `view_count`
- `created_at`
- `updated_at`

### replies

回复表。

字段建议：

- `id`
- `post_id`
- `user_id`
- `parent_reply_id`
- `content`
- `status`
- `created_at`
- `updated_at`

说明：

- `parent_reply_id` 为空时，表示这是对帖子的直接回复。
- `parent_reply_id` 不为空时，表示这是对某条回复的回复。
- 前端不做复杂嵌套回复 UI，只在回复内容上方用简洁 callout 显示父回复的摘要信息。

状态字段建议：

```text
status = 1 正常
status = 0 隐藏或停用
```

## 7. 后端接口规划

### 用户接口

- `POST /user/register`
- `POST /user/login`
- `GET /user/profile`
- `PUT /user/profile`

### 版块接口

- `GET /boards`
- `GET /boards/{id}`
- `POST /admin/boards`
- `PUT /admin/boards/{id}`
- `DELETE /admin/boards/{id}`

### 帖子接口

- `GET /posts`
- `GET /posts/{id}`
- `POST /posts`
- `DELETE /posts/{id}`
- `GET /admin/posts`
- `PUT /admin/posts/{id}/status`

### 回复接口

- `GET /posts/{postId}/replies`
- `POST /posts/{postId}/replies`
- `DELETE /replies/{id}`
- `GET /admin/replies`
- `GET /admin/posts/{postId}/replies`
- `PUT /admin/replies/{id}/status`
- `DELETE /admin/replies/{id}`

说明：新增回复时允许携带 `parentReplyId`。为空表示直接回复帖子，不为空表示引用式回复某条回复。管理员回复接口用于在回复管理页中查看、隐藏和恢复回复；`GET /admin/posts/{postId}/replies` 可用于查看单个帖子下的回复。

### 管理端统计接口

- `GET /admin/dashboard/stats`

用于 `/admin` 概览卡片展示用户数、启用版块数、正常帖子数、正常回复数，也可返回停用版块数、隐藏帖子数、隐藏回复数供管理入口展示。这里的 `dashboard` 只表示统计接口命名，不对应单独的 `/admin/dashboard` 页面。

## 8. UI 基调

前台界面参考 Apple Developer Forums，但只借鉴信息架构和观感，不做逐像素复刻。

基础原则：

- 白色或接近白色背景。
- 内容宽度收敛，避免满屏散开。
- 首页突出版块导航和最新讨论。
- 版块可以使用 `color_hex` 展示彩色色条。
- 帖子列表重点展示标题、所属版块、作者、回复数、浏览数、更新时间。
- 详情页重点突出帖子正文和回复流。
- 回复的回复采用引用 callout 展示父回复摘要，不做树状嵌套。
- 管理端使用 Element Plus 的表格、表单、弹窗和分页组件快速完成。

视觉关键词：

```text
简洁
清晰
轻量
留白
低饱和
开发者社区感
```

## 9. 开发约定

- 先保证功能闭环，再优化样式。
- 后端接口统一返回 `Result<T>`。
- 数据库字段使用下划线命名，Java 字段使用驼峰命名。
- 前端接口统一走 `/api` 代理。
- 页面命名尽量贴近业务，例如 `Home.vue`、`PostList.vue`、`BoardDetail.vue`、`PostDetail.vue`、`BoardManage.vue`。
- 不做超出题目要求太多的功能，避免压缩核心功能和报告时间。
- demo 数据应提前准备，包含若干版块、用户、帖子和回复，确保演示流程顺畅。

## 10. Demo 优先级

两天内优先完成以下闭环：

1. 数据库表与基础测试数据。
2. 用户注册、登录、个人资料修改。
3. 首页版块总览与最新帖子预览。
4. 帖子列表、帖子详情、发帖、回复。
5. 回复支持轻量引用展示。
6. 管理员版块管理。
7. 管理员帖子管理。
8. 基础样式统一与演示数据整理。

不在 demo 阶段追求复杂权限、安全增强、富文本、附件、通知、私信等功能。
