declare namespace API {
  type AiChatRequest = {
    chatId: string
    message: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListChatSessionVO = {
    code?: number
    data?: ChatSessionVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type ChatSessionDeleteRequest = {
    id: string
  }

  type ChatSessionVO = {
    id?: string
    title?: string
    agentType?: string
    createTime?: string
  }

  type doChatWithLoveAppServerSentEventParams = {
    message: string
    chatId: string
  }

  type doChatWithLoveAppSyncParams = {
    message: string
    chatId: string
  }

  type LoginUserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userRole?: string
    token?: string
    createTime?: string
  }

  type ServerSentEventString = true

  type UserLoginRequest = {
    userAccount: string
    userPassword: string
  }

  type UserRegisterRequest = {
    userAccount: string
    userPassword: string
    checkPassword: string
  }
}
