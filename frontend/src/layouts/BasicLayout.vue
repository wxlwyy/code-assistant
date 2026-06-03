<template>
  <!-- 外层大容器：全高全宽，溢出隐藏 -->
  <div class="flex h-screen w-screen overflow-hidden bg-gray-50 text-gray-800">

    <!-- 1. 引入全局侧边栏，通过 Props 传入折叠状态 -->
    <!-- 侧边栏：
       w-64 表示展开宽度（是css框架写法），w-0 表示完全收起（或者你可以设计成窄侧边栏 w-16，这里我们做标准的平滑收缩）
       transition-all duration-300 让展开收缩有 0.3 秒的平滑过渡动画
  -->
    <aside
        class="bg-white border-r border-gray-150 flex flex-col justify-between overflow-hidden transition-all duration-300"
        :class="isCollapsed ? 'w-0' : 'w-64'"
    >
      <GlobalSidebar :is-collapsed="isCollapsed"/>
    </aside>

    <!-- 2. 右侧主体工作区 -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- 2.1 引入全局顶部栏，监听其发送的折叠事件，触发 toggleSidebar 函数 -->
      <!-- 顶部栏：固定高度 14 (56px)，白底，下边框 -->
      <header class="h-14 border-b border-gray-150 bg-white flex items-center justify-between px-4">
        <GlobalHeader @toggle-sidebar="toggleSidebar"/>
      </header>

      <!-- 2.2 核心工作区（目前我们在这个大盒子里渲染具体的聊天业务） -->
      <main class="flex-1 overflow-hidden relative bg-gray-50 flex flex-col">
        <!--
          临时放一个假的“聊天欢迎页”作为占位，
          等会儿我们会把它抽取到单独的业务组件或页面中！
        -->
        <div class="flex-1 flex flex-col items-center justify-center p-6">
          <div class="text-4xl mb-4">👋</div>
          <h2 class="text-2xl font-bold text-gray-800">今天想聊点什么？</h2>
          <p class="text-sm text-gray-500 mt-2">请在下方选择一个对话模式开始吧</p>
        </div>
      </main>
    </div>

  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
// 局部引入我们刚写好的局部组件
import GlobalSidebar from '../components/layout/GlobalSidebar.vue'
import GlobalHeader from '../components/layout/GlobalHeader.vue'

// 1. 定义侧边栏是否折叠的全局状态（默认不折叠 false）
const isCollapsed = ref(false)

// 2. 切换侧边栏状态的函数
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>
