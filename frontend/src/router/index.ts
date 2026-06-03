import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user.ts'

const routes: RouteRecordRaw[] = [
  // 1. 登录页：全屏展示，不走任何 Layout 外壳
  {
    path: '/login',
    name: 'LoginPage',
    component: () => import('../pages/LoginPage.vue') // 懒加载引入
  },
  // 2. 注册页
  {
    path: '/register',
    name: 'RegisterPage',
    component: () => import('../pages/RegisterPage.vue')
  },
  // 2. 核心业务页：全部嵌套在 BasicLayout 外壳中
  {
    path: '/',
    component: () => import('../layouts/BasicLayout.vue'), //  父路由指向大盒子
    /*redirect: '/chat', // 默认重定向到 /chat 路径
    children: [
      {
        // 动态路由设计：:id? 带有问号代表参数是可选的。
        // 访问 /chat ➡ 代表“新建对话”（欢迎页状态）
        // 访问 /chat/123-abc ➡ 代表“正在进行特定的历史会话”
        path: 'chat/:id?',
        name: 'ChatWorkspace',
        component: () => import('../pages/ChatWorkspace.vue') // 子路由组件会被渲染在 BasicLayout 的 router-view 中
      }
    ]*/
  },

  // 3. 兜底路由：如果输入了不存在的地址，直接重定向回首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  // 使用标准的 HTML5 History 路由模式 [3]
  history: createWebHistory(),
  routes
})

// 💡 核心：全局前置路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  // 1. 如果访问的是【登录页】或【注册页】（白名单）
  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      // 如果已经有 token 了，还访问登录页，直接送他去首页
      return next('/')
    }
    return next() // 没 token，正常放行让他去登录
  }

  // 2. 如果访问的是其他页面（比如 /chat 或 / ），必须校验 Token
  if (!token) {
    return next('/login') // 没 token，直接去登录页，注意next('/login')也会触发路由守卫
  }

  // 3. 有 token，但内存里还没加载用户信息（比如刚刷新了网页）
  if (!userStore.loginUser.id) {
    // 强制等待拉取最新用户信息
    await userStore.fetchLoginUser()

    // 如果拉取完还是没 ID（说明 token 伪造或过期了，并且在 fetchLoginUser 里清空 token）
    if (!userStore.loginUser.id) {
      return next('/login')
    }
  }

  // 4. 身份验证全部通过，正常放行！
  next()
})


export default router

/*
1. 导入目录时默认找 index 文件
是的，完全正确。 当你写 import router from './router' 时，'./router' 是一个目录路径，没有指定具体文件名。此时，Node.js 和 Vite
的模块解析机制会自动寻找该目录下的 index.ts（或 index.js） 文件。这正是为什么 import router from './router' 能正确导入 src/router/index.ts 的默认导出。

2. routes: RouteRecordRaw[] 的写法
你看到的 : RouteRecordRaw[] 是 TypeScript 的类型注解。它告诉编译器：“routes 这个变量是一个数组，数组里的每一项都必须符合 RouteRecordRaw 类型”。
RouteRecordRaw 是 Vue Router 提供的一个类型，用来约束路由配置对象的格式（例如必须包含 path，可选 component、redirect 等）。这样，当你写路由配置时，
IDE 能给出自动补全和错误提示。

类比 Java：就像 List<RouteRecordRaw> routes = new ArrayList<>() 中的泛型，确保集合里只能放特定类型的对象。

3. path: '/' 的作用
完全正确。 '/' 代表网站的根路径。当用户在浏览器地址栏输入 http://localhost:5173/（后面没有其他路径）时，匹配的就是这个路由。
我们利用这个路由进行重定向（redirect: '/chat'），让用户自动跳转到聊天页面。

一句话先讲核心：
Pinia、普通 ref/变量 都存在「浏览器运行时内存」里，刷新页面 = 整个页面进程重启，内存直接清空；而 localStorage 存在「浏览器磁盘持久化存储」，刷新不会丢。

下面拆开给你讲明白，结合你项目场景。



一、先分清两种存储（最关键）

1. 内存存储（一刷新就没）

包括：

- Vue 组件里的 ref / reactive 变量
- Pinia / Vuex 状态
- 普通 JS 全局变量、临时变量

原理：
浏览器每打开一个网页，都会单独开一个渲染进程，页面所有运行数据都放在这个进程的运行内存中。
👉 刷新页面 = 杀死当前进程 → 重新启动进程 → 内存全部重置清空。

所以你看到：
刷新后 userStore.loginUser 变成空、页面变量全部还原，就是这个原因。

2. 持久化存储（刷新、关标签都还在）

包括：

- localStorage
- sessionStorage（关标签页才清空）
- Cookie

原理：
它们是存在浏览器本地磁盘上的文件/数据库，不属于页面运行内存。
只要你不手动删除、不清缓存，刷新页面完全不受影响。

这也就是为什么：
刷新后 Pinia 空了，但 localStorage 里的 token 还在。

二、结合你的登录流程，串一遍完整过程

1.登录成功
- 接口返回 token → 存入 localStorage（磁盘，持久）
- 用户信息存入 Pinia（内存，临时）
2.用户刷新页面
1.页面进程重启 → Pinia 内存清空，用户信息没了
2.localStorage 不受影响，token 还在
3.路由守卫触发：发现有 token，但 Pinia 里没有用户信息
4.自动调用 fetchLoginUser() 重新请求后端
5.后端校验 token 有效，返回用户信息 → 重新写入 Pinia
6.页面恢复登录状态

这也是你之前路由守卫里那段代码存在的意义：专门用来「刷新后恢复状态」。


三、延伸两个常见疑问

1. 那能不能让 Pinia 刷新也不丢？

可以，用 Pinia 持久化插件（pinia-plugin-persistedstate），它底层原理：
把 Pinia 数据自动同步到 localStorage。
但登录场景不建议无脑全持久：

- 用户信息可以存，但 token 本来就单独存在 localStorage；
- 而且 token 过期后，持久化的用户信息和真实状态不一致，反而容易出 bug。
所以你现在的方案（只存 token，刷新重拉用户信息）是最稳妥的标准方案。

2. sessionStorage 和 localStorage 区别？

- localStorage：永久保存，刷新、关闭标签、重启浏览器都还在，手动删除才消失
- sessionStorage：仅当前标签页有效，关闭当前标签 / 刷新页面就清空

你存 token 用 localStorage 是正确选择。

四、极简总结

1.变量、Pinia → 跑在浏览器内存里，刷新页面进程重启 → 全部清空。
2.localStorage → 存在浏览器磁盘，和页面进程无关，刷新不丢失。
3.你项目的设计逻辑：
只把 token 放持久化存储，刷新后靠路由守卫 + 接口重新拉取用户信息回填 Pinia，是行业通用、最安全的写法。
* */
