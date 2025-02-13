package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.SearchDisplay;
import cn.org.expect.intellij.idea.plugin.maven.menu.MenuItemAction;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.repository.gradle.GradlePlugin;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.util.Dates;
import cn.org.expect.util.StringUtils;
import com.intellij.ide.BrowserUtil;

public class SearchNavigationGradlePluginVersion extends AbstractSearchNavigation {

    public SearchNavigationGradlePluginVersion(GradlePlugin gradlePlugin) {
        super(gradlePlugin);
        this.setDepth(2);
        this.setPresentableText(gradlePlugin.getVersion());
        this.setLocationString(StringUtils.coalesce(Dates.format10(gradlePlugin.getDate()), ""));
        this.setMiddleText(gradlePlugin.getDate() == null ? "" : Dates.format10(gradlePlugin.getDate()));
        this.setLeftIcon(null);
        this.setRightIcon(null);
        this.setRightText("");
    }

    public boolean supportMenu() {
        return true;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
        MavenSearchPlugin plugin = this.getSearch();
        JMenuItem item = new JMenuItem(MavenMessage.get("maven.search.btn.navigation.open.url.text"));
        item.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) throws Exception {
                GradlePlugin gradlePlugin = navigation.getResult();
                BrowserUtil.browse(gradlePlugin.getHref());
            }
        });

        topMenu.removeAll();
        topMenu.add(item);

        // 在鼠标位置显示弹出菜单
        SearchDisplay display = plugin.getIdeaUI().getDisplay();
        int x = display.getX() + 30;
        int y = display.getCellBounds(0, selectedIndex).height; // JList 中第一行到选中导航记录之间的高度
        display.showMenu(topMenu, x, y);
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
