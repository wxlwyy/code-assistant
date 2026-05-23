import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        // 直接指向我们的主布局，我们的聊天逻辑都会在这个布局和组件中展现
        component: () => import('../layouts/BasicLayout.vue'),
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;