package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginIcon;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.concurrent.MavenJob;
import cn.org.expect.maven.repository.SearchResult;

public class SearchNavigationArtifact extends AbstractSearchNavigation {

    public SearchNavigationArtifact(Artifact artifact) {
        super(artifact);
        this.setDepth(1);
        this.setPresentableText(artifact.getArtifactId());
        this.setLocationString(" " + artifact.getGroupId());
        this.setMiddleText("");
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_FOLD);
        this.setRightIcon(MavenSearchPluginIcon.RIGHT);
        this.setRightText(artifact.getType() + " ");
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

            Artifact artifact = this.getResult();
            MavenSearchPlugin plugin = this.getSearch();
            SearchResult result = plugin.getDatabase().select(artifact.getGroupId(), artifact.getArtifactId());
            if (result != null && !result.isExpire(plugin.getSettings().getExpireTimeMillis())) {
                this.addChildList(plugin, result);
                if (command != null) {
                    command.run();
                }
                return;
            }

            // 后台搜索
            plugin.async(new MavenJob("") {
                public int execute() throws Exception {
                    SearchResult result = plugin.search(artifact.getGroupId(), artifact.getArtifactId());
                    addChildList(plugin, result);
                    if (command != null) {
                        command.run();
                    }
                    return 0;
                }
            });
        }
    }

    /**
     * 如果正在执行精确查询，则更新图标
     */
    public void update() {
        if (!this.isFold() && this.childList.size() > 0) {
            this.setLeftIcon(MavenSearchPluginIcon.LEFT_UNFOLD);
            return;
        }

        Artifact artifact = this.getResult();
        MavenSearchPlugin plugin = this.getSearch();
        SearchResult result = plugin.getDatabase().select(artifact.getGroupId(), artifact.getArtifactId());
        if (result != null && !result.isExpire(plugin.getSettings().getExpireTimeMillis())) {
            this.setLeftIcon(MavenSearchPluginIcon.LEFT_HAS_QUERY);
        }
    }

    private void addChildList(MavenSearchPlugin plugin, SearchResult result) {
        if (result != null) {
            for (Object object : result.getList()) {
                SearchNavigationArtifactVersion navigation = new SearchNavigationArtifactVersion((Artifact) object);
                childList.add(plugin, navigation);
            }
        }
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_UNFOLD);
    }

    public boolean supportMenu() {
        return false;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
    }
}
