package cn.org.expect.maven.repository.central;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;
import cn.org.expect.util.StringUtils;

/**
 * 中央仓库
 */
@EasyBean(value = "query.use.central", order = Integer.MAX_VALUE)
public class CentralRepository extends AbstractRepository {

    public CentralRepository(MavenEasyContext ioc) {
        super(ioc, CentralRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(true, true, true, true, true, true, true, true);
    }

    public String getAddress() {
        return "https://repo1.maven.org/maven2/";
    }

    public SearchResult query(String pattern, int start) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run central_query_pattern.usl", "pattern", escape(pattern), "start", (start - 1));
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run central_query_extra.usl", "groupId", escape(groupId), "artifactId", escape(artifactId));
    }

    public static String escape(String str) {
        StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (StringUtils.inArray(c, '%', '^', '[', ']', '{', '}', '|', '`', '_')) {
                continue;
            } else if (c == '.') {
                buf.append("%2E");
            } else if (c == '\'') {
                buf.append("%27");
            } else if (c == '+') {
                buf.append("%2B");
            } else if (c == '&') {
                buf.append("%26");
            } else if (c == ' ') {
                buf.append("%20");
            } else if (StringUtils.isLetter(c) || StringUtils.isNumber(c)) {
                buf.append(c);
            } else if (StringUtils.inArray(c, '(', ')', '~', '!', '@', '#', '$', '*', '=', ';', ':', ',', '<', '>', '.', '/', '?', '"', '-')) {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
