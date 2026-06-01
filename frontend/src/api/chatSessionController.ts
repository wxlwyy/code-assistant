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

/** 此处后端没有提供注释 GET /session/list */
export async function listMySessions(options?: { [key: string]: any }) {
  return request<API.BaseResponseListChatSessionVO>('/session/list', {
    method: 'GET',
    ...(options || {}),
  })
}
