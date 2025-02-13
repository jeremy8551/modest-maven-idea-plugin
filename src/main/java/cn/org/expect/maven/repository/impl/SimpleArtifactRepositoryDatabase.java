package cn.org.expect.maven.repository.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import cn.org.expect.concurrent.ThreadSource;
import cn.org.expect.ioc.EasyContext;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.RepositoryDatabaseEngine;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.StringUtils;

public class SimpleArtifactRepositoryDatabase implements RepositoryDatabase {
    protected final static Log log = LogFactory.getLog(SimpleArtifactRepositoryDatabase.class);

    /** 线程池 */
    protected ExecutorService executorService;

    /** 模糊搜索词 pattern 与 {@linkplain SearchResult} 的映射 */
    protected final Map<String, SearchResult> patternMap;

    /** groupid、artifactId 与 {@linkplain SearchResult} 的映射 */
    protected final Map<String, Map<String, SearchResult>> extraMap;

    /** 序列化与反序列化工具 */
    protected final RepositoryDatabaseEngine engine;

    public SimpleArtifactRepositoryDatabase(EasyContext ioc, Class<? extends RepositoryDatabaseEngine> type) {
        this.executorService = ioc.getBean(ThreadSource.class).getExecutorService();
        this.engine = Ensure.notNull(ioc.getBean(type));
        this.patternMap = this.engine.getPattern();
        this.extraMap = this.engine.getArtifact();
    }

    public void insert(String pattern, SearchResult result) {
        this.patternMap.put(pattern, result);
        this.store();
    }

    public SearchResult select(String pattern) {
        return this.patternMap.get(pattern);
    }

    public void delete(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            this.patternMap.remove(pattern);
            this.store();
        }
    }

    public void insert(String groupId, String artifactId, SearchResult result) {
        Map<String, SearchResult> group = this.extraMap.computeIfAbsent(groupId, k -> new HashMap<String, SearchResult>());
        group.put(artifactId, result);
        this.store();
    }

    public SearchResult select(String groupId, String artifactId) {
        Map<String, SearchResult> map = this.extraMap.get(groupId);
        if (map != null) {
            return map.get(artifactId);
        }
        return null;
    }

    public void store() {
        this.executorService.execute(this.engine::save);
    }
}
