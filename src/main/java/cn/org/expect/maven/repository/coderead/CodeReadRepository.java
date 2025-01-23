package cn.org.expect.maven.repository.coderead;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;

@EasyBean(value = "query.use.coderead", order = 0)
public class CodeReadRepository extends AbstractRepository {

    public CodeReadRepository(MavenEasyContext ioc) {
        super(ioc, CodeReadRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(true, true, true, true, true, true, true, true);
    }

    public String getAddress() {
        return "https://mvn.coderead.cn/";
    }

    public SearchResult query(String pattern, int start) throws Exception {
        return this.getEngine().evaluate("run coderead_query_pattern.usl", "pattern", pattern, "start", start);
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        return this.getEngine().evaluate("run coderead_query_extra.usl", "groupId", groupId, "artifactId", artifactId);
    }
}
