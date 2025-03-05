package cn.org.expect.maven.search;

import java.io.File;

/**
 * 搜索配置信息
 */
public interface SearchSettings {

    /**
     * 设置插件名
     *
     * @param name 插件名
     */
    void setName(String name);

    /**
     * 返回插件名
     *
     * @return 插件名
     */
    String getName();

    /**
     * 设置工作目录
     *
     * @param file 工作目录
     */
    void setWorkHome(File file);

    /**
     * 返回工作目录
     *
     * @return 工作目录
     */
    File getWorkHome();

    /**
     * 返回连续输入的间隔，单位毫秒
     *
     * @return 毫秒数
     */
    long getInputIntervalTime();

    /**
     * 设置连续输入的间隔，单位毫秒
     *
     * @param millis 毫秒数
     */
    void setInputIntervalTime(long millis);

    /**
     * 返回仓库ID
     *
     * @return 仓库ID
     */
    String getRepositoryId();

    /**
     * 仓库ID
     *
     * @param repositoryId 仓库ID
     */
    void setRepositoryId(String repositoryId);

    /**
     * 返回查询结果的过期时间，搜索结果过期后自动失效
     *
     * @return 毫秒数
     */
    long getExpireTimeMillis();

    /**
     * 失效时间（单位毫秒）
     *
     * @param millis 毫秒数
     */
    void setExpireTimeMillis(long millis);

    /**
     * 下载工件的方式
     *
     * @return 下载方式
     */
    String getDownloadWay();

    /**
     * 设置下载工件的方式
     *
     * @param downSource 下载方式
     */
    void setDownloadWay(String downSource);

    /**
     * （下载脚本文件的）代码仓库名
     *
     * @return 代码仓库名
     */
    String getScriptRepositoryName();

    /**
     * 设置（下载脚本文件的）代码仓库名
     *
     * @param scriptRepositoryName 代码仓库名
     */
    void setScriptRepositoryName(String scriptRepositoryName);

    /**
     * 判断是否使用缓存
     *
     * @return true表示使用，false表示不使用
     */
    boolean isUseCache();

    /**
     * 设置是否使用缓存
     *
     * @param useCache true表示使用，false表示不使用
     */
    void setUseCache(boolean useCache);

    /**
     * 判断是否下载脚本文件
     *
     * @return true表示下载，false表示不下载
     */
    boolean isDownScriptRepository();

    /**
     * 设置是否下载脚本文件
     *
     * @param downScriptRepository true表示下载，false表示不下载
     */
    void setDownScriptRepository(boolean downScriptRepository);
}
