package cn.org.expect.intellij.idea.plugin.maven;

import cn.org.expect.intellij.idea.plugin.maven.settings.MavenPluginSettings;
import cn.org.expect.maven.search.Search;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;

public interface MavenSearch extends Search {

    MavenSearchPluginContext getContext();

    /**
     * 配置信息
     *
     * @return 配置信息
     */
    MavenPluginSettings getSettings();

    /**
     * 返回 Maven+ 的搜索类别
     *
     * @return 搜索类别
     */
    SearchEverywhereContributor<?> getContributor();

    /**
     * 刷新查询结果
     *
     * @param pattern 字符串
     */
    default void refresh(String pattern) {
        this.setProgress("");
        this.setStatusBar(null, "");
        this.getContext().setNavigationCollection(null);
        this.getContext().setSelectedNavigation(null);
        this.display();

        this.getDatabase().delete(pattern);
        this.asyncSearch(pattern);
    }
}
