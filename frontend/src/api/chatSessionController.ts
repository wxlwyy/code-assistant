// @ts-ignore
/* eslint-disable */
import request from '@/utils/request'

/** 此处后端没有提供注释 POST /session/delete */
export async function deleteSession(
  body: API.ChatSessionDeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>('/session/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /session/history/${param0} */
export async function getSessionHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSessionHistoryParams,
  options?: { [key: string]: any },
) {
  const { chatId: param0, ...queryParams } = params
  return request<API.BaseResponseListChatMessageVO>(`/session/history/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /session/list */
export async function listMySessions(options?: { [key: string]: any }) {
  return request<API.BaseResponseListChatSessionVO>('/session/list', {
    method: 'GET',
    ...(options || {}),
  })
}
