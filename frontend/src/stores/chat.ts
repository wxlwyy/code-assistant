import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
// 💡 根据你实际自动生成的 API 文件路径引入
import { listMySessions, deleteSession, getSessionHistory } from '@/api/chatSessionController'

export const useChatStore = defineStore('chat', () => {
  // 1. 状态 State
  const sessionList = ref<API.ChatSessionVO[]>([]) // 历史会话列表
  const isLoadingSessions = ref(false) // 是否正在加载列表

  // 新增：当前会话的历史消息列表
  const currentMessages = ref<API.ChatMessageVO[]>([])
  const isLoadingMessages = ref(false)

  // 2. 动作 Actions

  /**
   * 拉取当前用户的会话列表
   */
  const fetchSessionList = async () => {
    isLoadingSessions.value = true
    try {
      const res = await listMySessions()
      if (res.data) {
        sessionList.value = res.data
      }
    } catch (error) {
      console.error('获取历史会话失败', error)
    } finally {
      isLoadingSessions.value = false
    }
  }

  /**
   * 新增：拉取某个会话的历史消息详情
   */
  const fetchSessionMessages = async (chatId: string) => {
    // 如果没有传入 chatId，说明是新建对话，直接清空消息
    if (!chatId) {
      currentMessages.value = []
      return
    }

    isLoadingMessages.value = true
    try {
      // 调用刚补齐的后端接口
      const res = await getSessionHistory({ chatId }) // 根据 OpenAPI 传参格式调整
      if (res.data) {
        // 拿到历史数据，赋值给本地数组
        currentMessages.value = res.data
      }
    } catch (error) {
      console.error('获取历史消息失败', error)
      ElMessage.error('无法加载历史记录')
      currentMessages.value = []
    } finally {
      isLoadingMessages.value = false
    }
  }

  /**
   * 删除指定的会话
   */
  const removeSession = async (chatId: string) => {
    try {
      const res = await deleteSession({ id: chatId })
      if (res.data) {
        ElMessage.success('删除成功')
        // 删除成功后，重新拉取一次列表，刷新侧边栏
        await fetchSessionList()
      }
    } catch (error) {
      console.error('删除会话失败', error)
    }
  }

  return {
    sessionList,
    isLoadingSessions,
    currentMessages,
    isLoadingMessages,
    fetchSessionList,
    fetchSessionMessages,
    removeSession,
  }
})
