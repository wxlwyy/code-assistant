<template>
  <div class="h-screen w-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="max-w-md w-full bg-white rounded-3xl shadow-xl p-8 border border-gray-150">

      <div class="text-center mb-8">
        <span class="text-4xl"></span>
        <h2 class="text-2xl font-bold text-gray-900 mt-3">创建您的账号</h2>
        <p class="text-sm text-gray-500 mt-1">欢迎加入 AI 超级智能体平台</p>
      </div>

      <div class="space-y-5">
        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1.5 uppercase">用户账号</label>
          <input v-model="form.userAccount" type="text" placeholder="请输入账号" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-500 outline-none" />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1.5 uppercase">密码</label>
          <input v-model="form.userPassword" type="password" placeholder="请输入至少 6 位密码" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-500 outline-none" />
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1.5 uppercase">确认密码</label>
          <input v-model="form.checkPassword" type="password" placeholder="请再次输入密码" @keydown.enter="handleRegister" class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-100 focus:border-blue-500 outline-none" />
        </div>

        <button @click="handleRegister" :disabled="loading" class="w-full py-3.5 bg-gray-900 hover:bg-gray-800 text-white rounded-xl text-sm font-medium transition-colors shadow-sm disabled:bg-gray-400 cursor-pointer">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>
      </div>

      <div class="mt-6 text-center text-sm text-gray-500">
        已经有账号？
        <button @click="router.push('/login')" class="text-blue-600 font-medium hover:underline ml-1 cursor-pointer">
          立即登录
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userRegister } from '@/api/userController' //  根据你生成的 API 路径调整

const router = useRouter()
const loading = ref(false)

const form = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const handleRegister = async () => {
  if (!form.userAccount || !form.userPassword || !form.checkPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.userPassword !== form.checkPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const res = await userRegister(form)
    // 根据拦截器的规范，如果不报错就说明注册成功了
    if (res.data) {
      ElMessage.success('注册成功，请登录')
      // 注册成功后，直接跳转到登录页
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>
