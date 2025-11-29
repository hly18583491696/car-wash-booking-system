## 问题概述
- 编译错误指向 `frontend/src/components/Layout/Navbar.vue:85`，元素使用 `v-else`。
- 对应的 `v-if` 在 `frontend/src/components/Layout/Navbar.vue:52`。两者之间存在关闭标签与注释（如 `</transition>`、`<!-- 登录按钮 -->`、新的 `<transition>`），打断了邻接关系。
- Vue 规则：`v-else` 必须紧跟前一个同层级的 `v-if`/`v-else-if`，中间不可出现任何节点（包含注释/文本）。

## 修复方案A（推荐）
- 目标：不改变交互与动画，仅调整结构以满足邻接规则。
- 调整步骤：
  1) 将“已登录用户菜单”块包裹到 `<template v-if="isLoggedIn">`，保留内部的 `<transition name="fade-slide">` 和 `user-menu` 内容。
  2) 将“未登录按钮”块包裹到 `<template v-else>`，保留内部的 `<transition name="fade-slide">` 和 `auth-buttons` 内容。
  3) 确保这两个 `<template>` 紧邻摆放，不插入注释或其他节点；将 `<!-- 登录按钮 -->` 注释移至两个分支之外或置于分支内部。
- 参考结构示例：
```
<template v-if="isLoggedIn">
  <transition name="fade-slide">
    <div class="user-menu">...</div>
  </transition>
</template>
<template v-else>
  <transition name="fade-slide">
    <div class="auth-buttons">...</div>
  </transition>
</template>
```

## 备选方案B
- 将 `v-if`/`v-else` 直接放到并列的外层 `div`：
  1) `<div class="user-menu" v-if="isLoggedIn"> <transition>...</transition> </div>`
  2) `<div class="auth-buttons" v-else> <transition>...</transition> </div>`
- 删除或移动二者之间的注释与多余标签，保证两者紧邻。

## 影响范围
- 仅影响导航栏模板分支结构；移动端图标切换 `Menu v-if / Close v-else`（约 `frontend/src/components/Layout/Navbar.vue:98-100`）已正确，无需修改。

## 验证步骤
- 本地编译应不再出现“v-else 未紧跟 v-if”相关错误。
- 切换登录/未登录状态，确认按钮与用户菜单按预期显示，`fade-slide` 动画正常。
- 前端运行检查导航栏在不同视口下的行为。

## 文档
- 完成功能后，按项目约定将变更与结果更新至计划书中文档。