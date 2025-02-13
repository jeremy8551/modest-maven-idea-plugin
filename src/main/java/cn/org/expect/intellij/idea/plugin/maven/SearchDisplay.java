package cn.org.expect.intellij.idea.plugin.maven;

import java.awt.*;
import java.awt.event.MouseListener;
import javax.swing.*;

import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.util.StringComparator;
import com.intellij.ide.actions.searcheverywhere.SearchListModel;
import com.intellij.ui.components.JBList;

public class SearchDisplay extends SearchDisplayModel {

    protected JBList<Object> list;

    public SearchDisplay(JBList<Object> list, SearchListModel model) {
        super(model);
        this.list = list;
    }

    public void addMouseListener(MouseListener listener) {
        this.list.addMouseListener(listener);
    }

    public int locationToIndex(Point location) {
        return this.list.locationToIndex(location);
    }

    /**
     * 选中导航记录
     *
     * @param index 位置信息，从0开始
     */
    public void setSelectedIndex(int index) {
        this.list.setSelectedIndex(index);
    }

    /**
     * 返回 JList 的横坐标
     *
     * @return 横坐标
     */
    public int getX() {
        return this.list.getX();
    }

    public Rectangle getCellBounds(int index0, int index1) {
        return this.list.getCellBounds(index0, index1);
    }

    /**
     * 显示菜单
     *
     * @param menu 菜单
     * @param x    横坐标
     * @param y    纵坐标
     */
    public void showMenu(JPopupMenu menu, int x, int y) {
        menu.show(this.list, x, y);
    }

    /**
     * 设置在等待时显示的进度文本
     *
     * @param message 文本
     */
    public void setProgress(String message) {
        try {
            this.list.setEmptyText(message);
            this.list.repaint();
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 重新渲染搜索结果
     */
    public void paint() {
        try {
            this.list.revalidate();
            this.list.repaint();
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 选中记录
     */
    public void select(SearchNavigation selected) {
        int selectedIndex = -1;
        if (selected != null) {
            SearchListModel listModel = this.model;
            for (int i = listModel.getSize() - 1; i >= 0; i--) {
                Object object = listModel.getElementAt(i);
                if (object instanceof SearchNavigation) {
                    SearchNavigation navigation = (SearchNavigation) object;
                    if (StringComparator.compareTo(navigation.getName(), selected.getName()) == 0) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
        }
        this.select(selectedIndex);
    }

    protected void select(int index) {
        try {
            if (index == -1) {
                this.list.clearSelection();
            } else {
                this.list.setSelectedIndex(index); // 选中
                this.list.ensureIndexIsVisible(index); // 设置 JList 显示 index 位置
            }
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 设置JList的显示区域
     *
     * @param visibleRect 显示区域
     */
    public void setVisibleRange(Rectangle visibleRect) {
        if (visibleRect == null) {
            return;
        }

        int index = -1;
        for (int i = 0; i < this.model.getSize(); i++) {
            Rectangle itemRect = list.getCellBounds(i, i); // 获取第 i 项的矩形区域
            if (itemRect != null && itemRect.intersects(visibleRect)) {
                index = i;
            }
        }

        if (index != -1) {
            this.list.ensureIndexIsVisible(index); // 设置 JList 显示 index 位置
        }
    }

    /**
     * 返回JList的显示区域
     *
     * @return 显示区域
     */
    public Rectangle getVisibleRect() {
        return this.list.getVisibleRect();
    }

    /**
     * 显示导航记录的菜单
     *
     * @param topMenu       根菜单
     * @param selectedIndex 选中导航记录的位置，从0开始
     * @param offset        菜单在横坐标上显示的偏移位置
     */
    public void displayMenu(JPopupMenu topMenu, int selectedIndex, int offset) {
        // 每行之间的间隔
        int space = 30;
        if (this.size() >= 2) {
            Point point0 = this.list.indexToLocation(0);
            Point point1 = this.list.indexToLocation(1);
            space = point1.y - point0.y;
        }

        int next = selectedIndex + 1;
        boolean hasNext = next < this.list.getModel().getSize();
        Point point = this.list.indexToLocation(hasNext ? next : selectedIndex);
        int x = point.x + offset;
        int y = point.y + (hasNext ? 0 : space);
        this.showMenu(topMenu, x, y);
    }
}
