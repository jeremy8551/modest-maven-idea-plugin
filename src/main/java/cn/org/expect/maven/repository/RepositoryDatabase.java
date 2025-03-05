package cn.org.expect.maven.repository;

/**
 * 数据库接口
 */
public interface RepositoryDatabase {

    /**
     * 模糊搜索
     *
     * @param id 唯一编号
     * @return 搜索结果
     */
    SearchResult select(String id);

    /**
     * 保存搜索结果
     *
     * @param id        唯一编号
     * @param resultSet 搜索结果
     */
    void insert(String id, SearchResult resultSet);

    /**
     * 删除搜索结果
     *
     * @param id 唯一编号
     */
    void delete(String id);

    /**
     * 保存搜索结果
     *
     * @param groupId    域名
     * @param artifactId 工件ID
     * @param result     搜索结果
     */
    void insert(String groupId, String artifactId, SearchResult result);

    /**
     * 查询搜索结果
     *
     * @param groupId    域名
     * @param artifactId 工件ID
     * @return 搜索结果
     */
    SearchResult select(String groupId, String artifactId);
}
