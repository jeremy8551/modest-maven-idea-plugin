package cn.org.expect.intellij.idea.plugin.maven.navigation;

import java.util.ArrayList;
import java.util.List;

import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.SearchNavigationList;
import cn.org.expect.maven.search.SearchNavigation;

public class NavigationListImpl implements SearchNavigationList {

    private final List<SearchNavigation> list;

    public NavigationListImpl() {
        this.list = new ArrayList<>();
    }

    public void clear() {
        this.list.clear();
    }

    public void add(Search search, SearchNavigation navigation) {
        search.aware(navigation);
        this.list.add(navigation);
    }

    public SearchNavigation get(int index) {
        return this.list.get(index);
    }

    public int size() {
        return this.list.size();
    }
}
