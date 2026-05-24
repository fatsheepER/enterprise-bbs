# 企业 BBS 后端任务交接文档

本文档基于 `docs/basic-plan.md`、`docs/API-CONTRACT.md`、`sql/schema.sql`、`sql/data.sql` 和当前 `frontend/` 实现整理，用于把接下来的后端实现、前端联调改造和验收顺序对齐。

## 1. 当前结论

前端已经完成了一个可演示的 mock 版本，覆盖了基础方案中的主要页面：

- 前台：版块总览、全部帖子、版块详情、帖子详情、登录、注册、发帖、个人主页、资料编辑。
- 管理端：概览、版块管理、帖子管理、回复管理。
- 交互：登录态、角色入口、发帖、回帖、编辑资料、修改密码、管理端新建/编辑版块、隐藏/恢复帖子、隐藏/恢复回复。
- 数据：`src/mock/users.js`、`boards.js`、`posts.js`、`replies.js` 和 `forumViewModels.js` 已经按四张核心表模拟了主要 VO。

但前端目前还不是可联调状态：

- `frontend/src/api/request.js` 只有注释，没有真正的 Axios 实例、`Result<T>` 解包、错误处理和登录请求头注入。
- 页面直接 import `@/mock/forumViewModels` 或 `../mock/users`，mock 查询和写入逻辑分散在页面与 Pinia store 中。
- 发帖会写入 mock 数组，但刷新后丢失；回帖只追加到当前详情页 `replies` 局部状态，离开页面后丢失；管理端状态修改也只影响内存数据。
- 搜索框、排序按钮、部分筛选条件还只是 UI 或本地简化筛选，没有完全对应 API 契约。
- 普通用户删除自己帖子、删除自己回复的 UI 还未实现。
- 前端路由已统一为 `/boards/:id`、`/posts/:id`，后续文档、跳转和后端返回的 `href` 字段都按复数路径处理。

整体判断：

- 作为课程设计静态/mock 前端：完成度约 80%。
- 作为后端联调前端：完成度约 55%-60%，核心页面已在，但需要补 API service 层和异步状态。
- 后端当前只有 Spring Boot 启动类和配置，业务实现基本从零开始；不过 SQL、API 契约和前端 VO 形态已经比较明确。

已验证：

```text
cd frontend
npm run build
```

结果：Vite production build 通过。

## 2. 现有前端功能清单

### 2.1 已覆盖页面

| 功能 | 当前页面/文件 | 当前状态 |
| --- | --- | --- |
| 版块总览 | `frontend/src/views/front/BoardsOverviewView.vue` | 已展示启用版块和每个版块最新帖子 |
| 全部帖子 | `frontend/src/views/front/PostsView.vue` | 已展示全部正常帖子；排序按钮未真正交互 |
| 版块详情 | `frontend/src/views/front/BoardDetailView.vue` | 已展示指定版块信息和该版块帖子 |
| 帖子详情 | `frontend/src/views/front/PostDetailView.vue` | 已展示正文、回复流、引用回复、浮动回复入口 |
| 登录 | `frontend/src/views/front/LoginView.vue` | mock 用户校验，写入 `localStorage.currentUser` |
| 注册 | `frontend/src/views/front/RegisterView.vue` | mock 注册，写入 `localStorage.mockUsers` 和当前用户 |
| 发帖 | `frontend/src/views/front/CreatePostView.vue` | 登录后可发帖，写入内存 mock posts |
| 个人主页 | `frontend/src/views/front/ProfileView.vue` | 展示当前用户资料、发帖、回复 |
| 编辑资料 | `frontend/src/views/front/ProfileEditView.vue` | 可修改 mock 资料和密码 |
| 管理端概览 | `frontend/src/views/admin/AdminDashboardView.vue` | 展示 mock 统计和最新内容 |
| 版块管理 | `frontend/src/views/admin/AdminBoardsView.vue` | 可新建、编辑、启停 mock 版块 |
| 帖子管理 | `frontend/src/views/admin/AdminPostsView.vue` | 可隐藏/恢复 mock 帖子，带本地分页 |
| 回复管理 | `frontend/src/views/admin/AdminRepliesView.vue` | 可隐藏/恢复 mock 回复，带本地分页 |

### 2.2 已覆盖但需要后端接管的行为

- 登录/注册/登出：当前在 `frontend/src/stores/auth.js` 内直接读写 mock 用户和 `localStorage`。
- 个人资料和密码：当前由 `authStore.updateProfile()`、`authStore.changePassword()` 本地处理。
- 公开读取：`visibleBoards()`、`visiblePosts()`、`postDetail()`、`postReplies()` 等来自 `forumViewModels.js`。
- 管理端读取和写入：`adminBoards()`、`createAdminBoard()`、`updateAdminBoard()`、`adminPosts()`、`updateAdminPostStatus()`、`adminReplies()`、`updateAdminReplyStatus()`。

这些函数的返回字段和 `docs/API-CONTRACT.md` 中的 VO 接近，可以作为后端实现和前端 service 层的字段参考。

### 2.3 前端待补缺口

| 缺口 | 影响 | 建议处理 |
| --- | --- | --- |
| 没有真正 API client | 无法联调 | 先实现 `src/api/request.js` 和按模块 API service |
| 页面直接依赖 mock | 替换成本高 | 每个页面改为调用 service，mock 只保留为备用数据源 |
| 列表分页仍是本地逻辑 | 演示数据不多，可以接受 | 后端返回完整数组，前端继续本地分页展示 |
| Header 搜索框未绑定行为 | 演示中搜索不可用 | 后端完成 `/api/posts?keyword=` 后再接入 |
| 全部帖子/版块详情排序按钮未绑定 | UI 和实际行为不一致 | 接入 `/api/posts?sort=latest/newest/views` |
| 用户删除帖子/回复 UI 缺失 | 基础功能未闭环 | 增加“删除自己的帖子/回复”按钮并调用 DELETE 接口 |
| 路由单复数已统一 | 需要后续保持一致 | 统一使用 `/posts/:id`、`/boards/:id` |
| 管理端筛选不完整 | API 支持但 UI 未完全覆盖 | 帖子补 board/status 筛选，回复补 postId/status 或明确不做 |

## 3. 后端实现总目标

后端按 `docs/API-CONTRACT.md` 实现接口，使用当前 SQL 的四张表：

- `users`
- `boards`
- `posts`
- `replies`

后端不需要引入复杂 JWT。课程设计 demo 阶段采用轻量登录态：

- 登录成功返回 `data.user`。
- 前端保存到 `localStorage.currentUser`。
- 需要登录的请求带 `X-User-Id` 和 `X-User-Role`。
- 后端基于请求头做基础用户存在性、角色和资源归属校验。

注意当前 `frontend/vite.config.js` 会把浏览器侧 `/api/**` 转发到 `http://localhost:8080/**`，并去掉 `/api` 前缀：

```js
rewrite: path => path.replace(/^\/api/, '')
```

因此本地联调有两种选择，必须二选一：

1. 保持 Vite rewrite：后端 Controller 暴露 `/user/login`、`/boards`、`/posts`、`/admin/**`。
2. 后端配置 `server.servlet.context-path=/api` 或 Controller 自带 `/api` 前缀：前端移除 Vite rewrite。

建议保留当前 Vite rewrite，后端 Controller 不写 `/api` 前缀；浏览器和 API 文档仍统一描述为 `/api/...`。

## 4. 后端工程结构建议

按 `basic-plan.md` 中的分层落地：

```text
backend/src/main/java/com/feiyang/bbs/
  common/
    Result.java
    ErrorCode.java
    BusinessException.java
  config/
    WebConfig.java
    GlobalExceptionHandler.java
  controller/
    UserController.java
    BoardController.java
    PostController.java
    ReplyController.java
    AdminController.java
  dto/
    UserRegisterDTO.java
    UserLoginDTO.java
    UserProfileUpdateDTO.java
    PasswordUpdateDTO.java
    PostCreateDTO.java
    ReplyCreateDTO.java
    BoardSaveDTO.java
    StatusUpdateDTO.java
  entity/
    User.java
    Board.java
    Post.java
    Reply.java
  mapper/
    UserMapper.java
    BoardMapper.java
    PostMapper.java
    ReplyMapper.java
  service/
    UserService.java
    BoardService.java
    PostService.java
    ReplyService.java
    AdminService.java
  service/impl/
    ...
  vo/
    UserVO.java
    BoardVO.java
    PostListItemVO.java
    PostDetailVO.java
    ReplyVO.java
    UserReplyListItemVO.java
    AdminDashboardStatsVO.java
```

MyBatis XML 建议放在：

```text
backend/src/main/resources/mapper/*.xml
```

当前 `application.yml` 里 `type-aliases-package` 是 `com.example.bbs.entity`，需要改为：

```yaml
mybatis:
  type-aliases-package: com.feiyang.bbs.entity
```

## 5. 后端任务拆分

### 5.1 第一阶段：基础设施

目标：让所有 Controller 可以统一返回、统一报错，并能读取当前登录用户。

需要完成：

- `Result<T>`
  - `code`
  - `message`
  - `data`
  - `timestamp`
- 列表接口直接在 `Result.data` 中返回数组，不要求后端实现 `PageResult<T>` 或服务端分页。
- `ErrorCode`
  - 对齐 API 契约中的 `200`、`40000`、`40001`、`40002`、`40003`、`40004`、`40100`、`40300`、`40400`、`40900`、`50000`。
- `BusinessException`
  - 用于 service 层主动抛业务错误。
- `GlobalExceptionHandler`
  - 捕获参数校验错误、业务错误和未知异常。
- `CurrentUser`
  - 可用简单工具方法从 `HttpServletRequest` 中读取 `X-User-Id`、`X-User-Role`。
  - 登录接口和公开接口不要求请求头。
  - 写接口和管理接口必须校验请求头。
- CORS
  - 如仅使用 Vite proxy，可以暂不配置。
  - 如前端直连 `localhost:8080`，需要允许 `http://localhost:5173`。

验收：

- 启动后任意测试 Controller 能返回统一 `Result`。
- 缺少登录头时返回 `40100`。
- 普通用户访问管理员接口时返回 `40300`。

### 5.2 第二阶段：实体、DTO、VO 和 Mapper

目标：让 Java 模型和 SQL 字段稳定。

需要完成：

- 四个实体类对应四张表，ID 用 `Long`。
- DTO 按接口请求体拆分，不直接复用实体作为请求对象。
- VO 按前端展示字段拆分：
  - `UserVO` 不返回 `password`。
  - `BoardVO` 需要聚合 `postCount`、`replyCount`、`latestPost`。
  - `PostListItemVO` 需要聚合版块名、版块颜色、作者名、作者头像、作者角色、回复数。
  - `PostDetailVO` 类似 `PostListItemVO`，但包含完整 `content`。
  - `ReplyVO` 需要聚合作者信息和 `parentReply` 摘要。
  - `UserReplyListItemVO` 需要 `reference`，用于个人主页展示。
- Mapper 查询尽量直接返回 VO，避免 service 层大量手动拼接。

关键 SQL 查询：

- 版块列表：启用版块按 `sort_order ASC, id ASC`。
- 版块统计：只统计 `status=1` 的帖子和回复。
- 帖子列表：支持 `boardId`、`userId`、`keyword`、`sort`。
- 管理帖子列表：支持 `id`、`boardId`、`keyword`、`status`。
- 回复列表：按 `created_at ASC` 返回帖子回复。
- 管理回复列表：支持 `id`、`postId`、`keyword`、`status`。

验收：

- Mapper 能基于 `sql/data.sql` 返回和 mock 前端接近的数据。
- 时间字段返回 `yyyy-MM-dd HH:mm:ss`。
- JSON 字段为小驼峰。

### 5.3 第三阶段：用户模块

接口：

- `POST /user/register`
- `POST /user/login`
- `GET /user/profile`
- `PUT /user/profile`
- `PUT /user/password`
- `GET /user/replies`

实现要点：

- 注册校验用户名唯一、长度 3-20、密码 6-32。
- 登录返回 `{ user: UserVO }`，普通用户和管理员共用。
- 密码字段 demo 阶段可以先明文对比，以兼容 `sql/data.sql`；若改 BCrypt，需要同步更新 seed 数据。
- `GET /user/profile` 以 `X-User-Id` 为准，不从 query/body 传用户 ID。
- `GET /user/replies` 返回数组，元素为 `UserReplyListItemVO`；个人主页由前端本地分页。

前端依赖：

- `authStore.login()` 要改为调用登录接口。
- `authStore.register()` 要改为调用注册接口。
- `authStore.updateProfile()`、`changePassword()` 要改为对应 PUT 接口。
- 个人主页回复列表需要调用 `/api/user/replies`。

验收：

- `admin/admin123` 登录返回 `role=ADMIN`。
- `alice/123456` 登录返回 `role=USER`。
- 修改资料后重新获取 profile 能看到变化。
- 修改密码后旧密码不能登录，新密码能登录。

### 5.4 第四阶段：公开版块和帖子模块

接口：

- `GET /boards`
- `GET /boards/{id}`
- `GET /posts`
- `GET /posts/{id}`
- `POST /posts`
- `DELETE /posts/{id}`

实现要点：

- 前台只返回 `status=1` 的版块和帖子。
- `/posts` 支持：
  - `boardId`
  - `userId`
  - `keyword`
  - `sort=latest|newest|views`
- `/posts` 不做服务端分页，直接返回筛选和排序后的完整数组。
- `GET /posts/{id}` 可顺手 `view_count + 1`，但返回值要和更新后的浏览量一致。
- 发帖必须校验：
  - 登录用户存在。
  - 版块存在且 `status=1`。
  - title/content 非空并符合长度。
- 删除自己的帖子是软删除：
  - 普通用户只能删 `posts.user_id = X-User-Id` 的帖子。
  - 管理员若走公开删除接口，也可以允许删除自己的帖子；管理任意帖子走 admin 接口。

前端依赖：

- 首页和版块页改为调用 `/api/boards`、`/api/posts?boardId=...`。
- 全部帖子页改为真实接口取全量数据，排序由后端按 `sort` 处理，分页仍由前端本地完成。
- 发帖页改为 `POST /api/posts`，成功后跳转详情页。
- 个人主页“我的发帖”改为 `/api/posts?userId=currentUser.id`。
- 需要补“删除自己的帖子”按钮，可放在个人主页发帖列表或帖子详情页作者本人可见区域。

验收：

- 未登录可以看版块、帖子列表、帖子详情。
- 未登录发帖返回 `40100`。
- 普通用户不能删除别人的帖子。
- 删除后前台列表不再展示该帖子，管理端仍可看到隐藏状态。

### 5.5 第五阶段：回复模块

接口：

- `GET /posts/{postId}/replies`
- `POST /posts/{postId}/replies`
- `DELETE /replies/{id}`

实现要点：

- 前台回复列表只返回 `status=1`。
- 直接回复帖子时 `parentReplyId = null`。
- 引用回复时要校验父回复存在、`status=1`，且属于同一个 `postId`。
- 新增回复后建议同步更新帖子 `updated_at`，这样帖子列表的 `latest` 排序符合“最近回复”语义。
- 删除自己的回复是软删除。

前端依赖：

- 帖子详情页初次加载调用 `/api/posts/{id}` 和 `/api/posts/{id}/replies`。
- 提交回复改为 `POST /api/posts/{id}/replies`，成功后追加返回的 `ReplyVO` 或重新加载回复列表。
- 需要补“删除自己的回复”按钮，可在回复条目中仅对作者本人显示。

验收：

- 未登录可以看回复，不能发回复。
- 登录用户可以直接回复帖子。
- 登录用户可以引用某条回复。
- 引用隐藏/不存在/其他帖子下的回复返回 `40900` 或 `40400`。
- 回复后帖子更新时间变化，全部帖子按最近回复排序时该帖子上移。

### 5.6 第六阶段：管理员模块

接口：

- `GET /admin/dashboard/stats`
- `GET /admin/boards`
- `POST /admin/boards`
- `PUT /admin/boards/{id}`
- `DELETE /admin/boards/{id}`
- `GET /admin/posts`
- `PUT /admin/posts/{id}/status`
- `DELETE /admin/posts/{id}`
- `GET /admin/replies`
- `GET /admin/posts/{postId}/replies`
- `PUT /admin/replies/{id}/status`
- `DELETE /admin/replies/{id}`

实现要点：

- 所有 `/admin/**` 必须校验 `X-User-Role=ADMIN`。
- 版块删除建议等同停用，`status=0`。
- 帖子删除建议等同隐藏，`status=0`。
- 回复删除建议等同隐藏，`status=0`。
- 管理端列表直接返回筛选后的完整数组，前端继续本地分页展示。
- 管理端统计至少返回契约字段：
  - `userCount`
  - `boardCount`
  - `disabledBoardCount`
  - `postCount`
  - `hiddenPostCount`
  - `replyCount`
  - `hiddenReplyCount`
- 当前前端 mock 还使用了 `totalBoardCount`、`totalPostCount`、`totalReplyCount`、`latestPost`、`latestReply`。后端可以二选一：
  - 补充返回这些字段，减少前端改动。
  - 前端联调时用已有字段计算总数，并单独查询最新内容。

建议为了降低联调成本，`/admin/dashboard/stats` 返回扩展字段：

```json
{
  "userCount": 4,
  "boardCount": 4,
  "totalBoardCount": 5,
  "disabledBoardCount": 1,
  "postCount": 5,
  "totalPostCount": 6,
  "hiddenPostCount": 1,
  "replyCount": 9,
  "totalReplyCount": 10,
  "hiddenReplyCount": 1,
  "latestPost": {},
  "latestReply": {}
}
```

验收：

- 普通用户访问管理端接口返回 `40300`。
- 管理员可以新建、修改、停用、恢复版块。
- 管理员可以隐藏/恢复帖子。
- 管理员可以隐藏/恢复回复。
- 管理端列表返回数组，前端分页展示结果和筛选条件正确。

## 6. 前端联调改造计划

### 6.1 新增 API 基础层

新增或补全：

```text
frontend/src/api/request.js
frontend/src/api/user.js
frontend/src/api/boards.js
frontend/src/api/posts.js
frontend/src/api/replies.js
frontend/src/api/admin.js
```

`request.js` 需要负责：

- `baseURL: '/api'`
- 请求头注入：
  - 从 `localStorage.currentUser` 读取 `id` 和 `role`
  - 设置 `X-User-Id`
  - 设置 `X-User-Role`
- 响应处理：
  - HTTP 成功但 `result.code !== 200` 时 reject `Error(result.message)`
  - 成功时返回 `result.data`
- 错误处理：
  - `40100` 时可清理当前用户并跳转登录页，或先只抛错给页面处理。

### 6.2 按模块替换 mock

替换顺序建议：

1. `authStore`
   - 先替换登录、注册、获取个人资料、修改资料、修改密码。
   - 保持 `localStorage.currentUser` 的结构不变，避免 Header 和路由判断大改。
2. 公开读取
   - `BoardsOverviewView`
   - `PostsView`
   - `BoardDetailView`
   - `PostDetailView`
3. 写操作
   - `CreatePostView`
   - `PostDetailView` 的提交回复
   - 用户删除自己的帖子和回复
4. 管理端
   - `AdminDashboardView`
   - `AdminBoardsView`
   - `AdminPostsView`
   - `AdminRepliesView`

### 6.3 页面状态需要补齐

每个异步页面建议至少有：

- `isLoading`
- `errorMessage`
- `emptyText`
- `page`
- `pageSize`
- `totalPages`
- `refresh()` 或 `loadData()`

当前同步 mock 页面里很多 `const posts = visiblePosts()` 需要改为：

```js
const posts = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

async function loadPosts() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    posts.value = await postApi.listPosts(params)
  } catch (error) {
    errorMessage.value = error.message || '加载失败'
  } finally {
    isLoading.value = false
  }
}
```

### 6.4 路由统一

前端路由已经改成基础方案中的复数形式：

```text
/boards/:id
/posts/:id
```

已同步替换：

- `frontend/src/router/index.js`
- `BoardCard.vue`
- `PostTable.vue`
- `CreatePostView.vue`
- `ProfileView.vue`
- `AdminDashboardView.vue`
- `forumViewModels.js` 中保留的 mock href
- `docs/API-CONTRACT.md` 中的 `/posts/1` 示例

后端返回的任何 `href` 字段和前端跳转都必须继续使用复数路径。

### 6.5 mock 保留策略

建议不要立即删除 `frontend/src/mock`：

- 第一轮联调时保留 mock，便于后端未启动时回退。
- API service 稳定后，再删除页面中的 mock import。
- 最后确认没有页面直接依赖 mock：

```text
rg "@/mock|../mock" frontend/src
```

目标结果：只有 mock 兼容层或测试代码还引用 mock，页面和 store 不再直接引用。

## 7. 联调验收清单

### 7.1 普通用户流程

- 注册新用户。
- 退出后用新用户登录。
- 修改昵称、邮箱、个人简介。
- 修改密码后用新密码登录。
- 浏览版块总览。
- 浏览全部帖子。
- 进入版块详情。
- 进入帖子详情。
- 发布新帖子。
- 回复自己的帖子。
- 引用别人的回复。
- 删除自己的帖子。
- 删除自己的回复。
- 确认不能删除别人的帖子和回复。

### 7.2 管理员流程

- 使用 `admin/admin123` 登录。
- 进入管理员控制台。
- 查看统计卡片。
- 新建版块。
- 修改版块名称、描述、颜色、排序、状态。
- 停用版块后前台不再展示。
- 恢复版块后前台重新展示。
- 隐藏帖子后前台不再展示。
- 恢复帖子后前台重新展示。
- 隐藏回复后帖子详情不再展示。
- 恢复回复后帖子详情重新展示。

### 7.3 权限和错误流程

- 未登录发帖返回“请先登录”。
- 未登录回复返回“请先登录”。
- 普通用户访问 `/api/admin/**` 返回“无权限访问”。
- 访问不存在的帖子、版块、回复返回“资源不存在”。
- 对停用版块发帖返回“资源状态不可用”。
- 引用不存在或隐藏的回复返回错误。

## 8. 建议开发顺序

推荐按下面顺序推进，避免前后端互相等待：

1. 后端先完成基础设施、实体、Mapper 和用户登录。
2. 前端先接入 `request.js` 和 `authStore`，用真实登录验证请求头。
3. 后端完成公开版块和帖子读取。
4. 前端替换首页、帖子列表、版块详情、帖子详情读取逻辑。
5. 后端完成发帖和回复。
6. 前端替换发帖、回帖，并补删除自己的帖子/回复。
7. 后端完成管理端统计、版块管理、帖子管理、回复管理。
8. 前端替换管理端四个页面。
9. 双方按联调验收清单跑一遍 demo 数据。
10. 最后再清理 mock import、无用 store 和未使用组件。

## 9. 后端最小可交付标准

后端第一版不用追求完整工程化，但至少需要满足：

- 启动无报错。
- 能连接 PostgreSQL。
- `sql/schema.sql` 和 `sql/data.sql` 导入后可跑通。
- 所有接口返回统一 `Result<T>`。
- 所有列表接口在 `Result.data` 中直接返回数组。
- 所有写接口有基本参数校验。
- 所有受保护接口读取并校验 `X-User-Id`、`X-User-Role`。
- 普通用户和管理员权限边界正确。
- 公开前台不展示 `status=0` 的版块、帖子和回复。
- 管理端能看到并恢复 `status=0` 数据。

## 10. 需要提前确认的问题

1. 后端 Controller 是否保留无 `/api` 前缀，并继续依赖 Vite proxy 去掉 `/api`？
2. 密码是否保持明文 demo，还是现在就改 BCrypt？
3. 管理端统计是否按前端 mock 扩展返回 `latestPost`、`latestReply` 和 `total*Count`？
4. 管理端帖子/回复列表是否必须马上补全全部筛选 UI，还是先以当前 UI 字段完成联调？

建议答案：

- 保留 Vite rewrite，后端无 `/api` 前缀，减少 Spring 配置。
- 第一版保持明文密码，答辩前有时间再升级 BCrypt。
- 统计接口返回扩展字段，降低前端改造量。
- 先按当前 UI 完成联调，board/status/postId 等额外筛选作为最后补充。
