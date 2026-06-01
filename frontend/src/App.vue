<script setup lang="ts">
</script>

<template>
  <router-view />
</template>

<!--
1. 谁决定页面渲染什么？
结论：浏览器地址栏的 URL 说了算。<router-view /> 只是执行者，不是决策者。

整个流程分三步：

第一步：用户在浏览器地址栏输入 URL，或点击链接改变地址。

比如用户访问 http://localhost:5173/chat。这个地址会触发 Vue Router 的匹配机制。

第二步：Vue Router 根据当前 URL，去路由表里找匹配的路由配置。

路由表就是你在 router/index.ts 里定义的 routes 数组：

typescript
const routes = [
  { path: '/', redirect: '/chat' },
  { path: '/chat', component: () => import('@/layouts/BasicLayout.vue') }
]
当前 URL 是 /chat，匹配到第二条。Vue Router 确定：应该渲染 BasicLayout.vue 这个组件。

第三步：<router-view /> 把匹配到的组件渲染出来。

App.vue 里只有一行 <router-view />。Vue Router 告诉它：“当前匹配到的组件是 BasicLayout”。于是 <router-view /> 就把 BasicLayout.vue 的内容渲染在这里。

如果用户点击了侧边栏的“新建对话”，地址变成 /new-chat，那么 <router-view /> 就会换成渲染 NewChatPage.vue。

2. <router-view /> 和浏览器地址栏的关系
浏览器地址栏：是“原因”。地址变了，页面才会变。

<router-view />：是“结果”。它只负责把匹配到的组件显示出来。

路由表：是“翻译官”。把 URL 翻译成具体的组件。

类比后端 Java：就像 @RequestMapping("/chat") 和 return "chatPage" 的关系。浏览器访问 /chat，Spring MVC 根据 @RequestMapping 找到对应方法，返回视图名。
这里 Vue Router 根据 URL 找到对应组件，<router-view /> 负责渲染。

3. 关于你说的“动态路由”
Vue Router 支持两种路由模式：

静态路由：像我们写的 path: '/chat'，就是固定的。

动态路由：比如 path: '/user/:id'，可以匹配 /user/123、/user/456 等。

我们目前没用到动态路由。<router-view /> 只是根据当前 URL 和路由表做匹配，无论路由是静态还是动态，原理都一样。

4. 你的 App.vue 和路由的完整流程
浏览器访问 http://localhost:5173/chat

main.ts 启动 Vue 应用，挂载到 #app

App.vue 渲染，里面只有一个 <router-view />

Vue Router 看到当前 URL 是 /chat，查路由表，发现匹配 BasicLayout.vue

<router-view /> 把 BasicLayout.vue 渲染出来

BasicLayout.vue 里有自己的 <router-view />（如果你配置了子路由），继续匹配子组件；如果没配置子路由，就只显示布局本身
-->
