import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios';  // AxiosInstance,
import { ElMessage } from 'element-plus';

// 1. 创建 Axios 实例
/*const request: AxiosInstance = axios.create({
    // baseURL 会拼接在所有请求路径的前面。
    // 配置 Vite 的代理（Proxy）来转发到你的 Java 后端端口（如 8081）
    baseURL: '/api', // 路径以/开头，浏览器会自动请求当前服务器地址5173，通过配置文件转到8081，用来解决跨域问题（如果直接写8081浏览器就会认为是5173向8081发请求，会有跨域问题）
    timeout: 60000, // 超时时间，AI 对话推理时间较长，这里设置 60 秒
});*/
// 1. 创建原生的 Axios 实例（注意这里叫 instance，不要暴露出去）
const instance = axios.create({
  baseURL: '/api',
  timeout: 60000,
});

// 2. 请求拦截器（Request Interceptor）
instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {  // ← 这是定义的回调函数，类比后端 Spring：这和 Spring MVC 的拦截器 preHandle、postHandle 完全一样。你定义拦截逻辑，框架在合适时机调用
        // 在这里你可以做一些发送请求前的统一处理，比如：
        // 从 localStorage 或 Pinia 状态中获取 Token
        const token = localStorage.getItem('token');
        if (token && config.headers) {
            // 如果有 token，统一在 headers 中携带
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {   // ← 这是定义的回调函数
        // 请求发送失败时的处理，把异常抛出去
        return Promise.reject(error);
    }
);

// 3. 响应拦截器（Response Interceptor）
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    // 拿到后端返回的原始 JSON：{ code: ..., data: ..., message: ... }
    const baseRes = response.data;

    // 大厂规范：如果业务码不是 0 (SUCCESS)，说明有业务错误
    if (baseRes.code !== 0 && baseRes.code !== undefined) {

      // 特殊处理 40100 (未登录)
      if (baseRes.code === 40100) {
        ElMessage.error(baseRes.message || '登录已失效，请重新登录');
        localStorage.removeItem('token'); // 清除无效 token
        // 强制跳转回登录页
        window.location.href = '/login';
      } else {
        // 其他普通业务错误（如账号密码错误、AI 额度不足），统一全局弹窗
        ElMessage.error(baseRes.message || '系统繁忙，请稍后再试');
      }

      // 核心：必须 reject 抛出异常，阻止执行后续的页面业务逻辑
      return Promise.reject(new Error(baseRes.message || 'Error'));
    }

    // 只有 code === 0 时，才走到这里。
    return baseRes;
  },
    (error) => {
        // 如果 HTTP 状态码不是 2xx（比如 401, 403, 500），会进入这里
        console.error('err' + error); // 打印错误日志用于排查

        let message = error.message;
        if (error.response) {
            // 针对常见的 HTTP 错误状态码进行友好提示
            switch (error.response.status) {
                case 401:
                    message = '登录已过期，请重新登录';
                    // 这里以后可以写清理本地 Token、跳转到登录页的逻辑
                    break;
                case 403:
                    message = '拒绝访问，您没有权限';
                    break;
                case 500:
                    message = '后端服务异常，请联系管理员';
                    break;
                default:
                    message = error.response.data?.message || '网络连接异常';
            }
        }

        // 使用 Element Plus 的 ElMessage 组件进行全局报错提示
        ElMessage.error(message);
        return Promise.reject(error);
    }
);

/**
 * 导出包装器函数 (Wrapper)
 * 我们导出一个普通的函数，名字叫 request，完美对接 OpenAPI 生成的代码！
 * 这行代码的作用就是抹掉 AxiosResponse 的外壳提示，让 TS 只认识泛型 T。
 */
export default async function request<T>(url: string, options?: any): Promise<T> {
  const res = await instance(url, options);
  return res as unknown as T;
}


/*
*       在请求拦截器中：
        两种常用捕获写法：
        写法1：Promise 链式 .catch（最常用）
        // 接口调用
        request.get('/user/list')
        .then(data => {
          // 正常拿到业务数据
          console.log('请求成功', data)
        })
        .catch(err => {
          // 拦截器抛出的错误，都会汇总到这里
          console.log('请求异常', err)
          // 页面自己做后续兜底处理
        })
        写法2：async/await + try catch
        async function getInfo() {
          try {
            // 正常流程
            const baseRes = await request.get('/user/list')
            console.log('成功数据', baseRes)
          } catch (err) {
            // 拦截器抛出的异常，在这里精准捕获
            console.log('捕获到错误', err)
            // 页面弹窗提示、重置数据等操作
          }
        }
        */
