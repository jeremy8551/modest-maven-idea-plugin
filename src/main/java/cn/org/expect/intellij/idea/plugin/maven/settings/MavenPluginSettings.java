package cn.org.expect.intellij.idea.plugin.maven.settings;

import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.search.SearchSettings;

/**
 * Idea搜索接口的配置信息
 */
public interface MavenPluginSettings extends SearchSettings {

    /** 搜索接口配置信息存储的文件名 */
    String SETTINGS_TABLE_NAME = "MAVEN_SEARCH_PLUGIN_SETTINGS.json";

    /**
     * 设置插件ID
     *
     * @param id 插件ID
     */
    void setId(String id);

    /**
     * 返回插件ID
     *
     * @return 插件ID
     */
    String getId();

    /**
     * 如果选中文本是 groupId:artifactId:version 时，是否自动切换tab
     *
     * @return 返回true表示启动切换，false表示不自动切换
     */
    boolean isAutoSwitchTab();

    /**
     * 设置是否自动切换tab
     *
     * @param autoSwitchTab 返回true表示启动切换，false表示不自动切换
     */
    void setAutoSwitchTab(boolean autoSwitchTab);

    /**
     * 标签页所在的位置
     *
     * @return 位置信息，从0开始
     */
    int getTabIndex();

    /**
     * 设置标签页所在的位置，从0开始
     *
     * @param index 位置信息，从0开始
     */
    void setTabIndex(int index);

    /**
     * 是否显示选项卡
     *
     * @return 返回true表示显示选项卡，false表示不显示选项卡
     */
    boolean isTabVisible();

    /**
     * 设置是否显示选项卡
     *
     * @param value 返回true表示显示选项卡，false表示不显示选项卡
     */
    void setTabVisible(boolean value);

    /**
     * 是否支持在 All 选项卡中执行搜索
     *
     * @return true表示支持在 All 选项卡中执行搜索
     */
    boolean isSearchInAllTab();

    /**
     * 设置是否支持在 All 选项卡中执行搜索
     *
     * @param searchInAllTab true表示支持在 All 选项卡中执行搜索
     */
    void setSearchInAllTab(boolean searchInAllTab);

    /**
     * 查询结果的排序权重
     *
     * @return 排序权重
     */
    int getNavigationPriority();

    /**
     * 查询结果的排序权重
     *
     * @param navigationPriority 排序权重
     */
    void setNavigationPriority(int navigationPriority);

    /**
     * 将配置信息持久化到文件
     *
     * @param filename 文件名
     */
    void save(String filename);

    /**
     * 加载配置文件
     *
     * @param filename 文件名
     */
    void load(String filename);

    /**
     * 返回选项卡名
     *
     * @return 选项卡名
     */
    default String getTabName() {
        return MavenMessage.get("maven.search.tab.name");
    }

    /**
     * 返回All选项卡的名
     *
     * @return All选项卡的名
     */
    default String getAllTabName() {
        return "All";
    }

    /**
     * 持久化配置信息
     */
    default void save() {
        this.save(SETTINGS_TABLE_NAME);
    }

    /**
     * 加载配置信息
     */
    default void load() {
        this.load(SETTINGS_TABLE_NAME);
    }

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    default MavenPluginSettings copy() {
        DefaultMavenPluginSettings copy = new DefaultMavenPluginSettings();
        copy.setId(this.getId());
        copy.setName(this.getName());
        copy.setWorkHome(this.getWorkHome());
        copy.setSearchInAllTab(this.isSearchInAllTab());
        copy.setNavigationPriority(this.getNavigationPriority());
        copy.setTabVisible(this.isTabVisible());
        copy.setTabIndex(this.getTabIndex());
        copy.setExpireTimeMillis(this.getExpireTimeMillis());
        copy.setAutoSwitchTab(this.isAutoSwitchTab());
        copy.setRepositoryId(this.getRepositoryId());
        copy.setInputIntervalTime(this.getInputIntervalTime());
        copy.setDownloadWay(this.getDownloadWay());
        copy.setUseCache(this.isUseCache());
        copy.setDownScriptRepository(this.isDownScriptRepository());
        return copy;
    }

    /**
     * 合并配置信息
     *
     * @param settings 配置信息
     */
    default MavenPluginSettings merge(MavenPluginSettings settings) {
        this.setSearchInAllTab(settings.isSearchInAllTab());
        this.setNavigationPriority(settings.getNavigationPriority());
        this.setTabVisible(settings.isTabVisible());
        this.setTabIndex(settings.getTabIndex());
        this.setExpireTimeMillis(settings.getExpireTimeMillis());
        this.setAutoSwitchTab(settings.isAutoSwitchTab());
        this.setRepositoryId(settings.getRepositoryId());
        this.setInputIntervalTime(settings.getInputIntervalTime());
        this.setDownloadWay(settings.getDownloadWay());
        this.setUseCache(settings.isUseCache());
        this.setDownScriptRepository(settings.isDownScriptRepository());
        return this;
    }

    /**
     * 判断配置信息是否相等
     *
     * @param settings 配置信息
     * @return 返回true表示不同
     */
    default boolean isEquals(MavenPluginSettings settings) {
        return settings != null //
                && settings.isSearchInAllTab() == this.isSearchInAllTab() //
                && settings.getNavigationPriority() == this.getNavigationPriority() //
                && settings.isTabVisible() == this.isTabVisible() //
                && settings.getTabIndex() == this.getTabIndex() //
                && settings.getExpireTimeMillis() == this.getExpireTimeMillis() //
                && settings.isAutoSwitchTab() == this.isAutoSwitchTab() //
                && settings.getRepositoryId().equals(this.getRepositoryId()) //
                && settings.getInputIntervalTime() == this.getInputIntervalTime() //
                && settings.getDownloadWay().equals(this.getDownloadWay()) //
                && settings.isUseCache() == this.isUseCache() //
                && settings.isDownScriptRepository() == this.isDownScriptRepository() //
                ;
    }
}
