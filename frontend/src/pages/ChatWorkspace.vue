<template>
  <div class="relative flex flex-col h-full w-full bg-white overflow-hidden">

    <!-- 1. 顶部栏 (仅在有对话时显示当前会话标题) -->
    <header class="h-14 flex items-center justify-center border-b border-gray-100 flex-shrink-0 z-10 bg-white/80 backdrop-blur-md">
      <span class="text-sm font-medium text-gray-700">
        {{ isEmpty ? '新对话' : '正在对话' }}
      </span>
    </header>

    <!-- 2. 聊天消息记录区 (仅在有记录时渲染) -->
    <main v-if="!isEmpty" class="flex-1 overflow-y-auto p-4 pb-32 space-y-6 scroll-smooth">
      <div v-for="(msg, index) in chatStore.currentMessages" :key="index" class="flex flex-col">
        <!-- 消息气泡外壳：用户在右，AI 在左 -->
        <div :class="msg.type === 'user' ? 'self-end bg-blue-600 text-white' : 'self-start bg-white border border-gray-150 text-gray-800 shadow-sm'"
             class="max-w-[85%] rounded-2xl px-5 py-3 text-sm leading-relaxed whitespace-pre-wrap">

          <!-- 如果是用户消息，直接显示文本（注意template是vue的占位标签，任意层级都能嵌套，渲染时直接消失） -->
          <template v-if="msg.type === 'user'">
            {{ msg.content }}
          </template>

          <!-- 如果是 AI 消息，进行深度解析渲染 -->
          <template v-else>

            <!-- 魔法 1：深度推理模式的折叠框 (💡只要有 hasThink 标记，就瞬间渲染它) -->
            <details
              v-if="parseMessage(msg.content).hasThink"
              class="group mb-3 bg-gray-50 border border-gray-200 rounded-xl overflow-hidden"
              open
            >
              <summary class="px-4 py-2 text-xs font-bold text-gray-500 cursor-pointer hover:bg-gray-100 transition-colors flex items-center select-none outline-none">
                <span class="mr-2">{{ parseMessage(msg.content).isThinking ? '深度思考中...' : '✅ 思考过程' }}</span>
                <span class="text-[10px] text-gray-400 font-normal ml-auto group-open:hidden">点击展开</span>
                <span class="text-[10px] text-gray-400 font-normal ml-auto hidden group-open:block">点击收起</span>
              </summary>

              <!-- 内部的思考步骤流 -->
              <div class="px-4 py-3 text-xs text-gray-500 border-t border-gray-100 bg-gray-50/50 font-mono whitespace-pre-wrap">
                {{ parseMessage(msg.content).think }}
                <!-- 💡 正在打字时的光标闪烁动画 -->
                <span v-if="parseMessage(msg.content).isThinking" class="inline-block w-1.5 h-3 ml-1 bg-gray-400 animate-pulse"></span>
              </div>
            </details>

            <!-- 魔法 2：最终的回答内容 (仅在有回答时显示) -->
            <div v-if="parseMessage(msg.content).answer" class="text-gray-800 text-base leading-relaxed whitespace-pre-wrap">
              {{ parseMessage(msg.content).answer }}
            </div>

            <!-- 兜底：如果是普通恋爱大师模式（没有 <think> 标记），直接全量显示普通文本 -->
            <div v-if="!parseMessage(msg.content).hasThink" class="text-gray-800 text-base leading-relaxed whitespace-pre-wrap">
              {{ msg.content }}
            </div>

          </template>
        </div>
      </div>
    </main>

    <!-- 3. 核心大招：动态跃迁的输入区外壳，模式选择和聊天框 -->
    <!-- 如果为空：充满整个屏幕居中 (flex-1 justify-center) -->
    <!-- 如果有记录：绝对定位固定在底部 (absolute bottom-0) -->
    <div
      class="transition-all duration-500 ease-in-out w-full px-4 flex flex-col items-center"
      :class="isEmpty ? 'flex-1 justify-center' : 'absolute bottom-0 pb-6 bg-gradient-to-t from-white via-white to-transparent pt-10'"
    >

      <!-- 3.1 DeepSeek 同款：欢迎语与模式选择 (仅空状态显示) -->
      <div v-if="isEmpty" class="mb-8 w-full max-w-3xl flex flex-col items-center transition-opacity duration-300">
        <div class="text-3xl font-bold text-gray-900 mb-6 flex items-center space-x-3">
          <span class="text-blue-600"></span>
          <span>我是你的 AI 超级智能体</span>
        </div>

        <!-- 模式切换按钮组 -->
        <div class="flex p-1 bg-gray-100 rounded-xl space-x-1 mb-2">
          <button
            @click="agentType = 'STANDARD'"
            :class="agentType === 'STANDARD' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'"
            class="px-6 py-2 rounded-lg text-sm font-medium transition-all flex items-center space-x-2 cursor-pointer"
          >
            <span>⚡️ 标准模式</span>
          </button>
          <button
            @click="agentType = 'REASONING'"
            :class="agentType === 'REASONING' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'"
            class="px-6 py-2 rounded-lg text-sm font-medium transition-all flex items-center space-x-2 cursor-pointer"
          >
            <span>🤖 深度思考</span>
          </button>
        </div>
        <p class="text-xs text-gray-400 mt-2">
          {{ agentType === 'STANDARD' ? '直接输出结果，速度快，适合日常闲聊。' : '调用多步推理和工具，适合解决复杂问题。' }}
        </p>
      </div>

      <!-- 3.2 唯一的一个聊天输入框 (无论何种状态，永远是这个它)，   prevent 是阻止回车换行（因为在textarea里敲回车会换行） -->
      <div class="w-full max-w-3xl relative bg-white border-2 border-gray-200 focus-within:border-blue-500 rounded-2xl shadow-sm transition-colors p-2 flex items-end">

        <textarea
          v-model="inputText"
          rows="1"
          placeholder="给智能体发送消息..."
          class="flex-1 max-h-40 min-h-[44px] resize-none border-0 bg-transparent px-3 py-2 text-gray-800 placeholder-gray-400 focus:outline-none sm:text-sm"
          @keydown.enter.prevent="sendMessage"
        ></textarea>

        <!--trim()是去掉字符串的首尾空格，如果用户发一串空格，去掉后是是空串false，取反后是禁用按钮就不能发送消息-->
        <button
          @click="sendMessage"
          :disabled="!inputText.trim()"
          class="ml-2 flex-shrink-0 w-10 h-10 flex items-center justify-center rounded-xl bg-blue-600 text-white disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors"
        >
          <el-icon :size="18"><Position /></el-icon>
        </button>

      </div>

      <!-- 底部免责声明 -->
      <div v-if="isEmpty" class="mt-4 text-[11px] text-gray-400">
        内容由 AI 模型生成，请仔细甄别
      </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, } from 'vue'  // onMounted
import { useRoute, useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { requestStream } from '@/utils/stream.ts'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

// 本地状态
const agentType = ref('STANDARD') // 默认选中的模式
const inputText = ref('')
const isGenerating = ref(false)

// 核心计算属性：当前会话是否为空？
// 只要消息列表长度为 0，页面就渲染为“空状态（居中）”；只要 > 0，就渲染为“对话状态（沉底）”！
const isEmpty = computed(() => chatStore.currentMessages.length === 0)

/**
 * 发送消息逻辑与打字机逻辑
 */
const sendMessage = async () => {
  // 输入框要有内容同时不能是正在生成内容，才能往下，继续发消息
  if (!inputText.value.trim() || isGenerating.value) return

  const userMessage = inputText.value
  inputText.value = ''  // 用双向绑定将输入框内容清空
  isGenerating.value = true  // isGenerating是防重复提交锁，是true就是上锁

  let currentChatId = route.params.id as string
  const isNewChat = !currentChatId
  if (isNewChat) {
    currentChatId = crypto.randomUUID()
  }

  chatStore.currentMessages.push({ type: 'user', content: userMessage })  // 将用户消息塞进消息数组，页面立刻渲染用户发的文字
  const aiMessagePlaceholder = { type: 'assistant', content: '' }  // 因为sse是分片慢慢返回，先塞一个空串，生成一个ai气泡位置，后续不断拼接
  chatStore.currentMessages.push(aiMessagePlaceholder)
  const currentAiMessage = chatStore.currentMessages[chatStore.currentMessages.length - 1]  // 去除最后一项，后面打字追加都改这个对象的content属性

  const apiUrl = agentType.value === 'REASONING' ? '/ai/manus/chat/sse' : '/ai/love_app/chat/sse'
  const requestBody = { chatId: currentChatId, message: userMessage }

  // 💡====== 核心大招：前端平滑打字队列 ======💡
  let typingQueue = ''   // 暂存后端发来的文字（消息缓冲区，生产者，onChunk往里面塞字）
  let isTyping = false   // 标记打字机是否正在工作中（打字机工作标记）
  let isStreamDone = false // 标记后端流是否已经彻底断开（后端流是否全部发完）

  // 消费队列的递归函数，从队列取出字，拼到ai消息content（aiMessagePlaceholder）
  const processTyping = () => {
    if (typingQueue.length > 0) {
      // 取出第一个字，追加到界面上
      currentAiMessage.content += typingQueue.charAt(0)
      // 把刚才取出的字从队列里删掉
      typingQueue = typingQueue.substring(1)
      // scrollToBottom() // 触发滚动条到底部

      // 控制打字速度，15毫秒吐一个字（可自己调）
      setTimeout(processTyping, 15)
    } else {
      isTyping = false
      // 如果打字队列空了，且后端也发完断开了，才真正解锁发送按钮
      if (isStreamDone) {
        finishGeneration()
      }
    }
  }

  const finishGeneration = () => {
    isGenerating.value = false  // 开锁
    if (isNewChat) {
      chatStore.fetchSessionList()
      router.replace(`/chat/${currentChatId}`)
    }
  }
  // 💡=========================================💡

  await requestStream(apiUrl, 'POST', requestBody, {
    onChunk: (chunkText) => {
      // 后端来分片数据了，不要直接上屏，而是塞进等待队列（消息缓冲区）
      typingQueue += chunkText
      // 如果打字机在休息，赶紧把它叫醒起来干活
      if (!isTyping) {
        isTyping = true
        processTyping()  // 一个个拼到aiMessagePlaceholder后面（15ms频率），用来显示ai气泡文字
      }
    },
    onDone: () => {
      isStreamDone = true // 标记后端流结束
      // 如果此时打字机已经打完了，直接结束；如果还在打，等它自己打完结束
      if (!isTyping) {
        finishGeneration()
      }
    },
    onError: (err) => {
      console.error('AI 请求失败', err)
      typingQueue += '\n[网络异常，请重试]'
      if (!isTyping) {
        isTyping = true
        processTyping()
      }
      isStreamDone = true
    }
  })
}

/**
 * 监听路由 URL 的变化，自动拉取对应的聊天记录
 * 比如从 /chat (新建) 切换到 /chat/123-abc (历史)，就会触发这里
 */
watch(
  () => route.params.id,
  async (newId) => {
    // 强制转换为 string 传入
    await chatStore.fetchSessionMessages(newId as string)
  },
  { immediate: true } // 页面刚加载时也立即执行一次（因为watch默认只有数据变化，才执行回调）
)

// 💡 解析消息内容，分离思考过程和最终回复
const parseMessage = (text: string) => {
  if (!text) return { hasThink: false, think: '', answer: '', isThinking: false }

  // 1. 判断是否包含 <think> 标签（哪怕是残缺的，只要有了它，就开启灰色框）
  const hasThink = text.includes('<think>')

  // 2. 提取 <think>...</think> 之间的内容
  const thinkMatch = text.match(/<think>([\s\S]*?)(<\/think>|$)/)
  const thinkContent = thinkMatch ? thinkMatch[1].trim() : ''

  // 3. 提取 </think> 之后的内容作为最终回答
  let answerContent = ''
  if (text.includes('</think>')) {
    answerContent = text.substring(text.indexOf('</think>') + 8).trim()
  }

  // 4. 判断是否正在思考中
  const isThinking = hasThink && !text.includes('</think>')

  return {
    hasThink,           // 是否应该展示灰色框
    think: thinkContent, // 思考的具体内容
    answer: answerContent, // 最终的回答内容
    isThinking          // 是否还在转圈思考
  }
}
</script>

<style scoped>
/* 隐藏原生滚动条，保持输入框清爽 */
textarea::-webkit-scrollbar {
  display: none;
}
</style>
