package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.maven.Artifact;

public class SearchNavigationArtifactDetail extends AbstractSearchNavigation {

    public SearchNavigationArtifactDetail(Artifact artifact, Icon rightIcon, String presentableText, String locationString, String rightText) {
        super(artifact);
        this.setDepth(3);
        this.setPresentableText(presentableText);
        this.setLocationString(locationString);
        this.setMiddleText("");
        this.setLeftIcon(null);
        this.setRightIcon(rightIcon);
        this.setRightText(rightText);
    }

    public boolean supportMenu() {
        return true;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
        MavenSearchPlugin plugin = this.getSearch();
        plugin.getResultMenu().displayDetailMenu(plugin, this, topMenu, selectedIndex);
    }

    public boolean supportUnfold() {
        return false;
    }

    public boolean supportFold() {
        return false;
    }

    public void setUnfold(Runnable command) {
    }

    public void setFold() {
    }

    public void update() {
    }
}
