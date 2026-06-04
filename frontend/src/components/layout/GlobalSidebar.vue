<template>
  <aside
    class="bg-white border-r border-gray-150 flex flex-col justify-between overflow-hidden transition-all duration-300"
    :class="isCollapsed ? 'w-0' : 'w-64'"
  >
    <div class="w-64 h-full flex flex-col p-4">

      <!-- 1. Logo 区域 -->
      <div class="text-xl font-bold tracking-wider text-gray-900 mb-6 px-2 flex items-center space-x-2">
        <span class="text-2xl"></span>
        <span>AI 超级智能体</span>
      </div>

      <!-- 2. 新建对话按钮 -->
      <!-- 点击时路由跳转到 /chat (不带 id)，就是新建模式 -->
      <button
        @click="router.push('/chat')"
        class="w-full py-2.5 px-4 mb-4 bg-gray-900 hover:bg-gray-800 text-white rounded-xl font-medium flex items-center justify-center space-x-2 transition-colors cursor-pointer shadow-sm"
      >
        <el-icon><Plus /></el-icon>
        <span>新建对话</span>
      </button>

      <!-- 3. 历史会话列表区域 (引入真实数据) -->
      <div class="flex-1 overflow-y-auto space-y-1.5 py-2 pr-1 custom-scrollbar">
        <!-- 如果正在加载，给个简单的提示 -->
        <div v-if="chatStore.isLoadingSessions" class="text-center text-xs text-gray-400 py-4">
          加载中...
        </div>

        <!-- 没有历史记录时的空状态 -->
        <div v-else-if="chatStore.sessionList.length === 0" class="text-center text-xs text-gray-400 py-4">
          暂无历史对话
        </div>

        <!-- 渲染真实的列表数据，只要浏览器的的地址发生变化，就会走路由守卫，守卫放行之后页面才会切换，route.params才能提取地址栏的动态参数 -->
        <div
          v-for="item in chatStore.sessionList"
          :key="item.id"
          @click="router.push(`/chat/${item.id}`)"
          class="group relative px-3 py-2.5 rounded-lg text-sm font-medium cursor-pointer transition-colors flex items-center justify-between"
          :class="$route.params.id === item.id ? 'bg-gray-100 text-gray-900' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-800'"
        >
          <!-- 标题（带截断） -->
          <div class="flex items-center space-x-2 truncate">
<!--            <span class="text-base">{{ item.agentType === 'REASONING' ? '🤖' : '❤️' }}</span>-->
            <span class="truncate">{{ item.title }}</span>
          </div>

          <!-- 隐藏的删除按钮：鼠标悬浮时(group-hover)才会显示，点击时加上 .stop 阻止事件冒泡触发外层的跳转到详情页面 -->
          <button
            @click.stop="handleDeleteSession(item.id)"
            class="opacity-0 group-hover:opacity-100 text-gray-400 hover:text-red-500 transition-opacity p-1 rounded-md"
          >
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 4. 💡 底部个人中心 (加上 mt-auto 强制死死钉在底部！) -->
      <div class="mt-auto border-t border-gray-100 pt-4 pb-2">
        <el-dropdown trigger="click" placement="top-start" class="w-full">
          <div class="w-full flex items-center space-x-3 px-2 py-2 hover:bg-gray-50 rounded-xl cursor-pointer transition-colors">
            <el-avatar :size="36" :src="userStore.loginUser.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <div class="flex-1 min-w-0 text-left">
              <p class="text-sm font-medium text-gray-700 truncate">
                {{ userStore.loginUser.userName || userStore.loginUser.userAccount }}
              </p>
              <p class="text-xs text-gray-400 mt-0.5">
                <span v-if="userStore.loginUser.userRole === 'admin'" class="bg-amber-100 text-amber-600 px-1.5 py-0.5 rounded text-[10px] font-bold mr-1">PRO</span>
                {{ userStore.loginUser.userRole === 'admin' ? '超级管理员' : '普通用户' }}
              </p>
            </div>
            <el-icon class="text-gray-400"><MoreFilled /></el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu class="w-56">
              <el-dropdown-item><el-icon><User /></el-icon> 个人信息</el-dropdown-item>
              <el-dropdown-item><el-icon><Setting /></el-icon> 系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout" class="text-red-500 hover:text-red-600 hover:bg-red-50">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

    </div>
  </aside>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { ElMessageBox, ElMessage } from 'element-plus'

defineProps<{ isCollapsed: boolean }>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()

// 页面挂载时，去拉取真实的会话列表
onMounted(() => {
  chatStore.fetchSessionList()
})

// 删除会话的防误触拦截
const handleDeleteSession = (id: string) => {
  ElMessageBox.confirm('确定要删除这条对话历史吗？', '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(() => {
    chatStore.removeSession(id)
    // 如果当前删掉的正是你正在聊天的会话，就把页面踢回“新建对话”模式
    if (route.params.id === id) {
      router.push('/chat')
    }
  }).catch(() => {})
}

// 退出登录逻辑
const handleLogout = () => {
  ElMessageBox.confirm('确认要退出当前登录账号吗？', '提示', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消',
  }).then(() => {
    userStore.clearLoginState()
    ElMessage.success('已安全退出')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
/* 给历史列表的滚动条做个美化，让它像 MacOS 一样好看 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 4px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
}
</style>



<!--
简化版的 el-dropdown 组件内部实现逻辑（伪代码）： Element Plus 内部的 el-dropdown 组件
<template>
  <div class="el-dropdown">
     1. 渲染默认插槽的内容（你的用户卡片）
    <div ref="triggerRef" @click="toggleMenu">
      <slot></slot>
    </div>

     2. 当菜单打开时，渲染 dropdown 插槽的内容
    <teleport to="body" v-if="isOpen">
      <div :style="menuPosition" class="el-dropdown-menu">
        <slot name="dropdown"></slot>
      </div>
    </teleport>
  </div>
</template>
- 你写的 <div>用户卡片</div> 会被渲染到「触发区域」

- 你写的 <template #dropdown>菜单内容</template> 会被渲染到「弹出菜单」里

- 组件内部通过 trigger="click" 来控制什么时候显示/隐藏菜单
-->
