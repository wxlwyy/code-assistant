import { defineStore } from 'pinia'
import { ref } from 'vue'
// 引入你的 API 接口（注意路径替换为你实际自动生成 API 所在的路径）
import { getLoginUser } from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  // 1. State（状态）
  // 存放当前登录的脱敏用户信息 (对应后端的 LoginUserVO)
  const loginUser = ref<API.LoginUserVO>({  // 响应式存储当前登录用户的信息。初始值是一个默认的空对象，避免未登录时页面报错。
    id: '',
    userAccount: '',
    userName: '未登录',
    userAvatar: '',
    userRole: 'user',
  })

  // 2. Actions（动作/方法）

  /**
   * 记录登录凭证
   * 在登录页面调用接口成功后，把后端返回的 token 存到浏览器本地，供 request.ts 拦截器读取
   */
  const setToken = (token: string) => {
    localStorage.setItem('token', token)   // localStorage 在前端是一个键值对存储，同一个key只能存一个value，因此保证token永远只有一个
  }

  /**
   * 拉取当前登录用户信息
   * 每次刷新网页时调用，用本地的 token 去向后端换取最新用户信息
   */
  const fetchLoginUser = async () => {
    try {
      // 由于拦截器已经自动拆壳，并且拦截了所有的报错
      // 只要这行代码没报错，data 就 100% 是后端返回的 LoginUserVO 对象！
      const res  = await getLoginUser()

      if (res.data) {
        loginUser.value = res.data
      }
    } catch (error) {
      // 如果报错了（比如报了 40100 异常），会被 catch 捕捉，清理过期的token
      console.error('获取用户信息失败', error)
      clearLoginState()
    }
  }

  /**
   * 退出登录 / 清除状态
   */
  const clearLoginState = () => {
    localStorage.removeItem('token') // 删掉 token
    loginUser.value = {              // 恢复未登录的默认状态
      id: '',
      userAccount: '',
      userName: '未登录',
      userAvatar: '',
      userRole: 'user',
    }
  }

  // 3. 将状态和方法暴露出去，供整个项目的任意 .vue 页面使用
  return {
    loginUser,
    setToken,
    fetchLoginUser,
    clearLoginState
  }
})
