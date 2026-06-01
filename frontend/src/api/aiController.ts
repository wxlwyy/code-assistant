// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 GET /ai/love_app/chat/server_sent_event */
export async function doChatWithLoveAppServerSentEvent(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doChatWithLoveAppServerSentEventParams,
  options?: { [key: string]: any },
) {
  return request<API.ServerSentEventString[]>('/ai/love_app/chat/server_sent_event', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/love_app/chat/sse */
export async function doChatWithLoveAppSse(
  body: API.AiChatRequest,
  options?: { [key: string]: any },
) {
  return request<string[]>('/ai/love_app/chat/sse', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /ai/love_app/chat/sync */
export async function doChatWithLoveAppSync(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doChatWithLoveAppSyncParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString>('/ai/love_app/chat/sync', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/manus/chat/sse */
export async function doChatWithManusSse(
  body: API.AiChatRequest,
  options?: { [key: string]: any },
) {
  return request<string[]>('/ai/manus/chat/sse', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
