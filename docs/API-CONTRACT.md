# 企业 BBS 系统 API 协议

本文档约定企业 BBS 系统前后端接口。目标是让前端可以直接按本文 mock 数据，后端可以按本文实现 Controller、DTO、VO 和统一响应。

## 1. 基础约定

### 1.1 Base URL

前端所有请求统一以 `/api` 开头：

```text
/api
```

例如前端请求登录接口：

```text
POST /api/user/login
```

后端若未配置 `server.servlet.context-path=/api`，则由 Vite 代理将 `/api` 转发到后端服务，并按需要去掉 `/api` 前缀。

### 1.2 数据格式

- 请求体格式：`application/json`
- 响应体格式：`application/json`
- JSON 字段统一使用小驼峰命名，例如 `boardId`、`createdAt`
- 数据库字段使用下划线命名，例如 `board_id`、`created_at`
- 时间字段统一返回字符串，格式为 `yyyy-MM-dd HH:mm:ss`
- ID 字段统一使用整数类型
- 状态字段统一使用整数：`1` 表示正常/启用，`0` 表示隐藏/停用

## 2. 统一响应格式

所有接口返回统一 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-05-23 16:30:00"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `code` | number | 是 | 业务状态码，成功固定为 `200` |
| `message` | string | 是 | 成功或错误提示 |
| `data` | any | 否 | 业务数据；无数据时可为 `null` |
| `timestamp` | string | 是 | 服务端响应时间 |

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice"
  },
  "timestamp": "2026-05-23 16:30:00"
}
```

失败响应示例：

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": "2026-05-23 16:30:00"
}
```

## 3. 登录态约定

本课程设计 demo 阶段暂不引入完整 JWT 鉴权，采用轻量登录态约定。

### 3.1 登录后前端保存

用户或管理员登录成功后，前端将 `data.user` 保存到 `localStorage`：

```text
localStorage.currentUser = JSON.stringify(data.user)
```

`user.role` 用于判断普通用户和管理员：

```text
USER  普通用户
ADMIN 管理员
```

### 3.2 受保护接口请求头

需要登录的接口，前端统一携带：

```text
X-User-Id: 当前登录用户 id
X-User-Role: 当前登录用户 role
```

示例：

```text
X-User-Id: 1
X-User-Role: USER
```

管理员接口必须携带：

```text
X-User-Role: ADMIN
```

### 3.3 权限规则

- 未登录用户只能访问注册、登录、版块列表、版块详情、帖子列表、帖子详情、回复列表等公开读取接口。
- 普通用户可以修改自己的资料、发帖、回复、删除自己的帖子和回复。
- 管理员可以发帖、回复，并可以访问 `/api/admin/**` 接口管理版块、帖子和回复状态。
- 后端可以根据 `X-User-Id` 和 `X-User-Role` 做基础权限校验。

## 4. 列表返回与前端分页约定

本课程设计演示数据量较小，后端列表接口默认直接返回满足筛选条件的全部数据，不做服务端分页。前端如需分页展示，在拿到完整数组后自行按页切片。

列表响应统一放在 `data` 中，`data` 直接是数组：

```json
{
  "code": 200,
  "message": "success",
  "data": [],
  "timestamp": "2026-05-23 16:30:00"
}
```

后端仍保留必要的筛选和排序参数，例如 `boardId`、`userId`、`keyword`、`status`、`sort`；不要求处理 `page`、`pageSize`、`totalPages` 等分页字段。

## 5. 公共数据模型

### 5.1 UserVO

```json
{
  "id": 1,
  "username": "alice",
  "nickname": "Alice",
  "avatar": "",
  "email": "alice@example.com",
  "bio": "前端开发工程师",
  "role": "USER",
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

### 5.2 BoardVO

```json
{
  "id": 1,
  "name": "技术交流",
  "description": "讨论企业内部技术问题",
  "colorHex": "#007aff",
  "sortOrder": 1,
  "status": 1,
  "postCount": 12,
  "replyCount": 36,
  "latestPost": {
    "id": 8,
    "title": "如何统一接口返回格式",
    "updatedAt": "2026-05-23 16:30:00"
  },
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

### 5.3 PostListItemVO

```json
{
  "id": 1,
  "boardId": 1,
  "boardName": "技术交流",
  "boardColorHex": "#007aff",
  "userId": 1,
  "authorName": "Alice",
  "authorAvatar": "",
  "authorRole": "USER",
  "title": "如何统一接口返回格式",
  "contentPreview": "帖子正文摘要，用于列表展示。",
  "status": 1,
  "viewCount": 20,
  "replyCount": 3,
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

### 5.4 PostDetailVO

```json
{
  "id": 1,
  "boardId": 1,
  "boardName": "技术交流",
  "boardColorHex": "#007aff",
  "userId": 1,
  "authorName": "Alice",
  "authorAvatar": "",
  "authorRole": "USER",
  "title": "如何统一接口返回格式",
  "content": "帖子正文内容",
  "status": 1,
  "viewCount": 21,
  "replyCount": 3,
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

### 5.5 ReplyVO

```json
{
  "id": 1,
  "postId": 1,
  "userId": 2,
  "authorName": "Bob",
  "authorAvatar": "",
  "authorRole": "USER",
  "parentReplyId": null,
  "parentReply": null,
  "content": "我也遇到过这个问题。",
  "status": 1,
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

引用式回复示例：

```json
{
  "id": 2,
  "postId": 1,
  "userId": 3,
  "authorName": "Carol",
  "authorAvatar": "",
  "authorRole": "USER",
  "parentReplyId": 1,
  "parentReply": {
    "id": 1,
    "authorName": "Bob",
    "contentPreview": "我也遇到过这个问题。"
  },
  "content": "可以先从统一 Result 开始。",
  "status": 1,
  "createdAt": "2026-05-23 16:35:00",
  "updatedAt": "2026-05-23 16:35:00"
}
```

### 5.6 UserReplyListItemVO

用于个人主页展示当前登录用户发表过的回复。`reference` 字段永远存在：如果 `parentReplyId` 不为空，引用父回复摘要；如果 `parentReplyId` 为空，引用原帖摘要。

```json
{
  "id": 1,
  "postId": 1,
  "userId": 2,
  "authorName": "Alice",
  "authorAvatar": "",
  "authorRole": "USER",
  "parentReplyId": null,
  "content": "我也遇到过这个问题。",
  "status": 1,
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00",
  "postTitle": "如何统一接口返回格式",
  "postContentPreview": "帖子正文摘要，用于个人主页引用展示。",
  "boardId": 1,
  "boardName": "技术交流",
  "boardColorHex": "#007aff",
  "reference": {
    "type": "post",
    "id": 1,
    "authorName": "Bob",
    "contentPreview": "帖子正文摘要，用于个人主页引用展示。",
    "href": "/posts/1"
  }
}
```

## 6. 用户接口

### 6.1 用户注册

```text
POST /api/user/register
```

公开接口。

请求体：

```json
{
  "username": "alice",
  "password": "123456",
  "nickname": "Alice",
  "email": "alice@example.com"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 登录用户名，长度 3-20，唯一 |
| `password` | string | 是 | 登录密码，长度 6-32 |
| `nickname` | string | 否 | 昵称；为空时默认等于 `username` |
| `email` | string | 否 | 邮箱 |

响应 `data`：

```json
{
  "id": 1,
  "username": "alice",
  "nickname": "Alice",
  "avatar": "",
  "email": "alice@example.com",
  "bio": "",
  "role": "USER",
  "createdAt": "2026-05-23 16:30:00",
  "updatedAt": "2026-05-23 16:30:00"
}
```

### 6.2 用户/管理员登录

```text
POST /api/user/login
```

公开接口。普通用户和管理员共用登录接口，通过返回的 `user.role` 区分身份。

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 密码 |

响应 `data`：

```json
{
  "user": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice",
    "avatar": "",
    "email": "alice@example.com",
    "bio": "",
    "role": "USER",
    "createdAt": "2026-05-23 16:30:00",
    "updatedAt": "2026-05-23 16:30:00"
  }
}
```

### 6.3 获取个人资料

```text
GET /api/user/profile
```

需要登录。

请求头：

```text
X-User-Id: 1
X-User-Role: USER
```

响应 `data`：`UserVO`

### 6.4 修改个人资料

```text
PUT /api/user/profile
```

需要登录。

请求头：

```text
X-User-Id: 1
X-User-Role: USER
```

请求体：

```json
{
  "nickname": "Alice Wang",
  "avatar": "https://example.com/avatar.png",
  "email": "alice@example.com",
  "bio": "企业 BBS 用户"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `nickname` | string | 否 | 昵称 |
| `avatar` | string | 否 | 头像 URL |
| `email` | string | 否 | 邮箱 |
| `bio` | string | 否 | 个人简介 |

说明：普通用户不能通过该接口修改 `username` 或 `role`。

响应 `data`：`UserVO`

### 6.5 修改密码

```text
PUT /api/user/password
```

需要登录。

请求头：

```text
X-User-Id: 1
X-User-Role: USER
```

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "new123456"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `oldPassword` | string | 是 | 当前密码 |
| `newPassword` | string | 是 | 新密码，长度 6-32 |

响应 `data`：

```json
true
```

### 6.6 获取我的回复列表

```text
GET /api/user/replies
```

需要登录。用于个人主页展示当前登录用户发表过的回复。

请求头：

```text
X-User-Id: 1
X-User-Role: USER
```

响应 `data`：数组，元素为 `UserReplyListItemVO`；前端个人主页自行分页展示。

## 7. 版块接口

### 7.1 获取版块列表

```text
GET /api/boards
```

公开接口。前台默认只返回 `status=1` 的版块；管理员版块管理使用管理员接口。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `keyword` | string | 否 | 空 | 按版块名称模糊搜索 |

响应 `data`：

```json
[
  {
    "id": 1,
    "name": "技术交流",
    "description": "讨论企业内部技术问题",
    "colorHex": "#007aff",
    "sortOrder": 1,
    "status": 1,
    "postCount": 12,
    "replyCount": 36,
    "latestPost": {
      "id": 8,
      "title": "如何统一接口返回格式",
      "updatedAt": "2026-05-23 16:30:00"
    },
    "createdAt": "2026-05-23 16:30:00",
    "updatedAt": "2026-05-23 16:30:00"
  }
]
```

### 7.2 获取版块详情

```text
GET /api/boards/{id}
```

公开接口。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 版块 ID |

响应 `data`：`BoardVO`

## 8. 帖子接口

### 8.1 获取帖子列表

```text
GET /api/posts
```

公开接口。前台默认只返回 `status=1` 的帖子。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `boardId` | number | 否 | 空 | 按版块筛选 |
| `userId` | number | 否 | 空 | 按发帖用户筛选，用于个人主页或本用户帖子列表 |
| `keyword` | string | 否 | 空 | 按标题关键词搜索 |
| `sort` | string | 否 | `latest` | 排序：`latest` 最新回复/更新，`newest` 最新发布，`views` 浏览量 |

`boardId`、`userId`、`keyword` 和 `sort` 可以组合使用；前台仍只返回 `status=1` 的帖子。

响应 `data`：数组，元素为 `PostListItemVO`；前端自行分页展示。

示例：

```json
[
  {
    "id": 1,
    "boardId": 1,
    "boardName": "技术交流",
    "boardColorHex": "#007aff",
    "userId": 1,
    "authorName": "Alice",
    "authorAvatar": "",
    "authorRole": "USER",
    "title": "如何统一接口返回格式",
    "contentPreview": "我在整理前端 Axios 封装，希望所有接口都返回统一的 code、message、data 和 timestamp...",
    "status": 1,
    "viewCount": 20,
    "replyCount": 3,
    "createdAt": "2026-05-23 16:30:00",
    "updatedAt": "2026-05-23 16:30:00"
  }
]
```

### 8.2 获取帖子详情

```text
GET /api/posts/{id}
```

公开接口。后端可以在访问详情时将 `viewCount` 加 1。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 帖子 ID |

响应 `data`：`PostDetailVO`

### 8.3 发表帖子

```text
POST /api/posts
```

需要登录。普通用户和管理员均可发表帖子；管理员发帖时仍使用同一接口，不走 `/api/admin/**`。

请求头：

```text
X-User-Id: 1
X-User-Role: USER 或 ADMIN
```

请求体：

```json
{
  "boardId": 1,
  "title": "如何统一接口返回格式",
  "content": "帖子正文内容"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `boardId` | number | 是 | 所属版块 ID |
| `title` | string | 是 | 帖子标题，长度 1-100 |
| `content` | string | 是 | 帖子正文，长度 1-5000 |

响应 `data`：`PostDetailVO`

### 8.4 删除自己的帖子

```text
DELETE /api/posts/{id}
```

需要登录。普通用户只能删除自己发表的帖子。建议后端软删除，将帖子 `status` 更新为 `0`。

请求头：

```text
X-User-Id: 1
X-User-Role: USER
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 帖子 ID |

响应 `data`：

```json
true
```

## 9. 回复接口

### 9.1 获取帖子回复列表

```text
GET /api/posts/{postId}/replies
```

公开接口。前台默认只返回 `status=1` 的回复。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `postId` | number | 是 | 帖子 ID |

响应 `data`：数组，元素为 `ReplyVO`；前端帖子详情页自行分页或直接展示。

### 9.2 发表回复

```text
POST /api/posts/{postId}/replies
```

需要登录。普通用户和管理员均可发表回复。`parentReplyId` 为空表示直接回复帖子，不为空表示引用式回复某条回复。

请求头：

```text
X-User-Id: 2
X-User-Role: USER 或 ADMIN
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `postId` | number | 是 | 帖子 ID |

请求体：

```json
{
  "parentReplyId": null,
  "content": "我也遇到过这个问题。"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `parentReplyId` | number/null | 否 | 父回复 ID；为空表示直接回复帖子 |
| `content` | string | 是 | 回复内容，长度 1-2000 |

响应 `data`：`ReplyVO`

### 9.3 删除自己的回复

```text
DELETE /api/replies/{id}
```

需要登录。普通用户只能删除自己的回复。建议后端软删除，将回复 `status` 更新为 `0`。

请求头：

```text
X-User-Id: 2
X-User-Role: USER
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 回复 ID |

响应 `data`：

```json
true
```

## 10. 管理员接口

管理员接口统一需要请求头：

```text
X-User-Id: 100
X-User-Role: ADMIN
```

### 10.1 管理端统计

```text
GET /api/admin/dashboard/stats
```

响应 `data`：

```json
{
  "userCount": 20,
  "boardCount": 4,
  "disabledBoardCount": 1,
  "postCount": 80,
  "hiddenPostCount": 6,
  "replyCount": 240,
  "hiddenReplyCount": 12
}
```

字段语义：

- `userCount`：用户总数；当前 `users` 表没有 `status` 字段，因此不区分启用和停用。
- `boardCount`、`postCount`、`replyCount`：`status=1` 的启用/正常数量。
- `disabledBoardCount`、`hiddenPostCount`、`hiddenReplyCount`：`status=0` 的停用/隐藏数量。

### 10.2 管理端获取版块列表

```text
GET /api/admin/boards
```

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | number | 否 | 空 | 按版块 ID 精确查询 |
| `keyword` | string | 否 | 空 | 按版块名称模糊搜索 |
| `status` | number | 否 | 空 | 状态：`1` 正常，`0` 停用 |

响应 `data`：

```json
[
  {
    "id": 1,
    "name": "技术交流",
    "description": "讨论企业内部技术问题",
    "colorHex": "#007aff",
    "sortOrder": 1,
    "status": 1,
    "postCount": 12,
    "replyCount": 36,
    "latestPost": {
      "id": 8,
      "title": "如何统一接口返回格式",
      "updatedAt": "2026-05-23 16:30:00"
    },
    "createdAt": "2026-05-23 16:30:00",
    "updatedAt": "2026-05-23 16:30:00"
  }
]
```

### 10.3 新增版块

```text
POST /api/admin/boards
```

请求体：

```json
{
  "name": "技术交流",
  "description": "讨论企业内部技术问题",
  "colorHex": "#007aff",
  "sortOrder": 1,
  "status": 1
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | string | 是 | 版块名称，长度 1-50，唯一 |
| `description` | string | 否 | 版块描述 |
| `colorHex` | string | 否 | 展示颜色，例如 `#007aff` |
| `sortOrder` | number | 否 | 排序值，越小越靠前 |
| `status` | number | 否 | 状态：`1` 正常，`0` 停用；默认 `1` |

响应 `data`：`BoardVO`

### 10.4 修改版块

```text
PUT /api/admin/boards/{id}
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 版块 ID |

请求体：

```json
{
  "name": "技术问答",
  "description": "讨论研发相关问题",
  "colorHex": "#34c759",
  "sortOrder": 2,
  "status": 1
}
```

请求字段同新增版块。响应 `data`：`BoardVO`

### 10.5 删除版块

```text
DELETE /api/admin/boards/{id}
```

建议后端软删除或停用，将版块 `status` 更新为 `0`。若版块下已有帖子，不建议物理删除。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 版块 ID |

响应 `data`：

```json
true
```

### 10.6 管理端获取帖子列表

```text
GET /api/admin/posts
```

管理员可查看正常和隐藏帖子。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | number | 否 | 空 | 按帖子 ID 精确查询 |
| `boardId` | number | 否 | 空 | 按版块筛选 |
| `keyword` | string | 否 | 空 | 按标题关键词搜索 |
| `status` | number | 否 | 空 | 状态：`1` 正常，`0` 隐藏 |

响应 `data`：数组，元素为 `PostListItemVO`；管理端自行分页展示。

### 10.7 修改帖子状态

```text
PUT /api/admin/posts/{id}/status
```

用于隐藏或恢复帖子。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 帖子 ID |

请求体：

```json
{
  "status": 0
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | number | 是 | `1` 正常，`0` 隐藏 |

响应 `data`：`PostDetailVO`

### 10.8 管理员删除帖子

```text
DELETE /api/admin/posts/{id}
```

用于管理员从管理端隐藏帖子。语义等同于将帖子 `status` 更新为 `0`。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 帖子 ID |

响应 `data`：

```json
true
```

### 10.9 管理端获取全部回复列表

```text
GET /api/admin/replies
```

管理员可查看全部正常和隐藏回复，用于独立的回复管理页。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | number | 否 | 空 | 按回复 ID 精确查询 |
| `postId` | number | 否 | 空 | 按帖子 ID 筛选 |
| `keyword` | string | 否 | 空 | 按回复内容或所属帖子标题模糊搜索 |
| `status` | number | 否 | 空 | 状态：`1` 正常，`0` 隐藏 |

响应 `data`：数组，元素为 `ReplyVO`；管理端自行分页展示。

### 10.10 管理端获取帖子回复列表

```text
GET /api/admin/posts/{postId}/replies
```

管理员可查看某个帖子下的正常和隐藏回复，用于帖子详情或帖子管理上下文。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `postId` | number | 是 | 帖子 ID |

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `status` | number | 否 | 空 | 状态：`1` 正常，`0` 隐藏 |

响应 `data`：数组，元素为 `ReplyVO`；管理端自行分页展示。

### 10.11 修改回复状态

```text
PUT /api/admin/replies/{id}/status
```

用于隐藏或恢复回复。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 回复 ID |

请求体：

```json
{
  "status": 0
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | number | 是 | `1` 正常，`0` 隐藏 |

响应 `data`：`ReplyVO`

### 10.12 管理员删除回复

```text
DELETE /api/admin/replies/{id}
```

用于管理员从管理端隐藏回复。语义等同于将回复 `status` 更新为 `0`，前端优先使用 `PUT /api/admin/replies/{id}/status` 表达隐藏和恢复。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 回复 ID |

响应 `data`：

```json
true
```

## 11. 状态码与错误信息

### 11.1 HTTP 状态码

| HTTP 状态码 | 使用场景 |
| --- | --- |
| `200` | 请求已处理，业务成功或业务失败都可以返回统一 `Result` |
| `400` | 请求参数格式错误，无法解析 JSON 或参数类型错误 |
| `401` | 未登录或登录态缺失 |
| `403` | 已登录但无权限 |
| `404` | 接口路径不存在 |
| `500` | 服务端异常 |

课程设计实现可简化为大部分接口返回 HTTP `200`，通过响应体 `code` 判断业务结果；但 `401`、`403`、`500` 建议保留。

### 11.2 业务状态码

| code | message | 说明 |
| --- | --- | --- |
| `200` | `success` | 成功 |
| `40000` | `请求参数错误` | 参数缺失、格式错误或校验失败 |
| `40001` | `用户名或密码错误` | 登录失败 |
| `40002` | `用户名已存在` | 注册用户名重复 |
| `40003` | `版块名称已存在` | 新增或修改版块时名称重复 |
| `40004` | `旧密码错误` | 修改密码时当前密码不正确 |
| `40100` | `请先登录` | 缺少 `X-User-Id` 或登录态无效 |
| `40300` | `无权限访问` | 普通用户访问管理员接口或操作他人资源 |
| `40400` | `资源不存在` | 用户、版块、帖子或回复不存在 |
| `40900` | `资源状态不可用` | 版块停用、帖子或回复隐藏等状态不允许继续操作 |
| `50000` | `服务器内部错误` | 未预期服务端异常 |

### 11.3 常见错误响应

未登录：

```json
{
  "code": 40100,
  "message": "请先登录",
  "data": null,
  "timestamp": "2026-05-23 16:30:00"
}
```

无权限：

```json
{
  "code": 40300,
  "message": "无权限访问",
  "data": null,
  "timestamp": "2026-05-23 16:30:00"
}
```

资源不存在：

```json
{
  "code": 40400,
  "message": "资源不存在",
  "data": null,
  "timestamp": "2026-05-23 16:30:00"
}
```

参数错误：

```json
{
  "code": 40000,
  "message": "帖子标题不能为空",
  "data": null,
  "timestamp": "2026-05-23 16:30:00"
}
```

## 12. 接口总览

| 模块 | 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- | --- |
| 用户 | `POST` | `/api/user/register` | 公开 | 用户注册 |
| 用户 | `POST` | `/api/user/login` | 公开 | 用户/管理员登录 |
| 用户 | `GET` | `/api/user/profile` | 登录 | 获取个人资料 |
| 用户 | `PUT` | `/api/user/profile` | 登录 | 修改个人资料 |
| 用户 | `PUT` | `/api/user/password` | 登录 | 修改密码 |
| 用户 | `GET` | `/api/user/replies` | 登录 | 获取我的回复列表 |
| 版块 | `GET` | `/api/boards` | 公开 | 获取启用版块列表 |
| 版块 | `GET` | `/api/boards/{id}` | 公开 | 获取版块详情 |
| 帖子 | `GET` | `/api/posts` | 公开 | 获取帖子列表 |
| 帖子 | `GET` | `/api/posts/{id}` | 公开 | 获取帖子详情 |
| 帖子 | `POST` | `/api/posts` | 登录（USER/ADMIN） | 发表帖子 |
| 帖子 | `DELETE` | `/api/posts/{id}` | 登录 | 删除自己的帖子 |
| 回复 | `GET` | `/api/posts/{postId}/replies` | 公开 | 获取帖子回复 |
| 回复 | `POST` | `/api/posts/{postId}/replies` | 登录（USER/ADMIN） | 发表回复 |
| 回复 | `DELETE` | `/api/replies/{id}` | 登录 | 删除自己的回复 |
| 管理员 | `GET` | `/api/admin/dashboard/stats` | 管理员 | 管理端统计 |
| 管理员 | `GET` | `/api/admin/boards` | 管理员 | 管理端版块列表 |
| 管理员 | `POST` | `/api/admin/boards` | 管理员 | 新增版块 |
| 管理员 | `PUT` | `/api/admin/boards/{id}` | 管理员 | 修改版块 |
| 管理员 | `DELETE` | `/api/admin/boards/{id}` | 管理员 | 删除或停用版块 |
| 管理员 | `GET` | `/api/admin/posts` | 管理员 | 管理端帖子列表 |
| 管理员 | `PUT` | `/api/admin/posts/{id}/status` | 管理员 | 修改帖子状态 |
| 管理员 | `DELETE` | `/api/admin/posts/{id}` | 管理员 | 管理员删除或隐藏帖子 |
| 管理员 | `GET` | `/api/admin/replies` | 管理员 | 管理端全部回复列表 |
| 管理员 | `GET` | `/api/admin/posts/{postId}/replies` | 管理员 | 管理端帖子回复列表 |
| 管理员 | `PUT` | `/api/admin/replies/{id}/status` | 管理员 | 修改回复状态 |
| 管理员 | `DELETE` | `/api/admin/replies/{id}` | 管理员 | 管理员删除或隐藏回复 |
