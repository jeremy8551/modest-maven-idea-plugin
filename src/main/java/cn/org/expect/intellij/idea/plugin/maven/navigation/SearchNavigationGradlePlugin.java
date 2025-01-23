package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginIcon;
import cn.org.expect.maven.concurrent.MavenJob;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.gradle.GradlePlugin;

public class SearchNavigationGradlePlugin extends AbstractSearchNavigation {

    public SearchNavigationGradlePlugin(GradlePlugin gradlePlugin) {
        super(gradlePlugin);
        this.setDepth(1);
        this.setPresentableText(gradlePlugin.getId());
        this.setLocationString("");
        this.setMiddleText("");
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_FOLD);
        this.setRightIcon(MavenSearchPluginIcon.RIGHT);
        this.setRightText("GradlePlugin");
    }

    public boolean supportUnfold() {
        this.update();
        return true;
    }

    public boolean supportFold() {
        this.update();
        return true;
    }

    public void setFold() {
        this.setFold(true); // 设置为：折叠
    }

    public void setUnfold(Runnable command) {
        this.setFold(false); // 设置为：展开
        if (this.childList.size() == 0) {
            this.setLeftIcon(MavenSearchPluginIcon.LEFT_WAITING); // 更改为：等待图标

            MavenSearchPlugin plugin = this.getSearch();
            GradlePlugin gradlePlugin = this.getResult();
            String groupId = "";
            String gradlePluginId = gradlePlugin.getId();

            SearchResult result = plugin.getDatabase().select(groupId, gradlePluginId);
            if (result == null || result.isExpire(plugin.getSettings().getExpireTimeMillis())) {
                plugin.async(new MavenJob("") {
                    public int execute() throws Exception {
                        SearchResult result = plugin.search(groupId, gradlePluginId);
                        addChild(result);
                        if (command != null) {
                            command.run();
                        }
                        return 0;
                    }
                }); // 后台搜索
                return;
            }

            this.addChild(result);
            if (command != null) {
                command.run();
            }
        }
    }

    public void addChild(SearchResult result) {
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_UNFOLD);
        if (result != null) {
            for (Object object : result.getList()) {
                this.childList.add(this.getSearch(), new SearchNavigationGradlePluginVersion((GradlePlugin) object));
            }
        }
    }

    public void update() {
        if (!this.isFold() && this.childList.size() > 0) {
            this.setLeftIcon(MavenSearchPluginIcon.LEFT_UNFOLD);
            return;
        }

        MavenSearchPlugin plugin = this.getSearch();
        GradlePlugin gradlePlugin = this.getResult();
        String groupId = "";
        String gradlePluginId = gradlePlugin.getId();

        SearchResult result = plugin.getDatabase().select(groupId, gradlePluginId);
        if (result != null && !result.isExpire(plugin.getSettings().getExpireTimeMillis())) {
            this.setLeftIcon(MavenSearchPluginIcon.LEFT_HAS_QUERY);
        }
    }

    public boolean supportMenu() {
        return false;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
    }
}
