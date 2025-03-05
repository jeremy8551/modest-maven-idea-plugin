package cn.org.expect.maven.repository.local;

import java.io.File;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.DefaultSearchResult;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;
import cn.org.expect.util.Ensure;

/**
 * 本地仓库
 */
@EasyBean(value = "query.use.local", order = Integer.MAX_VALUE - 1)
public class DefaultLocalRepository implements LocalRepository {

    /** 本地仓库 */
    private final LocalRepositoryDatabase database;

    /** 本地仓库的配置信息 */
    private final LocalRepositorySettings settings;

    public DefaultLocalRepository(LocalRepositorySettings settings) {
        this.settings = Ensure.notNull(settings);
        this.database = new LocalRepositoryDatabase(settings.getRepository());
    }

    public Permission getPermission() {
        return new PermissionAdaptor(true, true, true, true, true, true, true, true);
    }

    public LocalRepositorySettings getSettings() {
        return this.settings;
    }

    public RepositoryDatabase getDatabase() {
        return this.database;
    }

    public String getAddress() {
        File file = this.settings.getRepository();
        return file == null ? "" : file.getAbsolutePath();
    }

    public SearchResult query(String pattern, int start) throws Exception {
        return this.database.select(pattern);
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        SearchResult result = this.database.select(groupId, artifactId);
        return result == null ? new DefaultSearchResult(LocalRepository.class.getName()) : result;
    }

    public boolean isTerminate() {
        return false;
    }

    public void terminate() {
    }
}
