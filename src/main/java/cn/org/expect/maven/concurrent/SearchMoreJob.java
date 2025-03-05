package cn.org.expect.maven.concurrent;

import java.util.List;

import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.impl.DefaultSearchResult;
import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.SearchNavigationCollection;

public class SearchMoreJob extends SearchPatternJob {

    private final String pattern;

    public SearchMoreJob(String pattern) {
        super(pattern);
        this.description = MavenMessage.get("maven.search.job.search.more.description");
        this.pattern = pattern;
    }

    public int execute() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} search more: {}", this.getName(), this.pattern);
        }

        Search search = this.getSearch();
        RepositoryDatabase database = search.getDatabase();
        SearchResult result = database.select(this.pattern);
        if (result != null && result.isHasMore()) { // 还有未加载的数据
            int start = result.getStart();
            int foundNumber = result.getFoundNumber();
            List<Object> list = result.getList();

            SearchResult next = this.getRepository().query(this.pattern, start);
            if (next != null) {
                List<Object> nextList = next.getList();
                list.addAll(nextList);

                boolean hasMore = true;
                switch (result.getType()) {
                    case ALL:
                        hasMore = false;
                        break;
                    case LIMIT_PAGE:
                        hasMore = foundNumber > list.size();
                        break;
                    case NO_TOTAL:
                        hasMore = !nextList.isEmpty();
                        break;
                }

                // 将搜索结果合并
                DefaultSearchResult newResult = new DefaultSearchResult(result.getRepositoryName(), result.getType(), list, next.getStart(), Math.max(list.size(), foundNumber), System.currentTimeMillis(), hasMore);
                database.insert(this.pattern, newResult); // 保存到数据库

                // 保存搜索结果
                SearchNavigationCollection navigationCollection = search.toNavigationCollection(newResult);
                search.getContext().setNavigationCollection(navigationCollection);
                search.asyncDisplay();
            }
        }
        return 0;
    }
}
