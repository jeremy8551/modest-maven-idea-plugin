package cn.org.expect.intellij.idea.plugin.maven.menu;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.settings.MavenPluginConfigurable;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.search.ArtifactSearchNotification;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.ShowSettingsUtil;

/**
 * 搜索输入框中弹出的菜单
 */
public class SearchFieldMenu extends AbstractMenu {

    private final JPopupMenu topMenu = new JPopupMenu();
    private final JMenuItem website = new JMenuItem(MavenMessage.get("maven.search.btn.open.website.text"));
    private final JMenuItem query = new JMenuItem(MavenMessage.get("maven.search.btn.refresh.query.text"));
    private final JMenuItem reset = new JMenuItem(MavenMessage.get("maven.search.btn.reset.search.text"));
    private final JMenuItem settings = new JMenuItem(MavenMessage.get("maven.search.btn.open.settings.text"));

    public SearchFieldMenu(MavenSearchPlugin plugin) {
        super(plugin);
        this.topMenu.add(query);
        this.topMenu.add(reset);
        this.topMenu.add(website);
        this.topMenu.add(settings);
        this.addAction(plugin);
    }

    protected void addAction(MavenSearchPlugin plugin) {
        // 访问官网
        website.addActionListener(e -> {
            String address = plugin.getRepository().getAddress();
            BrowserUtil.browse(address);
        });

        // 执行查询
        query.addActionListener(e -> {
            String pattern = plugin.getIdeaUI().getSearchField().getText();
            plugin.refresh(pattern);
            plugin.sendNotification(ArtifactSearchNotification.NORMAL, query.getText());
        });

        // 重置搜索
        reset.addActionListener(e -> {
            plugin.setProgress("");
            plugin.setStatusBar(null, "");
            plugin.getIdeaUI().getSearchField().setText("");
            plugin.getContext().setNavigationCollection(null);
            plugin.getContext().setSelectedNavigation(null);
            plugin.display();
            plugin.sendNotification(ArtifactSearchNotification.NORMAL, reset.getText());
        });

        // 打开配置
        settings.addActionListener(e -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                    plugin.getContext().getActionEvent().getProject(),
                    new MavenPluginConfigurable().getDisplayName() // 插件设置页面的显示名称
            );
        });
    }

    public void mousePressed(MouseEvent e) {
        MavenSearchPlugin plugin = this.getPlugin();
        JTextField searchField = plugin.getIdeaUI().getSearchField();
        if (plugin.isSelfTab() && e.getButton() == MouseEvent.BUTTON3) { // 输入框右键，弹出菜单
            Point location = searchField.getLocation();
            int x = location.x;
            int y = location.y;
            topMenu.show(searchField, x, y); // 在鼠标位置显示弹出菜单
        }
    }
}
