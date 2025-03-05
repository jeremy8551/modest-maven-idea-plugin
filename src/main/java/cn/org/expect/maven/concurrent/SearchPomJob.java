package cn.org.expect.maven.concurrent;

import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.search.Search;

public class SearchPomJob extends MavenArtifactJob {

    public SearchPomJob(Artifact artifact) {
        super(artifact, "maven.search.job.search.pom.info.description");
    }

    public int execute() throws Exception {
        Search plugin = this.getSearch();
        plugin.getPomRepository().query(plugin, this.getArtifact());
        plugin.asyncDisplay();
        return 0;
    }
}
