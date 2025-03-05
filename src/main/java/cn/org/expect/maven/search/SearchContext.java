package cn.org.expect.maven.search;

/**
 * 搜索接口的上下文信息
 */
public interface SearchContext {

    /**
     * 返回导航记录
     *
     * @return 导航记录
     */
    SearchNavigationCollection getNavigationCollection();

    /**
     * 设置导航记录
     *
     * @param navigationList 导航记录
     */
    void setNavigationCollection(SearchNavigationCollection navigationList);
}
