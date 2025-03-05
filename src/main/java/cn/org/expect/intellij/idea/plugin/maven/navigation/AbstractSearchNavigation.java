package cn.org.expect.intellij.idea.plugin.maven.navigation;

import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
 import cn.org.expect.maven.search.ArtifactSearchAware;
import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationList;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.StringUtils;
import cn.org.expect.util.UniqueSequenceGenerator;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;

public abstract class AbstractSearchNavigation implements SearchNavigation, NavigationItem, ItemPresentation, ArtifactSearchAware {

    /** 序号生成器 */
    protected final static UniqueSequenceGenerator UNIQUE = new UniqueSequenceGenerator("Navigation-{}", 1);

    /** 序号 */
    protected final String name;

    /** 子节点 */
    protected final SearchNavigationList childList;

    /** 搜索接口 */
    private MavenSearchPlugin plugin;

    /** 层级 */
    private int depth;

    /** 工件 */
    private final Object result;

    /** 左侧图标 */
    private volatile Icon leftIcon;

    /** 右侧图标 */
    private volatile Icon rightIcon;

    /** true表示折叠，false表示展开 */
    private volatile boolean fold;

    /** 左侧文本 */
    private String leftText;

    /** 左侧小字的文本 */
    private String smallString;

    /** 中间的文本 */
    private String middleText;

    /** 右侧文本 */
    private String rightText;

    private int menuPosition;

    public AbstractSearchNavigation(Object result) {
        this.childList = new NavigationListImpl();
        this.name = UNIQUE.nextString();
        this.result = result;
        this.fold = true;
        this.depth = 1;
    }

    public String getName() {
        return this.name;
    }

    public SearchNavigationList getChildNavigation() {
        return this.childList;
    }

    public void clearChildNavigation() {
        this.childList.clear();
    }

    /**
     * 左侧图标
     *
     * @param unused Used to mean if open/close icons for tree renderer. No longer in use. The parameter is only there for API compatibility reason.
     * @return 图标
     */
    public Icon getIcon(boolean unused) {
        return this.leftIcon;
    }

    /**
     * 返回导航记录对象
     *
     * @return 导航记录对象
     */
    public ItemPresentation getPresentation() {
        return this;
    }

    /**
     * 左键点击导航记录执行的操作
     *
     * @param requestFocus {@code true} if focus requesting is necessary
     */
    public void navigate(boolean requestFocus) {
    }

    public void setSearch(Search plugin) {
        for (int i = 0; i < this.childList.size(); i++) {
            SearchNavigation navigation = this.childList.get(i);
            plugin.aware(navigation);
        }
        this.plugin = (MavenSearchPlugin) plugin;
    }

    public MavenSearchPlugin getSearch() {
        return plugin;
    }

    @SuppressWarnings("unchecked")
    public <T> T getResult() {
        return (T) this.result;
    }

    public void setLeftIcon(Icon icon) {
        this.leftIcon = icon;
    }

    public Icon getLeftIcon() {
        return leftIcon;
    }

    public void setRightIcon(Icon icon) {
        this.rightIcon = icon;
    }

    public Icon getRightIcon() {
        return this.rightIcon;
    }

    public void setRightText(String text) {
        this.rightText = StringUtils.coalesce(text, "");
    }

    public String getRightText() {
        return rightText;
    }

    public void setPresentableText(String text) {
        this.leftText = StringUtils.coalesce(text, "");
    }

    public String getPresentableText() {
        return this.leftText;
    }

    public void setLocationString(String text) {
        this.smallString = StringUtils.coalesce(text, "");
    }

    public String getLocationString() {
        return this.smallString;
    }

    public String getMiddleText() {
        return middleText;
    }

    public void setMiddleText(String middleText) {
        this.middleText = middleText;
    }

    public boolean isFold() {
        return this.fold;
    }

    /**
     * 设置是否折叠
     *
     * @param fold true表示折叠 false表示展开
     */
    public void setFold(boolean fold) {
        this.fold = fold;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = Ensure.fromOne(depth);
    }

    public int getMenuPosition() {
        return menuPosition;
    }

    public void setMenuPosition(int menuPosition) {
        this.menuPosition = menuPosition;
    }

    public boolean equals(Object object) {
        return object != null && object.getClass().equals(this.getClass()) && ((AbstractSearchNavigation) object).name.equals(this.name);
    }
}
