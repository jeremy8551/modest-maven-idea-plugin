package cn.org.expect.maven.pom;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.org.expect.maven.Artifact;

public class PomDatabase {

    /** 搜索结果 */
    protected final Map<String, Map<String, Map<String, Pom>>> database;

    public PomDatabase() {
        this.database = new ConcurrentHashMap<>();
    }

    public Pom select(Artifact artifact) {
        Map<String, Map<String, Pom>> groupMap = this.database.get(artifact.getGroupId());
        if (groupMap != null) {
            Map<String, Pom> artifactMap = groupMap.get(artifact.getArtifactId());
            if (artifactMap != null) {
                return artifactMap.get(artifact.getVersion());
            }
        }
        return null;
    }

    public Pom delete(Artifact artifact) {
        Map<String, Map<String, Pom>> groupMap = this.database.get(artifact.getGroupId());
        if (groupMap != null) {
            Map<String, Pom> artifactMap = groupMap.get(artifact.getArtifactId());
            if (artifactMap != null) {
                return artifactMap.remove(artifact.getVersion());
            }
        }
        return null;
    }

    public void insert(Artifact artifact, Pom pom) {
        Map<String, Map<String, Pom>> groupMap = this.database.computeIfAbsent(artifact.getGroupId(), k -> new HashMap<>());
        Map<String, Pom> artifactMap = groupMap.computeIfAbsent(artifact.getArtifactId(), k -> new HashMap<>());
        artifactMap.put(artifact.getVersion(), pom);
    }
}
