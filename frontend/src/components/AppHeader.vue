<template>
  <header class="app-header">
    <div class="app-header__inner content-width">
      <RouterLink class="app-header__brand" to="/">Enterprise BBS</RouterLink>

      <div class="app-header__search search-box">
        <input
          class="search-box__input"
          type="search"
          placeholder="搜索帖子、版块或关键词"
          aria-label="搜索帖子、版块或关键词"
        />
      </div>

      <div class="app-header__actions">
        <button class="post-button" type="button">发帖</button>
        <button class="avatar-button" type="button" aria-label="用户个人页入口">
          <span class="avatar-button__glyph" aria-hidden="true"></span>
        </button>
      </div>
    </div>
  </header>
</template>
