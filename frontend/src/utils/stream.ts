/**
 * 统一流式请求处理工具（升级版，支持 GET 和 POST）
 * @param url 请求路径
 * @param method 请求方法 'GET' | 'POST'
 * @param body 请求体数据（仅 POST 时有效）
 * @param callbacks 回调配置
 */
export async function requestStream(
  url: string,
  method: 'GET' | 'POST' = 'GET',  // 'GET' | 'POST' 是限定类型，= 'GET' 是默认值
  body?: any,
  callbacks?: {
    onChunk: (text: string) => void
    onDone?: () => void
    onError?: (err: any) => void
  }
) {
  const { onChunk, onDone, onError } = callbacks || {}

  try {
    const BASE_URL = '/api'

    // 自动拼接为 /api/ai/love_app/chat/sse
    const fullUrl = BASE_URL + url

    // 1. 配置 Fetch 请求参数
    const fetchOptions: RequestInit = {
      method,
      headers: {
        'Content-Type': 'application/json',
        //  联动：由于我们写了全局登录拦截器，这里必须手动把 Token 塞进 Header 里
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      }
    }

    // 如果是 POST 请求，将 body 序列化为 JSON 字符串。RequestInit的body属性不能直接传js对象，只接收字符串/Blob/FormData等二进制类型，因为js对象是内存结构，浏览器没法直接传给TCP网络。
    if (method === 'POST' && body) {
      fetchOptions.body = JSON.stringify(body)
    }

    // 2. 发起请求
    const response = await fetch(fullUrl, fetchOptions)

    if (!response.ok) {
      throw new Error(`HTTP 请求失败，状态码: ${response.status}`)
    }

    const reader = response.body?.getReader() // 响应体是二进制可读流，创建流读取器
    if (!reader) {
      throw new Error('当前浏览器不支持可读流，或者响应体为空')
    }

    const decoder = new TextDecoder('utf-8')  // 二进制转为json字符串
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()  // 每次读一小块二进制，done是否流结束
      if (done) break

      const chunkStr = decoder.decode(value, { stream: true })  // stream: true代表分段解码
      buffer += chunkStr

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed) continue

        if (trimmed.startsWith('data:')) {
          const content = trimmed.substring(5).trim()
          if (content === '[DONE]') continue
          onChunk?.(content)
        } else {
          onChunk?.(trimmed)
        }
      }
    }

    if (buffer) {
      const trimmed = buffer.trim()
      if (trimmed.startsWith('data:')) {
        onChunk?.(trimmed.substring(5).trim())
      } else {
        onChunk?.(trimmed)
      }
    }

    onDone?.()
  } catch (err) {
    if (onError) {
      onError(err)
    } else {
      console.error('流式请求处理失败:', err)
    }
  }
}

/**
 * 统一流式请求处理工具，这个工具函数将用最现代的 JS 原生流读取机制，将网络传过来的二进制数据流（Chunk）实时解码，按行解析，并在每收到新字符时触发回调通知页面渲染
 * @param url 完整的流式接口请求地址
 * @param options 配置回调函数
 */
/*export async function requestStream(  //标记为异步函数。因为内部要用 await 等待网络数据，但不阻塞浏览器主线程。
  url: string,
  options: {
    // 每次收到新的文本分片时触发
    onChunk: (text: string) => void
    // 传输完全结束时触发（问号代表可选）
    onDone?: () => void
    // 传输发生异常时触发
    onError?: (err: any) => void
  }
) {
  const { onChunk, onDone, onError } = options  //解构options对象里的三个函数成三个变量，之后代码直接用变量名

  try {
    // 1. 发起 Fetch 请求
    const response = await fetch(url)

    if (!response.ok) {  // true代表http状态码在200-299之间，表示成功
      throw new Error(`HTTP 请求失败，状态码: ${response.status}`)
    }

    // 2. 获取 ReadableStream 的读取器(一个可读流的读取器)，这是浏览器原生提供的 API，用于逐块读取响应体。
    const reader = response.body?.getReader()
    if (!reader) { // 用！变量，对象真值（所以普通对象、数组、函数等）就会转成true，假值（undefined、null、0、“”、NAN）转为false
      throw new Error('当前浏览器不支持可读流，或者响应体为空')
    }

    // 3. 初始化文本解码器（UTF-8）
    const decoder = new TextDecoder('utf-8')
    let buffer = '' // 用于暂存未拼接完整的文本行

    // 4. 循环读取流数据
    while (true) {
      const { value, done } = await reader.read()  // reader.read()读取下一个数据块。
      if (done) {  // done 为 true 表示流结束。
        break // 流读取结束，跳出循环
      }

      // 将二进制数据块转为文本字符串，{ stream: true } 表示数据是连续的，解码器会正确处理多字节字符的边界。
      const chunkStr = decoder.decode(value, { stream: true })
      buffer += chunkStr

      // 标准的 SSE (Server-Sent Events) 协议是以换行符 \n 分割数据帧的
      const lines = buffer.split('\n')

      // 弹出并保留最后一个可能尚未传输完毕的半截行，留到下一次接收时拼接
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed) continue

        // 判断是否是标准的 SSE 数据帧格式
        if (trimmed.startsWith('data:')) {
          const content = trimmed.substring(5).trim()

          // 过滤掉 SSE 协议可能自带的 [DONE] 结束标记
          if (content === '[DONE]') {
            continue
          }

          onChunk(content)
        } else {
          // 备用：如果后端输出的是纯文本（没有带 data: 前缀），直接作为文本输出
          onChunk(trimmed)
        }
      }
    }

    // 5. 循环结束后，如果缓冲区里还有最后一小段残留文本，进行最后一次输出
    if (buffer) {
      const trimmed = buffer.trim()
      if (trimmed.startsWith('data:')) {
        onChunk(trimmed.substring(5).trim())
      } else {
        onChunk(trimmed)
      }
    }

    // 6. 成功结束回调
    onDone?.()

  } catch (err) {
    if (onError) {
      onError(err)
    } else {
      console.error('流式请求处理失败:', err)
    }
  }
}*/
