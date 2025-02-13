package cn.org.expect.intellij.idea.plugin.maven.navigation;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginContributor;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationList;
import cn.org.expect.util.CharsetName;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.IO;
import cn.org.expect.util.Numbers;
import cn.org.expect.util.StringUtils;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.TextWithIcon;
import com.intellij.util.ui.NamedColorUtil;
import com.intellij.util.ui.UIUtil;

public class SearchNavigationRenderer extends SearchEverywherePsiRenderer {
    private final static Log log = LogFactory.getLog(SearchNavigationRenderer.class);

    /** 二级导航栏：版本号的起始位置（定位菜单的显示位置） */
    public static int navigation1 = 30;

    public final static int LEFT_CELL_WITH = 150;

    public final static int RIGHT_CELL_WITH = 200;

    private final MavenSearchPluginContributor contributor;

    public SearchNavigationRenderer(MavenSearchPluginContributor contributor) {
        super(contributor);
        this.contributor = Ensure.notNull(contributor);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // 其他搜索类别的导航记录
        if (!(value instanceof SearchNavigation)) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

        // 导航栏
        SearchNavigation navigation = (SearchNavigation) value;

        // 第一层
        if (navigation.getDepth() == 1) {
            return this.render1(list, value, index, isSelected, cellHasFocus, navigation);
        }

        // 第二层
        if (navigation.getDepth() == 2) {
            return this.render2(list, value, index, isSelected, cellHasFocus, navigation);
        }

        // 第三层
        if (navigation.getDepth() == 3) {
            return this.render3(list, value, index, isSelected, cellHasFocus, navigation);
        }

        // 其他
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    private Component render1(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus, SearchNavigation navigation) {
        this.removeAll();

        Icon leftIcon = navigation.getLeftIcon();
        String leftText = StringUtils.trimBlank(navigation.getPresentableText());
        String smallText = this.format(navigation.getLocationString());
        String rightText = navigation.getRightText();
        Icon rightIcon = navigation.getRightIcon();

        SearchNavigationListCellRenderer leftRender = new SearchNavigationListCellRenderer(leftIcon, leftText, SimpleTextAttributes.STYLE_PLAIN, JBColor.BLACK, smallText, SimpleTextAttributes.STYLE_SMALLER, JBColor.GRAY);
        Component leftComponent = leftRender.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // 左侧的文本，无图标
        JBPanel left = new JBPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // 设置布局为 FlowLayout
        left.add(leftComponent);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        left.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        this.add(left, BorderLayout.WEST);

        // 中间文本，无图标
        JBPanel middle = new JBPanel();
        middle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        middle.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        this.add(middle, BorderLayout.CENTER);

        // 右侧的图标与文本
        JBLabel right = new JBLabel(rightText, rightIcon, SwingConstants.RIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        right.setHorizontalTextPosition(SwingConstants.LEFT);
        right.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        right.setPreferredSize(new Dimension(SearchNavigationRenderer.RIGHT_CELL_WITH, right.getHeight()));
        this.add(right, BorderLayout.EAST);

        this.myRightComponentWidth = right.getPreferredSize().width;
        this.myRightComponentWidth += middle.getPreferredSize().width;

        Color color = isSelected ? UIUtil.getListSelectionBackground(true) : left.getBackground();
        this.setBackground(color);
        left.setBackground(color);
        right.setBackground(color);
        middle.setBackground(color);
        return this;
    }

    private Component render2(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus, SearchNavigation navigation) {
        this.removeAll();

        Icon leftIcon = navigation.getLeftIcon();
        String leftText = StringUtils.trimBlank(navigation.getPresentableText());
        String middleText = navigation.getMiddleText();
        String rightText = this.parseJDKVersion(navigation.getResult());
        Icon rightIcon = navigation.getRightIcon();

        SearchNavigationListCellRenderer leftRender = new SearchNavigationListCellRenderer(leftIcon, leftText, SimpleTextAttributes.STYLE_PLAIN, JBColor.BLACK);
        Component leftComponent = leftRender.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        SearchNavigationListCellRenderer middleRender = new SearchNavigationListCellRenderer(null, middleText, SimpleTextAttributes.STYLE_SMALLER, JBColor.GRAY);
        Component middleComponent = middleRender.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        middleComponent.setPreferredSize(new Dimension(100, middleComponent.getHeight()));

        // 左侧的文本，无图标
        JBPanel left = new JBPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // 设置布局为 FlowLayout
        left.add(leftComponent);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        left.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        left.setPreferredSize(new Dimension(SearchNavigationRenderer.LEFT_CELL_WITH, left.getHeight()));
        this.add(left, BorderLayout.WEST);

        // 三级导航栏：起始位置（定位菜单的显示位置）
        int menuPosition = leftRender.computeWith() + middleRender.computeWith();
        SearchNavigationList childNavigation = navigation.getChildNavigation();
        for (int i = 0; i < childNavigation.size(); i++) {
            SearchNavigation child = childNavigation.get(i);
            child.setMenuPosition(menuPosition);
        }

        // 中间文本，无图标
        JBPanel middle = new JBPanel();
        middle.setLayout(new BorderLayout());
        middle.add(middleComponent, BorderLayout.WEST);
        middle.add(new JBLabel(""), BorderLayout.CENTER);
        middle.add(new JBLabel(""), BorderLayout.EAST);
        middle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        middle.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        this.add(middle, BorderLayout.CENTER);

        // 右侧的图标与文本
        JBLabel right = new JBLabel(rightText, rightIcon, SwingConstants.RIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        right.setHorizontalTextPosition(SwingConstants.LEFT);
        right.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        right.setPreferredSize(new Dimension(SearchNavigationRenderer.RIGHT_CELL_WITH, right.getHeight()));
        this.add(right, BorderLayout.EAST);

        this.myRightComponentWidth = right.getPreferredSize().width;
        this.myRightComponentWidth += middle.getPreferredSize().width;

        Color color = isSelected ? UIUtil.getListSelectionBackground(true) : left.getBackground();
        this.setBackground(color);
        left.setBackground(color);
        right.setBackground(color);
        middle.setBackground(color);
        return this;
    }

    private Component render3(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus, SearchNavigation navigation) {
        this.removeAll();

        String leftText = navigation.getPresentableText();
        String middleText = this.format(navigation.getLocationString());
        String rightText = navigation.getRightText();
        Icon rightIcon = navigation.getRightIcon();

        SearchNavigationListCellRenderer leftRender = new SearchNavigationListCellRenderer(null, leftText, SimpleTextAttributes.STYLE_PLAIN, JBColor.BLACK);
        Component leftComponent = leftRender.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        SearchNavigationListCellRenderer middleRender = new SearchNavigationListCellRenderer(null, middleText, SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY);
        Component middleComponent = middleRender.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // 左侧的文本，无图标
        JBPanel left = new JBPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // 设置布局为 FlowLayout
        left.add(leftComponent);
        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        left.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        left.setPreferredSize(new Dimension(SearchNavigationRenderer.LEFT_CELL_WITH, left.getHeight()));
        this.add(left, BorderLayout.WEST);

        // 中间文本，无图标
        JBPanel middle = new JBPanel();
        middle.setLayout(new BorderLayout());
        middle.add(middleComponent, BorderLayout.WEST);
        middle.add(new JBLabel(""), BorderLayout.CENTER);
        middle.add(new JBLabel(""), BorderLayout.EAST);
        middle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        middle.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        this.add(middle, BorderLayout.CENTER);

        navigation1 = leftRender.computeWith();

        // 右侧的图标与文本
        JBLabel right = new JBLabel(rightText, rightIcon, SwingConstants.RIGHT);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIUtil.getListCellHPadding()));
        right.setHorizontalTextPosition(SwingConstants.LEFT);
        right.setForeground(isSelected ? NamedColorUtil.getListSelectionForeground(true) : NamedColorUtil.getInactiveTextColor());
        right.setPreferredSize(new Dimension(SearchNavigationRenderer.RIGHT_CELL_WITH, right.getHeight()));
        this.add(right, BorderLayout.EAST);

        this.myRightComponentWidth = right.getPreferredSize().width;
        this.myRightComponentWidth += middle.getPreferredSize().width;

        Color color = isSelected ? UIUtil.getListSelectionBackground(true) : left.getBackground();
        this.setBackground(color);
        left.setBackground(color);
        right.setBackground(color);
        middle.setBackground(color);
        return this;
    }

    /**
     * 渲染搜索结果右侧的图标和文字
     *
     * @param value 记录
     * @return 图标与文字信息
     */
    public TextWithIcon getItemLocation(Object value) {
        if (value instanceof SearchNavigation) {
            SearchNavigation navigation = (SearchNavigation) value;
            return new TextWithIcon(navigation.getRightText(), navigation.getRightIcon());
        } else {
            return super.getItemLocation(value);
        }
    }

    protected String parseJDKVersion(Object result) {
        if (!(result instanceof Artifact)) {
            return "";
        }

        Artifact artifact = (Artifact) result;
        File file = this.contributor.getPlugin().getLocalRepository().getJarfile(artifact);
        if (file != null && file.exists() && file.isFile() && file.length() > 0) {
            JarFile jarfile = null;
            try {
                jarfile = new JarFile(file);
                JarEntry entry = jarfile.stream().filter(e -> e.getName().endsWith(".class")).findFirst().orElse(null);
                if (entry != null) {
                    String prefix = "Java ";
                    try (InputStream in = jarfile.getInputStream(entry)) {
                        in.skip(6); // Skip the first 6 bytes
                        int major = in.read() << 8 | in.read();
                        switch (major) {
                            case 45:
                                return prefix + "1.1";
                            case 46:
                                return prefix + "1.2";
                            case 47:
                                return prefix + "1.3";
                            case 48:
                                return prefix + "1.4";
                            case 49:
                                return prefix + "5";
                            case 50:
                                return prefix + "6";
                            case 51:
                                return prefix + "7";
                            case 52:
                                return prefix + "8";
                            case 53:
                                return prefix + "9";
                            case 54:
                                return prefix + "10";
                            case 55:
                                return prefix + "11";
                            case 56:
                                return prefix + "12";
                            case 57:
                                return prefix + "13";
                            case 58:
                                return prefix + "14";
                            case 59:
                                return prefix + "15";
                            case 60:
                                return prefix + "16";
                            case 61:
                                return prefix + "17";
                            case 62:
                                return prefix + "18";
                            case 63:
                                return prefix + "19";
                            case 64:
                                return prefix + "20";
                            case 65:
                                return prefix + "21";
                            case 66:
                                return prefix + "22";
                            case 67:
                                return prefix + "23";
                            case 68:
                                return prefix + "24";
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage());
            } finally {
                IO.closeQuietly(jarfile);
            }
        }
        return "";
    }

    /**
     * 格式化字符串
     *
     * @param str 字符串
     * @return 新字符串
     */
    public String format(String str) {
        int width = this.contributor.getPlugin().getIdeaUI().getSearchEverywhereUI().getWidth();
        int max = width / 8;
        if (str.length() > max) {
            return StringUtils.left(str, Numbers.max(max, 100), CharsetName.UTF_8) + " ..";
        } else {
            return str;
        }
    }
}
