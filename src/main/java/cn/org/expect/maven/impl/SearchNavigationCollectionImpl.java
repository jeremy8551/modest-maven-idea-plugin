package cn.org.expect.maven.impl;

import java.util.List;

import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationCollection;
import cn.org.expect.util.Ensure;

/**
 * 导航记录集合
 */
public class SearchNavigationCollectionImpl implements SearchNavigationCollection {

    /** 总记录数 */
    private final int foundNumber;

    /** true表示还有未读数据，false表示已全部读取 */
    private final boolean hasMore;

    /** 导航记录集合 */
    private final List<SearchNavigation> list;

    public SearchNavigationCollectionImpl(List<SearchNavigation> list, int foundNumber, boolean hasMore) {
        this.list = Ensure.notNull(list);
        this.foundNumber = foundNumber;
        this.hasMore = hasMore;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public int size() {
        return this.list.size();
    }

    public SearchNavigation get(int index) {
        return this.list.get(index);
    }
}
