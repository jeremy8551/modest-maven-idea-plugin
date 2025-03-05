package cn.org.expect.maven.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.MavenOption;
import cn.org.expect.maven.concurrent.MavenService;
import cn.org.expect.maven.pom.PomRepository;
import cn.org.expect.maven.repository.Repository;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.DefaultSearchResult;
import cn.org.expect.maven.repository.local.LocalRepository;
import cn.org.expect.maven.repository.local.LocalRepositorySettings;
import cn.org.expect.util.StringUtils;

/**
 * 搜索结果
 */
public interface Search {

    /**
     * 返回配置信息
     *
     * @return 配置信息
     */
    SearchSettings getSettings();

    /**
     * 返回上下文信息
     *
     * @return 上下文信息
     */
    SearchContext getContext();

    /**
     * 异步执行模糊搜索
     *
     * @param pattern 字符串
     */
    void asyncSearch(String pattern);

    /**
     * 异步执行精确搜索
     *
     * @param groupId    域名
     * @param artifactId 工件名
     */
    void asyncSearch(String groupId, String artifactId);

    /**
     * 执行模糊搜索
     *
     * @param pattern 字符串
     * @return 搜索结果
     * @throws Exception 发生错误
     */
    default SearchResult search(String pattern) throws Exception {
        RepositoryDatabase database = this.getDatabase();
        SearchSettings settings = this.getSettings();

        SearchResult result = database.select(pattern);
        if (result == null || result.isExpire(settings.getExpireTimeMillis())) {
            result = this.getRepository().query(pattern, 1);
            if (result != null) {
                database.insert(pattern, result);
            }
        }
        return result;
    }

    /**
     * 执行精确搜索
     *
     * @param groupId    域名
     * @param artifactId 工件名
     * @return 搜索结果
     * @throws Exception 发生错误
     */
    default SearchResult search(String groupId, String artifactId) throws Exception {
        RepositoryDatabase database = this.getDatabase();
        SearchSettings settings = this.getSettings();

        SearchResult result = database.select(groupId, artifactId);
        if (result == null || result.isExpire(settings.getExpireTimeMillis())) {
            result = this.getRepository().query(groupId, artifactId);
            if (result != null) {
                database.insert(groupId, artifactId, result);
            }
        }
        return result;
    }

    /**
     * 根据字符串格式，执行搜索
     *
     * @param str 字符串
     * @return 搜索结果
     * @throws Exception 发生错误
     */
    default SearchResult execute(String str) throws Exception {
        String pattern = StringUtils.trimBlank(str);
        RepositoryDatabase database = this.getDatabase();
        Repository repository = this.getRepository();

        // 精确搜索
        if (repository.getPermission().supportExtraSearch() && this.getPattern().isExtra(pattern)) {
            String[] array = StringUtils.trimBlank(StringUtils.split(pattern, ':'));
            String groupId = array[0];
            String artifactId = array[1];

            // 精确搜索结果
            SearchResult result = this.search(groupId, artifactId);
            if (result == null) {
                return null;
            }

            // 将精确搜索结果转为模糊搜索结果
            List<Object> list = new ArrayList<>();
            if (!result.getList().isEmpty()) {
                list.add(result.getList().get(0));
            }

            SearchResult searchResult = new DefaultSearchResult(repository.getClass().getName(), result.getType(), list, 0, list.size(), System.currentTimeMillis(), false);
            database.insert(pattern, searchResult);
            return searchResult;
        } else {
            return this.search(pattern); // 模糊搜索
        }
    }

    /**
     * 将文本信息复制到剪切板中
     *
     * @param text 文本信息
     */
    void copyToClipboard(String text);

    /**
     * 推送通知
     *
     * @param type 通知类型
     * @param text 通知内容
     */
    void sendNotification(ArtifactSearchNotification type, String text, Object... array);

    /**
     * 推送通知
     *
     * @param type       通知类型
     * @param text       通知内容
     * @param actionName 操作名称
     * @param file       打开的文件
     * @param textParams 通知内容的参数
     */
    void sendNotification(ArtifactSearchNotification type, String text, String actionName, File file, Object... textParams);

    /**
     * 在等待搜索结果时，显示进度的文本信息
     *
     * @param message       文本信息
     * @param messageParams 文本的参数
     */
    void setProgress(String message, Object... messageParams);

    /**
     * 设置状态栏的信息
     *
     * @param type          文本的类型
     * @param message       文本信息
     * @param messageParams 文本参数
     */
    void setStatusBar(ArtifactSearchStatusMessageType type, String message, Object... messageParams);

    /**
     * 文本处理器
     *
     * @return 文本处理器
     */
    ArtifactSearchPattern getPattern();

    /**
     * 返回 Ioc 容器
     *
     * @return 容器
     */
    MavenEasyContext getIoc();

    /**
     * 提交到线程池并发执行任务
     *
     * @param command 任务
     */
    void async(Runnable command);

    /**
     * 返回线程池
     *
     * @return 线程池
     */
    MavenService getService();

    /**
     * 返回仓库信息
     *
     * @return 仓库信息
     */
    MavenOption getRepositoryInfo();

    /**
     * 设置仓库ID
     *
     * @param repositoryId 仓库ID
     */
    void setRepository(String repositoryId);

    /**
     * 返回仓库接口
     *
     * @return Maven Maven仓库接口
     */
    Repository getRepository();

    /**
     * 返回本地仓库接口
     *
     * @return 本地Maven仓库接口
     */
    LocalRepository getLocalRepository();

    /**
     * 返回本地仓库的配置信息
     *
     * @return 配置信息
     */
    LocalRepositorySettings getLocalRepositorySettings();

    /**
     * 返回数据库接口
     *
     * @return 数据库接口
     */
    RepositoryDatabase getDatabase();

    /**
     * 返回 PomInfo 仓库
     *
     * @return 仓库
     */
    PomRepository getPomRepository();

    /**
     * 下载工件
     *
     * @param artifact 工件信息
     */
    void download(Artifact artifact);

    /**
     * 下载工件
     *
     * @param artifact 工件信息
     */
    void asyncDownload(Artifact artifact);

    /**
     * 保存搜索结果
     *
     * @param result 搜索结果
     */
    SearchNavigationCollection toNavigationCollection(SearchResult result);

    /**
     * 异步获取工件的POM信息
     *
     * @param artifact 工件
     */
    void asyncPom(Artifact artifact);

    /**
     * 显示搜索结果
     */
    void display();

    /**
     * 异步显示搜索结果
     */
    default void asyncDisplay() {
        this.async(this::display);
    }

    /**
     * 如果参数对象是 {@linkplain ArtifactSearchAware}，则向参数对象设置搜索接口
     *
     * @param t   参数对象
     * @param <T> 参数类型
     * @return 参数对象
     */
    default <T> T aware(T t) {
        if (t instanceof ArtifactSearchAware) {
            ((ArtifactSearchAware) t).setSearch(this);
        }
        return t;
    }
}
