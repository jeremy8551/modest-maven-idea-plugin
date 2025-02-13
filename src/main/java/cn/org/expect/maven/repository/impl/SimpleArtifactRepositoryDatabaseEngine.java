package cn.org.expect.maven.repository.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.impl.DefaultArtifact;
import cn.org.expect.maven.repository.RepositoryDatabaseEngine;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.SearchResultType;
import cn.org.expect.maven.search.SearchSettings;
import cn.org.expect.util.CharsetName;
import cn.org.expect.util.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class SimpleArtifactRepositoryDatabaseEngine implements RepositoryDatabaseEngine {
    protected final static Log log = LogFactory.getLog(SimpleArtifactRepositoryDatabaseEngine.class);

    /** 模糊搜索表名 */
    protected String patternTable;

    /** 精确搜索表名 */
    protected String artifactTable;

    /** 表文件存储的目录 */
    protected File parent;

    /** 模糊搜索结果 */
    protected final Map<String, SearchResult> pattern;

    /** 精确搜索结果 */
    protected final Map<String, Map<String, SearchResult>> artifact;

    /** 配置 */
    protected SearchSettings settings;

    /** 读锁 */
    protected final static Object readLock = new Object();

    /** 写锁 */
    protected final static Object writeLock = new Object();

    public SimpleArtifactRepositoryDatabaseEngine(SearchSettings settings, String patternTableName, String artifactTableName) {
        this.settings = settings;
        this.pattern = new ConcurrentHashMap<>();
        this.artifact = new ConcurrentHashMap<>();
        this.parent = settings.getWorkHome();
        this.patternTable = patternTableName;
        this.artifactTable = artifactTableName;
        this.load();
    }

    public Map<String, SearchResult> getPattern() {
        return pattern;
    }

    public Map<String, Map<String, SearchResult>> getArtifact() {
        return artifact;
    }

    public int size() {
        return this.pattern.size() + this.artifact.size();
    }

    public void clear() {
        if (log.isDebugEnabled()) {
            log.debug("clear {} cache ..", this.getClass().getSimpleName());
        }

        this.pattern.clear();
        this.artifact.clear();
    }

    public void load() {
        if (this.settings.isUseCache()) {
            synchronized (readLock) {
                this.loadFile(this.pattern, this.artifact);
            }
        }
    }

    public void save() {
        if (this.settings.isUseCache()) {
            synchronized (writeLock) {
                this.saveFile(this.pattern, this.artifact);
            }
        }
    }

    protected File getTableFile(String tableName) {
        return this.parent == null ? null : new File(this.parent, tableName);
    }

    protected void saveFile(Map<String, SearchResult> pattern, Map<String, Map<String, SearchResult>> artifact) {
        File file1 = this.getTableFile(this.patternTable);
        File file2 = this.getTableFile(this.artifactTable);

        ObjectMapper mapper = new ObjectMapper();
        if (file1 != null) {
            try {
                String jsonStr = mapper.writeValueAsString(pattern);
                FileUtils.write(file1, CharsetName.UTF_8, false, jsonStr);
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage(), e);
                FileUtils.delete(file1);
            }
        }

        if (file2 != null) {
            try {
                String jsonStr = mapper.writeValueAsString(artifact);
                FileUtils.write(file2, CharsetName.UTF_8, false, jsonStr);
            } catch (Throwable e) {
                log.error(e.getLocalizedMessage(), e);
                FileUtils.delete(file2);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("save database files: {}({}), {}({}) ..", file1, pattern.size(), file2, artifact.size());
        }
    }

    protected void loadFile(Map<String, SearchResult> pattern, Map<String, Map<String, SearchResult>> artifact) {
        File file1 = this.getTableFile(this.patternTable);
        File file2 = this.getTableFile(this.artifactTable);

//        pattern.clear();
//        artifact.clear();

        if (file1 != null) {
            if (file1.exists() && file1.isFile()) {
                try {
                    String jsonStr = FileUtils.readline(file1, CharsetName.UTF_8, 0);
                    pattern.putAll(this.deserializePatternTable(jsonStr));
                } catch (Throwable e) {
                    log.error(e.getLocalizedMessage(), e);
                    FileUtils.delete(file1);
                }
            }
        }

        if (file2 != null) {
            if (file2.exists() && file2.isFile()) {
                try {
                    String jsonStr = FileUtils.readline(file2, CharsetName.UTF_8, 0);
                    artifact.putAll(this.deserializeArtifactTable(jsonStr));
                } catch (Throwable e) {
                    log.error(e.getLocalizedMessage(), e);
                    FileUtils.delete(file2);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("load database file: {}({}), {}({})", file1, pattern.size(), file2, artifact.size());
        }
    }

    protected Map<String, SearchResult> deserializePatternTable(String jsonStr) {
        Map<String, SearchResult> map = new LinkedHashMap<>();
        JSONObject jsonObject = new JSONObject(jsonStr);
        Iterator<String> patternList = jsonObject.keys();
        while (patternList.hasNext()) {
            String pattern = patternList.next();
            JSONObject patternJsonObject = jsonObject.getJSONObject(pattern);
            map.put(pattern, this.toSearchResult(patternJsonObject));
        }
        return map;
    }

    protected Map<String, Map<String, SearchResult>> deserializeArtifactTable(String jsonStr) {
        Map<String, Map<String, SearchResult>> map = new ConcurrentHashMap<>();
        JSONObject json = new JSONObject(jsonStr);
        Iterator<String> groups = json.keys();
        while (groups.hasNext()) {
            String group = groups.next();
            JSONObject jsonObject = json.getJSONObject(group);
            Iterator<String> artifactIds = jsonObject.keys();
            while (artifactIds.hasNext()) {
                String artifactId = artifactIds.next();
                DefaultSearchResult searchResult = this.toSearchResult(jsonObject.getJSONObject(artifactId));
                map.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(artifactId, searchResult);
            }
        }
        return map;
    }

    protected DefaultSearchResult toSearchResult(JSONObject jsonObject) {
        String repositoryName = jsonObject.getString("repositoryName");
        int start = jsonObject.getInt("start");
        int foundNumber = jsonObject.getInt("foundNumber");
        long queryTime = jsonObject.getLong("queryTime");
        boolean hasMore = jsonObject.getBoolean("hasMore");
        SearchResultType resultType = jsonObject.getEnum(SearchResultType.class, "type");

        List<Object> list = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(this.toItem(jsonArray.getJSONObject(i)));
        }

        return new DefaultSearchResult(repositoryName, resultType, list, start, foundNumber, queryTime, hasMore);
    }

    protected Object toItem(JSONObject jsonObject) {
        String groupId = jsonObject.getString("groupId");
        String artifactId = jsonObject.getString("artifactId");
        String version = jsonObject.getString("version");
        String type = jsonObject.getString("type");
        long timestamp = jsonObject.optLong("timestamp", -1);
        int versionCount = jsonObject.getInt("versionCount");

        return new DefaultArtifact(groupId, artifactId, version, type, timestamp == -1 ? null : new Date(timestamp), versionCount);
    }
}
