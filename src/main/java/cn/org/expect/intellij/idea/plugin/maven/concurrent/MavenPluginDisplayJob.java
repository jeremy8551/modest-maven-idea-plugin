package cn.org.expect.intellij.idea.plugin.maven.concurrent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearch;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.SearchDisplay;
import cn.org.expect.maven.search.SearchNavigationList;
import cn.org.expect.maven.concurrent.MavenJob;
import cn.org.expect.maven.concurrent.SearchMoreJob;
import cn.org.expect.maven.impl.SearchNavigationCollectionImpl;
import cn.org.expect.maven.search.ArtifactSearchStatusMessageType;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationCollection;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereFoundElementInfo;

public class MavenPluginDisplayJob extends MavenJob implements EDTJob {

    /** 锁 */
    protected final static Object lock = new Object();

    /** 搜索结果的导航记录 */
    protected final SearchNavigationCollection navigationCollection;

    public MavenPluginDisplayJob(SearchNavigationCollection navigationCollection) {
        super("maven.search.job.display.search.result.description");
        this.navigationCollection = navigationCollection;
    }

    /**
     * 需要同步锁保证同时只有一个渲染任务在执行
     *
     * @return 返回值
     */
    public int execute() {
        synchronized (lock) {
            this.display(this.navigationCollection);
        }
        return 0;
    }

    protected void display(SearchNavigationCollection navigationCollection) {
        if (navigationCollection == null) {
            navigationCollection = new SearchNavigationCollectionImpl(new ArrayList<>(0), 0, false);
        }

        MavenSearchPlugin plugin = (MavenSearchPlugin) this.getSearch();
        SearchDisplay display = plugin.getIdeaUI().getDisplay();
        boolean isAllTab = plugin.isAllTab();
        boolean isSelfTab = plugin.isSelfTab();

        // 如果不是All与自身的Tab，则删除搜索结果中的导航记录
        if (!isAllTab && !isSelfTab) {
            display.clearSelfNavigation();
            display.paint();
            return;
        }

        Rectangle visibleRect = plugin.getContext().getVisibleRect();
        plugin.getContext().setVisibleRect(null);
        boolean hasMore = display.hasMore();

        // 备份所有搜索类别的 more 值
        Map<SearchEverywhereContributor<?>, Boolean> backup = isAllTab ? display.getContributorMores() : null;

        // 生成导航记录
        plugin.aware(navigationCollection);
        List<SearchEverywhereFoundElementInfo> infos = this.toNavigationCollection(plugin, navigationCollection);

        display.clearMore(); // 一定要先删除 more 按钮
        display.merge(infos, isAllTab); // 将导航记录合并到数据模型中
        display.select(plugin.getContext().geSelectedNavigation());

        if (backup != null) {
            display.setContributorMores(backup); // 恢复所有搜索类别的 more 值
        }

        // 设置 more 按钮
        display.setContributorMore(plugin.getContributor(),  //
                !plugin.getService().isRunning(SearchMoreJob.class)  // 在 MavenSearchPluginListener 中会重复生成 more 按钮，判断如果正在执行 more 搜索，则不能显示 more 按钮
                        && ( //
                        (isAllTab && hasMore && display.size() > 0) // ALL标签页，有 more 按钮
                                || (isSelfTab && navigationCollection.isHasMore()) //
                ) //
        );

        display.paint(); // 渲染
        display.setVisibleRange(visibleRect);

        if (log.isDebugEnabled()) {
            log.debug("{} size: {}", this.getName(), display.size());
        }

        // 设置状态栏信息
        plugin.setStatusBar(ArtifactSearchStatusMessageType.NORMAL, "maven.search.status.text", navigationCollection.getFoundNumber(), navigationCollection.size());
    }

    public List<SearchEverywhereFoundElementInfo> toNavigationCollection(MavenSearch plugin, SearchNavigationCollection navigationCollection) {
        int navigationPriority = plugin.getSettings().getNavigationPriority();
        SearchEverywhereContributor<?> contributor = plugin.getContributor();

        List<SearchEverywhereFoundElementInfo> list = new ArrayList<>();
        for (int i = 0; i < navigationCollection.size(); i++) {
            SearchNavigation navigation = navigationCollection.get(i);
            this.addNavigation(list, navigation, navigationPriority, contributor);
        }
        return list;
    }

    protected void addNavigation(List<SearchEverywhereFoundElementInfo> list, SearchNavigation navigation, int priority, SearchEverywhereContributor<?> contributor) {
        navigation.update();
        list.add(new SearchEverywhereFoundElementInfo(navigation, priority, contributor));
        if (!navigation.isFold()) {
            SearchNavigationList navigationList = navigation.getChildNavigation();
            for (int i = 0; i < navigationList.size(); i++) {
                SearchNavigation child = navigationList.get(i);
                this.addNavigation(list, child, priority, contributor);
            }
        }
    }
}
