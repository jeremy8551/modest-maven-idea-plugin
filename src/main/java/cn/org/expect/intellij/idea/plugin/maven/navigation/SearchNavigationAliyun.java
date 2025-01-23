package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginIcon;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.concurrent.ArtifactDownloadJob;

public class SearchNavigationAliyun extends AbstractSearchNavigation {

    public SearchNavigationAliyun(Artifact artifact) {
        super(artifact);
        this.setDepth(1);
        this.setPresentableText(artifact.getArtifactId());
        this.setLocationString(" " + artifact.getGroupId() + "  (" + artifact.getVersion() + ")");
        this.setMiddleText("");
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_ALIYUN);
        this.setRightIcon(MavenSearchPluginIcon.RIGHT_REMOTE);
        this.setRightText(artifact.getType() + " ");
    }

    public boolean supportUnfold() {
        this.update();
        return false;
    }

    public boolean supportFold() {
        this.update();
        return false;
    }

    public boolean supportMenu() {
        return true;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
        MavenSearchPlugin plugin = this.getSearch();
        plugin.getResultMenu().displayItemMenu(plugin, this, topMenu, selectedIndex, 30);
    }

    public void setUnfold(Runnable command) {
    }

    public void setFold() {
    }

    public void update() {
        Artifact artifact = this.getResult();
        MavenSearchPlugin search = this.getSearch();

        // 如果正在下载工件，则更新图标
        if (search.getService().isRunning(ArtifactDownloadJob.class, job -> job.getArtifact().equals(artifact))) { // 正在下载
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_DOWNLOAD);
            return;
        }

        // 如果工件已下载，则更新图标
        if (search.getLocalRepository().exists(artifact)) {
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_LOCAL);
        } else {
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_REMOTE);
        }
    }
}
