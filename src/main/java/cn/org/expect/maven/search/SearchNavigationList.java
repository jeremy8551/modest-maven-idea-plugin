package cn.org.expect.maven.search;

public interface SearchNavigationList {

    /**
     * 添加导航记录
     *
     * @param search     搜索接口
     * @param navigation 导航记录
     */
    void add(Search search, SearchNavigation navigation);

    /**
     * 返回导航记录
     *
     * @param index 位置信息，从0开始
     * @return 导航就
     */
    SearchNavigation get(int index);

    /**
     * 导航记录个数
     *
     * @return 导航记录个数
     */
    int size();

    /**
     * 删除所有导航记录
     */
    void clear();
}
