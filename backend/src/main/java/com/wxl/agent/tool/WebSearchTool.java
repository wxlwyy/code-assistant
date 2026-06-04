package com.wxl.agent.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具
 */
public class WebSearchTool {

    // 修改为 Serper.dev 的接口地址
    private static final String SEARCH_API_URL = "https://google.serper.dev/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Google Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        try {
            // 构造 JSON 请求体（只需要 q 参数）
            String requestBody = JSONUtil.toJsonStr(Map.of("q", query));

            // 发送 POST 请求：API Key 放入 X-API-KEY 请求头
            String response = HttpRequest.post(SEARCH_API_URL)
                    .header("X-API-KEY", apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .execute()
                    .body();

            // 解析响应 JSON
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // Serper.dev 的搜索结果在 "organic" 数组中
            JSONArray organicResults = jsonObject.getJSONArray("organic");
            if (organicResults == null || organicResults.isEmpty()) {
                return "No results found.";
            }

            // 取前 5 条
            int limit = Math.min(5, organicResults.size());
            List<Object> topResults = organicResults.subList(0, limit);

            // 拼接结果
            return topResults.stream()
                    .map(obj -> ((JSONObject) obj).toString())
                    .collect(Collectors.joining(","));

        } catch (Exception e) {
            return "Error searching Google: " + e.getMessage();
        }
    }
}

/*
public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            // 取出返回结果的前 5 条
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取 organic_results 部分
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            List<Object> objects = organicResults.subList(0, 5);
            // 拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}*/
