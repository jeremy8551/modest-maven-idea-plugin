package cn.org.expect.maven.concurrent;

import cn.org.expect.maven.search.Search;
import cn.org.expect.util.StringUtils;

public class SearchExtraJob extends MavenJob {

    private final String groupId;

    private final String artifactId;

    public SearchExtraJob(String groupId, String artifactId) {
        super("maven.search.job.search.extra.description", groupId + ":" + artifactId);
        this.groupId = StringUtils.trimBlank(groupId);
        this.artifactId = StringUtils.trimBlank(artifactId);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public int execute() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} search groupId: {}, artifactId: {} ..", this.getName(), this.groupId, this.artifactId);
        }

        Search search = this.getSearch();
        if (search.getRepository().getPermission().supportExtraSearch()) {
            this.getSearch().search(this.groupId, this.artifactId);
            search.asyncDisplay();
        }
        return 0;
    }
}
