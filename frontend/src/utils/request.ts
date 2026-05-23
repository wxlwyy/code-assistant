import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 1. 创建 Axios 实例
const request: AxiosInstance = axios.create({
    // baseURL 会拼接在所有请求路径的前面。
    // 暂时写 /api，后面我们会配置 Vite 的代理（Proxy）来转发到你的 Java 后端端口（如 8080）
    baseURL: '/api',
    timeout: 60000, // 超时时间，AI 对话推理时间较长，这里设置 60 秒
});

// 2. 请求拦截器（Request Interceptor）
request.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 在这里你可以做一些发送请求前的统一处理，比如：
        // 从 localStorage 或 Pinia 状态中获取 Token
        const token = localStorage.getItem('token');
        if (token && config.headers) {
            // 如果有 token，统一在 headers 中携带
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        // 请求发送失败时的处理
        return Promise.reject(error);
    }
);

// 3. 响应拦截器（Response Interceptor）
request.interceptors.response.use(
    (response: AxiosResponse) => {
        // 如果 HTTP 状态码是 2xx，会进入这里。
        // 你可以根据后端定义的统一返回格式（比如 Result<T>）进行数据拆包。
        // 假设你的后端统一返回格式是 { code: 0, data: ..., message: ... }
        const res = response.data;

        // 如果后端返回的业务 code 不是成功（假设 0 代表成功），则进行全局弹窗报错
        if (res.code !== 0 && res.code !== undefined) {
            ElMessage.error(res.message || '系统繁忙，请稍后再试');
            return Promise.reject(new Error(res.message || 'Error'));
        }

        // 成功则直接返回 data 数据，免去我们在页面里每次都要写 .data.data 的烦恼
        return res.data !== undefined ? res.data : res;
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

export default request;