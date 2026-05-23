<template>

    <!-- 左侧：折叠按钮 + 标题 -->
    <div class="flex items-center space-x-3">
      <!-- 折叠/展开按钮 -->
      <button
          @click="emitToggle"
          class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-gray-800 transition-colors cursor-pointer"
      >
        <!-- 使用 Element Plus 的图标组件 -->
        <el-icon :size="20">
          <Fold />
        </el-icon>
      </button>

      <!-- 动态标题（这里暂时写死，后续可以根据当前会话动态展示） -->
      <span class="font-medium text-gray-800">新建对话</span>
    </div>

    <!-- 右侧：放置一些快速操作按钮（比如清除会话、或者设置按钮） -->
    <div class="flex items-center space-x-2">
      <!-- 临时放一个图标 -->
      <button class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 cursor-pointer">
        <el-icon :size="18"><Setting /></el-icon>
      </button>
    </div>

</template>

<script setup lang="ts">
// 定义向父组件发送的事件
const emit = defineEmits<{
  (e: 'toggle-sidebar'): void
}>()

// 点击按钮时，通知父组件
const emitToggle = () => {
  emit('toggle-sidebar')
}
</script>