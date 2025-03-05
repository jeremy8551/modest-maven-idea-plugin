package cn.org.expect.maven.repository.clazz;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.central.CentralRepository;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;

/**
 * 按类名搜索
 */
@EasyBean(value = "query.use.class", order = -1)
public class ClassRepository extends AbstractRepository {

    public ClassRepository(MavenEasyContext ioc) {
        super(ioc, ClassRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(false, true, true, true, true, true, true, true);
    }

    public String getAddress() {
        return "https://search.maven.org";
    }

    public SearchResult query(String className, int start) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run central_class_query_pattern.usl", "className", CentralRepository.escape(className), "start", (start - 1));
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        throw new UnsupportedOperationException();
    }
}
