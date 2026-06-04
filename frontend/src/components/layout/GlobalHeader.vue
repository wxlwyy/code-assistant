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

    <!-- 当前会话标题和模式（这里暂时写死，后续可以根据当前会话动态展示） -->
    <!-- 💡 场景 A：已经有对应的历史会话了 -->
    <div v-if="currentSession" class="flex flex-col items-center justify-center leading-tight mt-1">
      <!-- 主标题：显示会话的第一句话，加个 max-w 防止太长撑爆，truncate 自动加省略号 -->
      <span class="text-sm font-bold text-gray-800 truncate max-w-md">
        {{ currentSession.title }}
      </span>

      <!-- 副标题：DeepSeek 同款的模式展示 -->
      <span class="text-[11px] text-gray-500 mt-0.5 flex items-center">
        <span class="mr-1">{{ currentSession.agentType === 'REASONING' ? '🤖' : '⚡️' }}</span>
        {{ currentSession.agentType === 'REASONING' ? '深度推理模式' : '标准快速模式' }}
      </span>
    </div>

    <!-- 💡 场景 B：正在新建对话 -->
    <!--      <div v-else class="flex flex-col items-center justify-center">
            <span class="text-sm font-bold text-gray-800">新对话</span>
            <span class="text-[11px] text-gray-400 mt-0.5">请在下方输入以开始</span>
          </div>-->
  </div>

  <!-- 右侧：放置一些快速操作按钮（比如清除会话、或者设置按钮） -->
  <div class="flex items-center space-x-2">
    <!-- 临时放一个图标 -->
    <button class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-500 cursor-pointer">
      <el-icon :size="18">
        <Setting />
      </el-icon>
    </button>
  </div>

</template>

<script setup lang="ts">
// 定义向父组件发送的事件
import { useRoute } from 'vue-router'
import { useChatStore } from '@/stores/chat.ts'
import { computed } from 'vue'

const route = useRoute()
const chatStore = useChatStore()
const emit = defineEmits<{
  (e: 'toggle-sidebar'): void  // void表示表示事件函数没返回值也不带参数
}>()

// 点击按钮时，通知父组件
const emitToggle = () => {
  emit('toggle-sidebar')
}

// 核心逻辑：找出当前正在聊天的会话对象
const currentSession = computed(() => {
  // 如果 URL 里没有 id，说明是新建对话，返回 null
  if (!route.params.id) return null

  // 去侧边栏的会话列表里，找到 id 匹配的那一条ChatSessionVO对象
  return chatStore.sessionList.find(s => s.id === route.params.id)  // find是根据后面的判断返回数组里面的一个参数而不是布尔值。
})
</script>
