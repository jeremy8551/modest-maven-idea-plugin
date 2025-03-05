package cn.org.expect.intellij.idea.plugin.maven;

import java.awt.*;

import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationCollection;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class MavenSearchPluginContextImpl implements MavenSearchPluginContext {

    /** 事件 */
    private final AnActionEvent event;

    /** 选中的版本列表记录 */
    private volatile SearchNavigation selectedNavigation;

    /** 显示搜索结果的位置信息 */
    private volatile Rectangle visibleRect;

    /** 导航信息 */
    private volatile SearchNavigationCollection navigationList;

    public MavenSearchPluginContextImpl(AnActionEvent event) {
        this.event = event;
    }

    public AnActionEvent getActionEvent() {
        return this.event;
    }

    public SearchNavigation geSelectedNavigation() {
        return selectedNavigation;
    }

    public void setSelectedNavigation(SearchNavigation selectNavigation) {
        this.selectedNavigation = selectNavigation;
    }

    public Rectangle getVisibleRect() {
        return visibleRect;
    }

    public void setVisibleRect(Rectangle visibleRect) {
        this.visibleRect = visibleRect;
    }

    public SearchNavigationCollection getNavigationCollection() {
        return navigationList;
    }

    public void setNavigationCollection(SearchNavigationCollection navigationList) {
        this.navigationList = navigationList;
    }
}
