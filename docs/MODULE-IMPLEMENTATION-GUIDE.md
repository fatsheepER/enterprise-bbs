# 模块实现简明流程

本文档用于在基础设施、实体、DTO、VO、Mapper 已经搭建完成后，按模块继续实现后端业务和前端联调。

当前约定：

- 浏览器侧请求路径使用 `/api/...`。
- Vite 代理会把 `/api` 去掉后转发到后端，因此后端 Controller 暴露 `/user/...`、`/boards/...`、`/posts/...`、`/admin/...`，不额外写 `/api` 前缀。
- 后端统一返回 `Result<T>`，前端 `src/api/request.js` 负责解包 `data`。
- 登录态暂不使用 JWT，受保护接口依赖 `X-User-Id` 和 `X-User-Role`。
- 列表接口直接返回数组，前端本地分页。

## 1. 后端实现一个模块

### 1.1 Service 接口

在 `backend/src/main/java/com/feiyang/bbs/service/` 下新增或补全模块接口，例如：

```text
BoardService.java
PostService.java
ReplyService.java
AdminService.java
```

Service 接口按 API 契约定义方法，不暴露 Controller 细节：

- 入参使用 DTO、路径 ID、当前登录用户 ID/角色。
- 返回值使用 VO 或 `Boolean`。
- 列表返回 `List<VO>`。

示例：

```java
List<PostListItemVO> listVisiblePosts(Long boardId, Long userId, String keyword, String sort);
PostDetailVO createPost(Long currentUserId, PostCreateDTO dto);
Boolean deleteOwnPost(Long currentUserId, Long postId);
```

### 1.2 Service 实现

在 `service/impl/` 下实现业务逻辑。每个写操作建议加 `@Transactional`。

Service 负责：

- 查询资源是否存在，不存在抛 `BusinessException(ErrorCode.NOT_FOUND)`。
- 校验登录用户是否存在。
- 校验权限，例如只能操作自己的帖子/回复，管理员接口必须是 `ADMIN`。
- 校验资源状态，例如停用版块不能发帖，隐藏帖子不能继续回复。
- 调用 Mapper 完成写入、状态更新和 VO 查询。
- 把业务错误转成 `BusinessException`，不要在 Controller 里堆业务判断。

常用错误码：

- `LOGIN_FAILED`：用户名或密码错误。
- `USERNAME_EXISTS`：用户名重复。
- `BOARD_NAME_EXISTS`：版块名称重复。
- `OLD_PASSWORD_ERROR`：旧密码错误。
- `UNAUTHORIZED`：缺少登录头或登录态无效。
- `FORBIDDEN`：无权限访问或操作他人资源。
- `NOT_FOUND`：资源不存在。
- `RESOURCE_UNAVAILABLE`：资源状态不可用。

### 1.3 Controller

在 `controller/` 下新增模块 Controller。

Controller 只做三件事：

- 绑定路径、请求参数和请求体。
- 通过 `CurrentUser.requireLogin(request)` 或 `CurrentUser.requireAdmin(request)` 读取登录态。
- 调用 Service，并用 `Result.success(...)` 返回。

示例结构：

```java
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public Result<PostDetailVO> create(@Valid @RequestBody PostCreateDTO dto,
                                       HttpServletRequest request) {
        CurrentUser currentUser = CurrentUser.requireLogin(request);
        return Result.success(postService.createPost(currentUser.getId(), dto));
    }
}
```

Controller 中不要直接访问 Mapper，不要手写响应 envelope。

### 1.4 后端验证

每完成一个模块后至少执行：

```text
cd backend
./mvnw test
```

如果需要真实联调，再启动后端并用前端页面或接口工具验证：

- 成功响应是否符合 `Result<T>`。
- 错误响应是否返回契约里的业务码。
- 时间格式是否为 `yyyy-MM-dd HH:mm:ss`。
- 前台是否过滤 `status=0` 数据。
- 管理端是否能看到并恢复 `status=0` 数据。

## 2. 前端接入一个模块

### 2.1 API 文件

在 `frontend/src/api/` 下为模块新增文件，例如：

```text
boards.js
posts.js
replies.js
admin.js
```

所有 API 文件统一使用 `request.js`：

```js
import request from './request'

export function getPosts(params) {
  return request.get('/posts', { params })
}
```

注意：

- API 文件路径写后端真实路径，例如 `/posts`，不要写完整域名。
- 不需要手动处理 `Result<T>`，`request.js` 已经返回 `result.data`。
- 不需要手动加登录头，`request.js` 会从 `localStorage.currentUser` 注入。

### 2.2 页面或 Store 改造

把页面中直接 import mock 的地方替换成 API 调用。

常见改法：

- 列表页：`ref([])` 保存数据，`onMounted` 或 `watch` 中调用接口。
- 详情页：根据 route param 调用详情接口。
- 表单页：提交时 `await` API，成功后跳转或刷新列表。
- 管理页：筛选条件变化时重新请求，分页仍保留在前端本地。

如果模块有共享状态，例如登录用户，放在 Pinia store；普通列表数据可以直接放在页面组件中。

### 2.3 异步错误处理

所有提交函数改成 `async`：

```js
async function submitForm() {
  errorMessage.value = ''

  try {
    await createPost(form)
    router.push('/posts')
  } catch (error) {
    errorMessage.value = error.message || '操作失败'
  }
}
```

不要再把 API 当同步函数调用，否则页面会在请求完成前跳转或显示旧数据。

### 2.4 前端验证

每完成一个模块接入后至少执行：

```text
cd frontend
npm run build
```

如果要做联调，再手动检查：

- 登录态接口是否自动带上 `X-User-Id` 和 `X-User-Role`。
- 页面是否仍引用旧 mock 数据。
- 表单错误信息是否能显示后端 `message`。
- 成功后页面是否刷新或跳转到正确位置。

## 3. 推荐模块顺序

已经完成基础设施和 User 模块后，建议顺序如下：

1. 公开版块模块：`GET /boards`、`GET /boards/{id}`。
2. 公开帖子模块：`GET /posts`、`GET /posts/{id}`、`POST /posts`、`DELETE /posts/{id}`。
3. 回复模块：`GET /posts/{postId}/replies`、`POST /posts/{postId}/replies`、`DELETE /replies/{id}`。
4. 管理端版块：`GET/POST/PUT/DELETE /admin/boards`。
5. 管理端帖子和回复状态：`/admin/posts`、`/admin/replies`。
6. 管理端统计：`/admin/dashboard/stats`。

每个模块完成后先跑构建，再做最小联调，不要等所有模块写完才一起排错。
