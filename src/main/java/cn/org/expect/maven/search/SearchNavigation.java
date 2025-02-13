package cn.org.expect.maven.search;

import javax.swing.*;

/**
 * 搜索结果的导航记录
 */
public interface SearchNavigation {

    /**
     * 导航记录名（唯一）
     *
     * @return 导航记录名
     */
    String getName();

    /**
     * 返回搜索结果
     *
     * @param <T> 搜索结果
     * @return 搜索结果
     */
    <T> T getResult();

    /**
     * 层级深度
     *
     * @return 层级深度，从1开始
     */
    int getDepth();

    /**
     * 设置层级深度
     *
     * @param depth 层级深度，从1开始
     */
    void setDepth(int depth);

    /**
     * 设置左侧图标
     *
     * @param icon 图标
     */
    void setLeftIcon(Icon icon);

    /**
     * 返回左侧图标
     *
     * @return 图标
     */
    Icon getLeftIcon();

    /**
     * 设置左侧文本
     *
     * @param text 文本信息
     */
    void setPresentableText(String text);

    /**
     * 返回左侧文本
     *
     * @return 文本信息
     */
    String getPresentableText();

    /**
     * 返回左侧（小字）文本
     *
     * @param text 文本信息
     */
    void setLocationString(String text);

    /**
     * 设置左侧（小字）文本
     *
     * @return 文本信息
     */
    String getLocationString();

    /**
     * 返回中间显示的文本
     *
     * @return 文本信息
     */
    String getMiddleText();

    /**
     * 设置中间显示的文本
     *
     * @param text 文本信息
     */
    void setMiddleText(String text);

    /**
     * 设置右侧文本
     *
     * @param text 文本信息
     */
    void setRightText(String text);

    /**
     * 返回右侧文本
     *
     * @return 文本信息
     */
    String getRightText();

    /**
     * 设置导航记录右侧的图标
     *
     * @param icon 图标
     */
    void setRightIcon(Icon icon);

    /**
     * 返回导航记录右侧的图标与文本
     *
     * @return 图标与文本
     */
    Icon getRightIcon();

    /**
     * 是否支持折叠操作
     *
     * @return 返回true表示支持，false表示不支持
     */
    boolean supportUnfold();

    /**
     * 是否支持展开操作
     *
     * @return 返回true表示支持，false表示不支持
     */
    boolean supportFold();

    /**
     * 判断是否折叠
     *
     * @return 返回true表示折叠，false表示展开
     */
    boolean isFold();

    /**
     * 设置展开
     */
    void setUnfold(Runnable command);

    /**
     * 设置折叠
     */
    void setFold();

    /**
     * 更新状态
     */
    void update();

    /**
     * 返回子导航记录集合
     *
     * @return 导航记录集合
     */
    SearchNavigationList getChildNavigation();

    /**
     * 删除所有子导航记录
     */
    void clearChildNavigation();

    /**
     * 是否支持在导航记录上显示菜单
     *
     * @return 返回true表示支持，false表示不支持
     */
    boolean supportMenu();

    /**
     * 在弹出的菜单上添加子菜单
     *
     * @param topMenu       弹出的菜单（添加子菜单）
     * @param selectedIndex 导航记录在搜索结果的位置信息，从 0 开始
     */
    void displayMenu(JPopupMenu topMenu, int selectedIndex);

    int getMenuPosition();

    void setMenuPosition(int menuPosition);
}
