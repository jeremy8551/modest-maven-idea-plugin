package cn.org.expect.maven.repository.impl;

import java.util.ArrayList;
import java.util.List;

import cn.org.expect.maven.repository.SearchResultType;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.util.Ensure;

public class DefaultSearchResult implements SearchResult {

    /** 搜索结果所属仓库的Class信息 */
    private final String repositoryName;

    /** 搜索结果类型 */
    private final SearchResultType type;

    /** 工件集合 */
    private final List<Object> list;

    /** 下次开始读取记录的位置，从1开始 */
    private final int start;

    /** 总记录数 */
    private final int foundNumber;

    /** 查询时间 */
    private final long queryTime;

    /** true表示还有未读数据，false表示已全部读取 */
    private final boolean hasMore;

    public DefaultSearchResult(String repository) {
        this(repository, SearchResultType.ALL, new ArrayList<>(0), 0, 0, System.currentTimeMillis(), false);
    }

    public DefaultSearchResult(String repository, SearchResultType type, List<Object> list, int start, int foundNumber, long queryTime, boolean hasMore) {
        this.repositoryName = repository;
        this.type = Ensure.notNull(type);
        this.list = Ensure.notNull(list);
        this.start = start;
        this.foundNumber = foundNumber;
        this.queryTime = queryTime;
        this.hasMore = hasMore;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public SearchResultType getType() {
        return type;
    }

    public List<Object> getList() {
        return this.list;
    }

    public int getStart() {
        return start;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public int size() {
        return this.list.size();
    }

    public boolean isHasMore() {
        return hasMore;
    }
}
