package cn.org.expect.maven.repository.cve;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;

@EasyBean(value = "query.use.cve", order = -2)
public class CveRepository extends AbstractRepository {

    public CveRepository(MavenEasyContext ioc) {
        super(ioc, CveRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(false, false, false, false, false, false, false, false);
    }

    public String getAddress() {
        return "https://cve.mitre.org";
    }

    public SearchResult query(String pattern, int start) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run cve_query_response.usl", "pattern", pattern);
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        throw new UnsupportedOperationException();
    }
}
