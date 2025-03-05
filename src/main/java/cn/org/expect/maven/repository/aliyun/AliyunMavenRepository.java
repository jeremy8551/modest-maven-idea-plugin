package cn.org.expect.maven.repository.aliyun;

import java.net.UnknownHostException;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.central.CentralRepository;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;

/**
 * 阿里云仓库
 */
@EasyBean(value = "query.use.aliyun", order = 0)
public class AliyunMavenRepository extends AbstractRepository {

    public AliyunMavenRepository(MavenEasyContext ioc) {
        super(ioc, AliyunMavenRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(false, true, true, true, true, true, true, true);
    }

    public String getAddress() {
        return "https://developer.aliyun.com/mvn/search";
    }

    public SearchResult query(String pattern, int start) throws UnknownHostException {
        this.terminate = false;
        return this.getEngine().evaluate("run aliyun_query_pattern.usl", "pattern", CentralRepository.escape(pattern));
    }

    public SearchResult query(String groupId, String artifactId) throws UnknownHostException {
        throw new UnsupportedOperationException(groupId + ":" + artifactId);
    }
}
