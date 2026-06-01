import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/utils/request'",  //引入这个对象
  schemaPath: 'http://localhost:8081/api/v3/api-docs',  //swagger接口文档
  serversPath: './src',   //生成的代码放在src目录下
  folderName: '/api'
})
