import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'

import App from './App.vue'
import router from './router'

// 1. 引入所有的 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建 Vue 应用实例，App 是从 App.vue 文件导入的一个组件对象。createApp 是 Vue 提供的 API，接收一个根组件，返回一个应用实例。
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 2. 循环遍历并全局注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 挂载到 id为 app 的容器，把整个 Vue 应用渲染到 index.html 里 id="app" 的那个 div 中。
app.mount('#app')

/**
 * import 后 from 的路径是指什么？
 * from 后面跟的字符串，只有两种可能：
 *
 * 情况 A：包名（不带路径符号）
 * 比如 'vue'、'pinia'、'element-plus'、'@element-plus/icons-vue'。这些是 npm 包名。
 *
 * 如何解析：Vite 会去 node_modules 目录下找到同名文件夹，然后根据该包的 package.json 中的 module 或 main 字段，找到真正的入口文件。
 *
 * 例子：import { createApp } from 'vue' → 找到 node_modules/vue/ → 读取其 package.json → 加载 dist/vue.runtime.esm-bundler.js（简化版）。这里导出了 createApp 这个方法，所以我们用 { } 精确提取。
 *
 * 情况 B：相对路径或绝对路径（以 ./、../、@/ 等开头）
 * 比如 './App.vue'、'./router'、'@/utils/request'。这些是文件系统中的文件或文件夹。
 *
 * 如何解析：
 *
 * ./App.vue'：直接找当前目录下的 App.vue 文件。
 *
 * './router'：没有后缀名，说明这是一个目录。Node.js 和 Vite 会自动寻找该目录下的 index.ts（或 index.js）文件。这就是为什么你写 import router from './router' 能正确导入 src/router/index.ts 的默认导出。
 *
 * '@/utils/request'：@ 是 Vite 配置的别名，指向 src/。所以等价于 './src/utils/request'。
 *
 *
 * 3. 导入进来的东西，到底是什么类型？
 * Java 里导入的都是类；前端 ES Module 里，导入的可以是任何 JavaScript 值：对象、函数、类、字符串……需要根据导出方的写法判断。
 *
 * 我们从你的代码里看几个典型例子：
 *
 * 导入语句	                      导入的是什么	      怎么用	            为什么
 * import { createApp } from 'vue'	一个函数	createApp(App)	Vue 包导出了这个函数，我们用解构取出。
 * import ElementPlus from 'element-plus'	一个对象（插件）	app.use(ElementPlus)	ElementPlus 包默认导出一个包含 install 方法的对象，正是 Vue 插件标准。
 * import App from './App.vue'	一个组件选项对象	作为根组件	.vue 文件的 <script setup> 会编译出一个对象，以 export default 形式导出。导入时我们可以随意命名，这里命名为 App。
 * import router from './router'	一个路由实例对象	app.use(router)	router/index.ts 里执行了 createRouter(...)，并 export default router。所以导入的就是那个创建好的 router 实例。
 * import * as Icons from '@element-plus/icons-vue'	一个对象，键是图标名，值是组件	app.component(key, component)	把该包所有命名导出合并成一个对象，然后遍历注册为全局组件。
 * 关键规则：
 *
 * 默认导出（export default）：导入时不用大括号，且名字可以随便起。比如 export default router; → import myRouter from './router'。
 *
 * 命名导出（export const xxx 或 export { xxx }）：导入时必须用大括号，且名字必须原样匹配。比如 Vue 导出 createApp，你必须写 import { createApp } from 'vue'。想改名可以用 as：import { createApp as myCreate } from 'vue'。
 *
 */
