package cn.org.expect.maven.repository;

import java.util.Map;

/**
 * 仓库存储引擎
 */
public interface RepositoryDatabaseEngine {

    /**
     * 返回模糊搜索结果的集合
     *
     * @return 模糊搜索结果的集合
     */
    Map<String, SearchResult> getPattern();

    /**
     * 返回精确搜索结果的集合
     *
     * @return 精确搜索结果的集合
     */
    Map<String, Map<String, SearchResult>> getArtifact();

    /**
     * 加载数据
     */
    void load();

    /**
     * 保存数据
     */
    void save();

    /**
     * 记录数
     *
     * @return 记录数
     */
    int size();

    /**
     * 清空所有数据
     */
    void clear();
}
