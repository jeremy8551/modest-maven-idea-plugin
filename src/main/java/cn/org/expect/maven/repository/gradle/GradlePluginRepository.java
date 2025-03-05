package cn.org.expect.maven.repository.gradle;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.AbstractRepository;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.PermissionAdaptor;
import cn.org.expect.util.StringUtils;

/**
 * gradle插件仓库
 */
@EasyBean(value = "query.use.gradle", order = 1)
public class GradlePluginRepository extends AbstractRepository {

    public GradlePluginRepository(MavenEasyContext ioc) {
        super(ioc, GradlePluginRepositoryDatabaseEngine.class);
    }

    public Permission getPermission() {
        return new PermissionAdaptor(true, false, false, false, false, false, true, false);
    }

    public String getAddress() {
        return "https://plugins.gradle.org";
    }

    public SearchResult query(String pattern, int start) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run gradle_query_pattern.usl", "pattern", this.escape(pattern), "start", (start - 1));
    }

    public SearchResult query(String groupId, String artifactId) throws Exception {
        this.terminate = false;
        return this.getEngine().evaluate("run gradle_query_extra.usl", "gradlePluginID", artifactId);
    }

    public String escape(String str) {
        StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '!') {
                buf.append("%21");
            } else if (c == '`') {
                buf.append("%60");
            } else if (c == ';') {
                buf.append("%3B");
            } else if (c == ':') {
                buf.append("%3A");
            } else if (c == '\'') {
                buf.append("%27");
            } else if (c == '|') {
                buf.append("%7C");
            } else if (c == '\\') {
                buf.append("%5C");
            } else if (c == ',') {
                buf.append("%2C");
            } else if (c == '/') {
                buf.append("%2F");
            } else if (c == '?') {
                buf.append("%3F");
            } else if (c == '#') {
                buf.append("%23");
            } else if (c == '%') {
                buf.append("%25");
            } else if (c == '+') {
                buf.append("%2B");
            } else if (c == '@') {
                buf.append("%40");
            } else if (c == '$') {
                buf.append("%24");
            } else if (c == '^') {
                buf.append("%5E");
            } else if (c == '&') {
                buf.append("%26");
            } else if (c == '(') {
                buf.append("%28");
            } else if (c == ')') {
                buf.append("%29");
            } else if (c == '=') {
                buf.append("%3D");
            } else if (c == '[') {
                buf.append("%5B");
            } else if (c == ']') {
                buf.append("%5D");
            } else if (c == '{') {
                buf.append("%7B");
            } else if (c == '}') {
                buf.append("%7D");
            } else if (c == ' ') {
                buf.append('+');
            } else if (StringUtils.isLetter(c) || StringUtils.isNumber(c) || StringUtils.isSymbol(c)) { // - " < > .
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
