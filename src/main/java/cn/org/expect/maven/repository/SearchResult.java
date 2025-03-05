package cn.org.expect.maven.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.org.expect.maven.Artifact;

/**
 * 搜索结果
 */
public interface SearchResult {

    /**
     * 搜索结果所属的仓库
     *
     * @return 仓库的Class信息
     */
    String getRepositoryName();

    /**
     * 搜索结果类型
     *
     * @return 类型
     */
    SearchResultType getType();

    /**
     * Maven 工件列表
     *
     * @return 集合
     */
    List<Object> getList();

    /**
     * 下次查询的起始位置
     *
     * @return 位置信息，从1开始
     */
    int getStart();

    /**
     * 返回总记录数
     *
     * @return 总记录数
     */
    int getFoundNumber();

    /**
     * Maven 工件个数
     *
     * @return 工件个数
     */
    int size();

    /**
     * 返回查询时间
     *
     * @return 查询时间
     */
    long getQueryTime();

    /**
     * 是否有未读数据
     *
     * @return true表示还有未读数据，false表示已全部读取
     */
    boolean isHasMore();

    /**
     * 判断搜索结果是否是指定仓库的搜索结果
     *
     * @param type 仓库Class信息
     * @return 返回true表示是 false表示不是
     */
    default boolean isRepository(Class<?> type) {
        return type != null && type.getName().equals(this.getRepositoryName());
    }

    /**
     * 判断查询结果是否过期
     *
     * @param timeMillis 过期时间，单位毫秒
     * @return 返回true表示过期，false表示未过期
     */
    default boolean isExpire(long timeMillis) {
        return System.currentTimeMillis() - this.getQueryTime() >= timeMillis;
    }

    /**
     * 脚本文件中使用的方法 <br>
     * 在保持工件的原始顺序的前提下，对集合中不同类型的工件进行归类
     */
    default SearchResult sortByGroup() {
        List<Object> list = this.getList();
        Map<String, List<Artifact>> map = new LinkedHashMap<>(); // 使用 LinkedHashMap 保证插入顺序
        for (Object object : list) { // 遍历列表，根据类型分组
            Artifact artifact = (Artifact) object;
            String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(artifact);
        }

        list.clear();
        Set<Map.Entry<String, List<Artifact>>> entries = map.entrySet();
        for (Map.Entry<String, List<Artifact>> entry : entries) {
            List<Artifact> value = entry.getValue();
            value.sort((a1, a2) -> a2.getVersion().compareTo(a1.getVersion()));
            list.addAll(value);
        }
        return this;
    }
}
