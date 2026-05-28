# 前端 CSS 归档指南

本文档约定企业 BBS 前端样式文件的归档方式。目标是让新增页面和组件样式有固定位置，避免继续把所有规则堆进 `frontend/src/assets/main.css`。

## 1. 当前目录结构

全局样式入口仍然只有一个：

```text
frontend/src/assets/main.css
```

`main.css` 只负责按顺序导入拆分后的样式文件，不直接写具体选择器：

```css
@import './styles/base.css';
@import './styles/layout.css';

@import './styles/components/app-header.css';
@import './styles/components/overview-tabs.css';
@import './styles/components/board-badge.css';
@import './styles/components/board-card.css';
@import './styles/components/post-list-item.css';
@import './styles/components/post-table.css';
@import './styles/components/post-block.css';
@import './styles/components/reply-composer.css';

@import './styles/pages/auth.css';
@import './styles/pages/placeholder.css';
@import './styles/pages/boards.css';
@import './styles/pages/board-detail.css';
@import './styles/pages/posts.css';
@import './styles/pages/post-detail.css';
@import './styles/pages/profile.css';
@import './styles/pages/profile-edit.css';
```

样式目录：

```text
frontend/src/assets/styles/
  base.css
  layout.css
  components/
  pages/
```

## 2. 新样式写在哪里

| 样式类型 | 写入位置 | 判断标准 |
| --- | --- | --- |
| 浏览器基础样式 | `styles/base.css` | `*`、`:root`、`body`、原生 `button`、`input`、`textarea`、`a` 等基础元素规则 |
| 应用布局壳 | `styles/layout.css` | `.app-shell`、`.content-width`、`.page-shell` 等影响整体页面容器的规则 |
| 可复用组件 | `styles/components/*.css` | 对应 `src/components` 中的组件，或被多个页面共用的 UI 块 |
| 管理端表格与操作按钮组 | `styles/components/admin-table.css` | `AdminTable.vue`、`.admin-table__actions`、`.admin-table__action` 等跨管理页复用的表格和操作样式 |
| 单页面样式 | `styles/pages/*.css` | 只服务一个 route view 的页面结构和页面状态 |
| 管理端页面 | `styles/pages/admin/*.css` | 未来新增 `/admin/**` 页面后，按管理端页面继续拆分 |

优先从“使用范围”判断归档位置，而不是从视觉长相判断。

## 3. 组件与页面的边界

- 只在一个页面出现的 class，先放进对应 `pages/*.css`。
- 同一组样式被两个以上页面复用时，再移动到 `components/*.css`。
- 对应 `src/components/Foo.vue` 的样式，优先放进 `styles/components/foo.css`。
- 页面可以对组件做少量上下文修饰，例如 `.post-detail__header .board-badge`，这类规则放在页面 CSS。

## 4. 命名规则

继续使用当前的 BEM-ish 命名风格：

```text
.block
.block__element
.block--modifier
.block__element--modifier
```

建议：

- 新增一个页面时，先用页面根 class 作为 block，例如 `.profile-edit-page`。
- 新增一个组件时，使用组件名对应的 block，例如 `.post-table`。
- 避免使用过宽的全局选择器，例如 `.page h1`、`.card button`。
- 避免依赖 DOM 层级过深的选择器，优先通过明确 class 表达样式目标。

## 5. 响应式规则

响应式规则放在所属文件底部，不单独建立 `responsive.css`。

例如：

- `.board-card` 的移动端规则写在 `styles/components/board-card.css`。
- `.profile-edit-page` 的移动端规则写在 `styles/pages/profile-edit.css`。
- `.page-shell` 的移动端规则写在 `styles/layout.css`。

这样修改一个组件或页面时，可以在同一个文件里看到默认样式和响应式样式。

## 6. 禁止事项

- 不要把新的具体选择器继续写进 `frontend/src/assets/main.css`。
- 不要为了一个页面临时新增全局工具类，除非确认会被多个页面复用。
- 不要在页面 CSS 中覆盖不相关组件的内部细节。
- 不要把同一个 block 的默认样式和响应式样式拆到不同文件。
- 不要在没有必要时引入 scoped style、CSS Modules 或新的 CSS 方案。

## 7. 新增样式流程

1. 先确认样式服务的是基础元素、布局壳、组件还是页面。
2. 写入对应 CSS 文件；如果文件不存在，按目录规则新建。
3. 如果新建了 CSS 文件，在 `frontend/src/assets/main.css` 中补充 `@import`。
4. 保持导入顺序：base、layout、components、pages。
5. 运行前端构建，确认 CSS 导入链没有问题。
