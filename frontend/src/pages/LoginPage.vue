<template>
  <div class="h-screen w-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="max-w-md w-full bg-white rounded-3xl shadow-xl p-8 border border-gray-150">

      <div class="text-center mb-8">
        <span class="text-4xl"></span>
        <h2 class="text-2xl font-bold text-gray-900 mt-3">登录您的账号</h2>
        <p class="text-sm text-gray-500 mt-1">欢迎回来，请登录开始对话</p>
      </div>

      <div class="space-y-5">
        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1.5 uppercase">用户账号</label>
          <input v-model="form.userAccount" type="text" placeholder="请输入账号" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-500 outline-none" />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1.5 uppercase">密码</label>
          <input v-model="form.userPassword" type="password" placeholder="请输入密码" @keydown.enter="handleLogin" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-500 outline-none" />
        </div>

        <button @click="handleLogin" :disabled="loading" class="w-full py-3.5 bg-gray-900 hover:bg-gray-800 text-white rounded-xl text-sm font-medium transition-colors shadow-sm disabled:bg-gray-400 cursor-pointer">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>

      <div class="mt-6 text-center text-sm text-gray-500">
        还没有账号？
        <!-- 💡 编程式跳转到注册页 -->
        <button @click="router.push('/register')" class="text-blue-600 font-medium hover:underline ml-1 cursor-pointer">
          立即注册
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userLogin } from '@/api/userController' // 👈 根据你生成的 API 路径调整
import { useUserStore } from '@/stores/user'

const router = useRouter()
// 注意这里拿到的userStore是pinia包装后的对象（defineStore是pinia提供的核心方法用来定义一个全局仓库），包装后的对象里的ref属性的变量，使用时不用写.value
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  userAccount: '',
  userPassword: ''
})

const handleLogin = async () => {
  if (!form.userAccount || !form.userPassword) {
    ElMessage.warning('请填写完整的账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await userLogin(form)
    if (res.data) {
      ElMessage.success('登录成功')
      userStore.setToken(res.data.token as string)
      userStore.loginUser = res.data
      router.push('/') // 登录成功，跳转首页
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
</script>
