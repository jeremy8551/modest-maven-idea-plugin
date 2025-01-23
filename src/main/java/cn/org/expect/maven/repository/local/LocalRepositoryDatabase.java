package cn.org.expect.maven.repository.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.impl.DefaultArtifact;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.SearchResultType;
import cn.org.expect.maven.repository.impl.DefaultSearchResult;
import cn.org.expect.util.ArrayUtils;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.FileUtils;
import cn.org.expect.util.StringUtils;

/**
 * 本地仓库的数据库
 */
public class LocalRepositoryDatabase implements RepositoryDatabase {
    private final static Log log = LogFactory.getLog(LocalRepositoryDatabase.class);

    /** groupid、artifactId 与 {@linkplain SearchResult} 的映射 */
    protected final Map<String, Map<String, SearchResult>> map;

    /** 域名集合 */
    protected final Set<String> groupIds;

    /** 工件集合 */
    protected final Set<String> artifactIds;

    /** 本地仓库地址 */
    private final File repository;

    /** 域名工具 */
    private final GroupID groupId;

    public LocalRepositoryDatabase(File repository) {
        this.map = new LinkedHashMap<>();
        this.groupIds = new LinkedHashSet<>();
        this.artifactIds = new LinkedHashSet<>();
        this.groupId = new GroupID();
        this.repository = Ensure.notNull(repository);
        this.load(repository);
    }

    public SearchResult select(String pattern) {
        List<String> list = StringUtils.splitByBlanks(pattern);
        List<String> parts = new ArrayList<>();
        for (String str : list) {
            String[] array = StringUtils.split(str, ':');
            for (String element : array) {
                parts.add(element);
            }
        }

        Set<Object> mas = new LinkedHashSet<>();
        for (String key : parts) {
            Set<Map.Entry<String, Map<String, SearchResult>>> entries = this.map.entrySet();
            for (Map.Entry<String, Map<String, SearchResult>> entry : entries) {
                Map<String, SearchResult> a2r = entry.getValue(); // artifactId - searchResult
                String groupId = entry.getKey();
                if (groupId.contains(key)) {
                    Collection<SearchResult> msrs = a2r.values();
                    for (SearchResult msr : msrs) {
                        List<Object> mal = msr.getList();
                        if (mal.size() > 0) {
                            mas.add(mal.get(0));
                        }
                    }
                } else {
                    Set<String> arts = a2r.keySet();
                    for (String art : arts) {
                        if (art.contains(key)) {
                            SearchResult searchResult = a2r.get(art);
                            List<Object> mal = searchResult.getList();
                            if (mal.size() > 0) {
                                mas.add(mal.get(0));
                            }
                        }
                    }
                }
            }
        }

        return new DefaultSearchResult(LocalRepository.class.getName(), SearchResultType.ALL, new ArrayList<>(mas), mas.size(), mas.size(), System.currentTimeMillis(), false);
    }

    public void insert(String id, SearchResult resultSet) {
    }

    public void delete(String id) {
    }

    public void insert(String groupId, String artifactId, SearchResult result) {
    }

    public SearchResult select(String groupId, String artifactId) {
        Map<String, SearchResult> map = this.map.get(groupId);
        if (map != null) {
            return map.get(artifactId);
        }
        return null;
    }

    /**
     * 加载本地仓库中的工件
     *
     * @param dir 本地仓库目录
     */
    protected void load(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                this.load(file);
                continue;
            }

            if (file.isFile()) {
                this.process(file);
            }
        }
    }

    public void process(File file) {
        // 目录应该是 version
        File parent = file.getParentFile();
        if (parent == null || parent.equals(this.repository)) {
            return;
        }
        String version = parent.getName();

        // 目录应该是 artifactId
        File artifactParent = parent.getParentFile();
        if (artifactParent == null || artifactParent.equals(this.repository)) {
            return;
        }
        String artifactId = artifactParent.getName();

        // 目录应该是 groupId，不应为仓库根目录
        File groupIdParent = artifactParent.getParentFile();
        if (groupIdParent == null || groupIdParent.equals(this.repository)) {
            return;
        }

        String name = FileUtils.getFilenameNoExt(file.getName());
        if (name.equals(artifactId + "-" + version)) {
            String groupId = this.groupId.toString(groupIdParent, this.repository);
            this.groupIds.add(groupId);
            this.artifactIds.add(artifactId);

            // 保存到缓存
            String ext = FileUtils.getFilenameExt(file.getName());
            Artifact artifact = new DefaultArtifact(groupId, artifactId, version, ext, new Date(file.lastModified()), 0);
            Map<String, SearchResult> group = this.map.computeIfAbsent(groupId, k -> new LinkedHashMap<>());
            SearchResult result = group.computeIfAbsent(artifactId, key -> new DefaultSearchResult(LocalRepository.class.getName()));
            this.addArtifact(result, artifact);

            if (log.isTraceEnabled()) {
                log.debug("scan: {}  {}  {} file: {}", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), file.getAbsolutePath());
            }
        }
    }

    /**
     * 添加工件
     *
     * @param artifact 工件
     */
    public void addArtifact(SearchResult result, Artifact artifact) {
        List<Object> list = result.getList();
        for (int i = 0; i < list.size(); i++) {
            Artifact item = (Artifact) list.get(i);
            if (item.getGroupId().equals(artifact.getGroupId()) //
                    && item.getArtifactId().equals(artifact.getArtifactId()) //
                    && item.getVersion().equals(artifact.getVersion()) //
            ) {
                if (artifact.getType().equalsIgnoreCase("jar")) {
                    list.set(i, artifact);
                    return;
                }

                if (item.getType().equalsIgnoreCase("jar")) {
                    return;
                }

                if (!artifact.getType().equalsIgnoreCase("pom")) {
                    list.set(i, artifact);
                    return;
                }
                return;
            }
        }

        list.add(artifact);
        List<String> delimiters = ArrayUtils.asList(".", "-"); // 版本号文本的分隔符
        list.sort((o1, o2) -> {
            Artifact a1 = (Artifact) o1;
            Artifact a2 = (Artifact) o2;
            String v1 = a1.getVersion();
            String v2 = a2.getVersion();
            String[] array1 = StringUtils.split(v1, delimiters, false);
            String[] array2 = StringUtils.split(v2, delimiters, false);
            int size = Math.min(array1.length, array2.length);
            for (int i = 0; i < size; i++) {
                String element1 = array1[i];
                String element2 = array2[i];

                if (StringUtils.isNumber(element1) && StringUtils.isNumber(element2)) {
                    int v = Integer.parseInt(element1) - Integer.parseInt(element2);
                    if (v != 0) {
                        return -v;
                    }
                } else {
                    int v = element1.compareTo(element2);
                    if (v != 0) {
                        return -v;
                    }
                }
            }
            return array2.length - array1.length;
        });
    }

    public static class GroupID extends ArrayList<String> {

        public String toString(File parent, File repository) {
            this.clear();
            this.add(parent.getName());
            File groupParent = parent.getParentFile();
            while (!groupParent.equals(repository)) {
                this.add(groupParent.getName());
                groupParent = groupParent.getParentFile();
            }

            StringBuilder buf = new StringBuilder();
            for (int i = this.size() - 1; i >= 1; i--) {
                buf.append(this.get(i)).append('.');
            }
            buf.append(this.get(0));
            return buf.toString();
        }
    }
}
