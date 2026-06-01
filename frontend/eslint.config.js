import pluginVue from 'eslint-plugin-vue';
import vueTsEslintConfig from '@vue/eslint-config-typescript';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';

export default [
    // 1. 配置需要校验的文件范围
    {
        name: 'app/files-to-lint',
        files: ['**/*.{ts,mts,tsx,vue,js,jsx}'],
    },

    // 2. 配置需要忽略校验的文件夹（打包产物、缓存等）
    {
        name: 'app/files-to-ignore',
        ignores: [
            '**/dist/**',
            '**/dist-ssr/**',
            '**/coverage/**',
            '**/node_modules/**',
            'src/api/**' // 核心：加入这行！让 ESLint 彻底放行自动生成的代码（比如openapi生成的文件）
        ],
    },

    // 3. 引入 Vue 3 的基础校验规则
    ...pluginVue.configs['flat/essential'],

    // 4. 引入 TypeScript 与 Vue 结合的校验规则
    ...vueTsEslintConfig(),

    // 5. 引入 Prettier 冲突规避规则（告诉 ESLint 凡是格式问题全部让步给 Prettier 处理，避免打架）
    skipFormatting,
];
