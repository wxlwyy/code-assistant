<template>

    <!-- 侧边栏内部容器，固定 w-64 防止宽度缩小时内部元素变形 -->
    <div class="w-64 h-full flex flex-col p-4">

      <!-- Logo 区域 -->
      <div class="text-xl font-bold tracking-wider text-gray-900 mb-4 px-2 flex items-center space-x-2">
        <span class="text-2xl">🧠</span>
        <span>AI 超级智能体</span>
      </div>

      <!-- 新建对话按钮 -->
      <button class="w-full py-2.5 px-4 mb-4 bg-gray-900 hover:bg-gray-800 text-white rounded-xl font-medium flex items-center justify-center space-x-2 transition-colors cursor-pointer">
        <el-icon><Plus /></el-icon>
        <span>新建对话</span>
      </button>

      <!-- 中间：历史会话列表区域（支持滚动） -->
      <div class="flex-1 overflow-y-auto space-y-1.5 py-2">
        <!-- 暂时写一个假的列表展示外观 -->
        <div class="px-3 py-2 rounded-lg bg-gray-50 text-gray-700 text-sm font-medium cursor-pointer">
          💬 恋爱聊天会话 1
        </div>
        <div class="px-3 py-2 rounded-lg hover:bg-gray-50 text-gray-600 hover:text-gray-800 text-sm cursor-pointer transition-colors">
          💬 Manus 智能体搜索
        </div>
      </div>

      <!-- 底部：个人中心/设置 -->
      <div class="border-t border-gray-100 pt-4 px-2 flex items-center space-x-3">
        <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-700 truncate">极客用户</p>
        </div>
      </div>

    </div>
</template>

<script setup lang="ts">
// 使用 defineProps 接收父组件传递过来的状态
defineProps<{
  isCollapsed: boolean
}>()
</script>