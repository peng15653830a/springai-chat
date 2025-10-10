package com.example.service;

import java.util.List;
import java.util.Map;

/**
 * 小说写作 RAG 服务接口
 */
public interface NovelRagService {

    /**
     * 从本地路径导入素材
     *
     * @param path 文件或文件夹路径
     * @param recursive 是否递归导入子文件夹
     * @return 导入结果统计
     */
    Map<String, Object> importFromPath(String path, boolean recursive);

    /**
     * 从网页抓取素材
     *
     * @param url 起始URL
     * @param maxPages 最大抓取页面数
     * @param sameDomainOnly 是否只抓取同域名页面
     * @return 抓取结果统计
     */
    Map<String, Object> crawlFromUrl(String url, int maxPages, boolean sameDomainOnly);

    /**
     * 获取已导入的素材列表
     *
     * @return 素材列表和统计信息
     */
    Map<String, Object> getMaterials();

    /**
     * 搜索素材
     *
     * @param query 搜索查询
     * @param topK 返回结果数量
     * @return 相关素材列表
     */
    List<Map<String, Object>> searchMaterials(String query, int topK);

    /**
     * 清空所有素材
     */
    void clearAll();
}
