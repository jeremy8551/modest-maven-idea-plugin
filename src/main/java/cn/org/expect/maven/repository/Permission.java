package cn.org.expect.maven.repository;

public interface Permission {

    /**
     * 是否支持精确搜索
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportExtraSearch();

    /**
     * 是否支持在中央仓库中浏览
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportOpenInCentralRepository();

    /**
     * 是否支持下载工件
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportDownload();

    /**
     * 是否支持删除工件
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportDelete();

    /**
     * 是否支持在本地操作系统上使用文件系统打开
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportOpenInFileSystem();

    /**
     * 是否支持复制Maven依赖
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportCopyMavenDependency();

    /**
     * 是否支持复制Gradle依赖
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportCopyGradleDependency();

    /**
     * 是否支持打开 POM 文件
     *
     * @return 返回 true 表示支持，false 表示不支持
     */
    boolean supportOpenPomFile();
}
