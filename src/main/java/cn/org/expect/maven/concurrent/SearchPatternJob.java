package cn.org.expect.maven.concurrent;

import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.ArtifactSearchStatusMessageType;
import cn.org.expect.maven.search.SearchNavigationCollection;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.StringUtils;

public class SearchPatternJob extends MavenJob {

    /** 模糊搜索的文本 */
    protected final String pattern;

    public SearchPatternJob(String pattern) {
        super("maven.search.job.search.pattern.description", pattern);
        this.pattern = Ensure.notNull(pattern);
    }

    public String getPattern() {
        return this.pattern;
    }

    public int execute() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} search pattern: {}", this.getName(), this.pattern);
        }

        if (StringUtils.isBlank(this.pattern)) {
            return -1;
        }

        Search search = this.getSearch();
        try {
            SearchResult result = search.execute(this.pattern);
            if (this.terminate) {
                return 0;
            } else {
                SearchNavigationCollection navigationCollection = search.toNavigationCollection(result);
                search.getContext().setNavigationCollection(navigationCollection);
            }

            // 搜索结果为空
            if (result == null || result.size() == 0) {
                search.display();
                search.setProgress("maven.search.nothing.found");
                search.setStatusBar(ArtifactSearchStatusMessageType.NORMAL, "maven.search.nothing.found");
                return 0;
            }

            search.asyncDisplay();
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage(), e);
            search.getContext().setNavigationCollection(null);
            search.display();
            search.setProgress("maven.search.send.url.fail", search.getRepositoryInfo().getDisplayName());
            search.setStatusBar(ArtifactSearchStatusMessageType.ERROR, "maven.search.send.url.fail", search.getRepositoryInfo().getDisplayName());
        }

        return 0;
    }
}
