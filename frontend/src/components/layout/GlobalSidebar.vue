<template>
  <aside
    class="bg-white border-r border-gray-150 flex flex-col justify-between overflow-hidden transition-all duration-300"
    :class="isCollapsed ? 'w-0' : 'w-64'"
  >
    <div class="w-64 h-full flex flex-col p-4">

      <!-- 1. Logo 区域 -->
      <div class="text-xl font-bold tracking-wider text-gray-900 mb-4 px-2 flex items-center space-x-2">
        <span class="text-2xl"></span>
        <span>AI 超级智能体</span>
      </div>

      <!-- 2. 新建对话按钮 -->
      <button class="w-full py-2.5 px-4 mb-4 bg-gray-900 hover:bg-gray-800 text-white rounded-xl font-medium flex items-center justify-center space-x-2 transition-colors cursor-pointer shadow-sm">
        <el-icon><Plus /></el-icon>
        <span>新建对话</span>
      </button>

      <!-- 3. 历史会话列表区域（目前还是假数据，下一关我们就来通电！） -->
      <div class="flex-1 overflow-y-auto space-y-1.5 py-2">
        <div class="px-3 py-2 rounded-lg bg-gray-50 text-gray-700 text-sm font-medium cursor-pointer">
          💬 恋爱聊天会话 1
        </div>
        <div class="px-3 py-2 rounded-lg hover:bg-gray-50 text-gray-600 hover:text-gray-800 text-sm cursor-pointer transition-colors">
          💬 Manus 智能体搜索
        </div>
      </div>

      <!-- 4. 💡 底部：个人中心/设置（使用 Element Plus 的 Dropdown 组件包裹） -->
      <div class="border-t border-gray-100 pt-4 mt-2">
        <!-- el-dropdown 触发器，设为 click 点击触发弹出菜单内容 -->
        <el-dropdown trigger="click" placement="top-start" class="w-full">
          <!-- 触发区域（用来被点击或交互）：也就是用户卡片，用的默认插槽（没有名字） -->
          <div class="w-full flex items-center space-x-3 px-2 py-2 hover:bg-gray-50 rounded-xl cursor-pointer transition-colors">
            <!-- 左侧头像：如果有真实头像就用真实的，没有就用默认的可爱默认图 -->
            <!--属性前的冒号或v-bind是把引号里的内容当js表达式，值会随着变量变化，这里的36是因为数字或布尔类型放在引号里容易被当成字符串，要想传真实数据类型，必须加冒号-->
            <el-avatar
              :size="36"
              :src="userStore.loginUser.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            />
            <div class="flex-1 min-w-0 text-left">
              <!-- 右上侧昵称：取 userName，如果没有则显示 userAccount -->
              <p class="text-sm font-medium text-gray-700 truncate">
                {{ userStore.loginUser.userName || userStore.loginUser.userAccount }}
              </p>
              <!-- 右下侧：角色如果是管理员，亮出一个炫酷的 Badge -->
              <p class="text-xs text-gray-400 mt-0.5 flex items-center">
                <span v-if="userStore.loginUser.userRole === 'admin'" class="bg-amber-100 text-amber-600 px-1.5 py-0.5 rounded text-[10px] font-bold mr-1">PRO</span>
                <span>{{ userStore.loginUser.userRole === 'admin' ? '超级管理员' : '普通用户' }}</span>
              </p>
            </div>
            <!-- 最右侧小点点图标，暗示可以点击 -->
            <el-icon class="text-gray-400"><MoreFilled /></el-icon>
          </div>

          <!-- 弹出的菜单内容，这里用了具名插槽，template #dropdown 是简写，template v-solt:dropdown是完整写法-->
          <template #dropdown>
            <el-dropdown-menu class="w-56">
              <el-dropdown-item>
                <el-icon><User /></el-icon> 个人信息
              </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Setting /></el-icon> 系统设置
              </el-dropdown-item>
              <!--divided是给菜单选项加一条分割线-->
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

// 接收父组件传递过来的侧边栏折叠状态
defineProps<{
  isCollapsed: boolean
}>()

const router = useRouter()
const userStore = useUserStore()

/**
 * 处理退出登录逻辑（企业级防误触做法）
 */
const handleLogout = () => {
  // 弹出二次确认框，防止用户手滑点错
  ElMessageBox.confirm(
    '确认要退出当前登录账号吗？',
    '提示',
    {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 1. 调用 Pinia Store 里的清理方法，删掉本地 Token 和状态
    userStore.clearLoginState()

    // 2. 提示成功
    ElMessage.success('已安全退出')

    // 3. 强制页面跳转到登录页
    router.push('/login')
  }).catch(() => {
    // 用户点击取消，什么都不做
  })
}
</script>



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
