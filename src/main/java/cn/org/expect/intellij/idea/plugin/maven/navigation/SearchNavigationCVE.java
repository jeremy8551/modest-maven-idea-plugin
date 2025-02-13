package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginIcon;
import cn.org.expect.intellij.idea.plugin.maven.SearchDisplay;
import cn.org.expect.intellij.idea.plugin.maven.menu.MenuItemAction;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.repository.cve.CveInfo;
import cn.org.expect.maven.search.SearchNavigation;
import com.intellij.ide.BrowserUtil;

public class SearchNavigationCVE extends AbstractSearchNavigation {

    public SearchNavigationCVE(CveInfo cve) {
        super(cve);
        this.setDepth(1);
        this.setPresentableText(cve.getName());
        this.setLocationString(cve.getDescription());
        this.setMiddleText("");
        this.setLeftIcon(MavenSearchPluginIcon.LEFT_CVE);
        this.setRightIcon(MavenSearchPluginIcon.RIGHT_CVE);
        this.setRightText("");
    }

    public boolean supportUnfold() {
        return false;
    }

    public boolean supportFold() {
        return false;
    }

    public boolean supportMenu() {
        return true;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
        MavenSearchPlugin plugin = this.getSearch();

        JMenuItem item = new JMenuItem(MavenMessage.get("maven.search.btn.navigation.open.url.text"));
        item.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) throws Exception {
                CveInfo cveInfo = navigation.getResult();
                BrowserUtil.browse(cveInfo.getHref());
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

    public void setUnfold(Runnable command) {
    }

    public void setFold() {
    }

    public void update() {
    }
}
