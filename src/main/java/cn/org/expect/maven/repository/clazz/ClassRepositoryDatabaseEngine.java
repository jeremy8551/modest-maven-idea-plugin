package cn.org.expect.maven.repository.clazz;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabaseEngine;
import cn.org.expect.maven.search.SearchSettings;

@EasyBean(singleton = true)
public class ClassRepositoryDatabaseEngine extends SimpleArtifactRepositoryDatabaseEngine {

    public ClassRepositoryDatabaseEngine(SearchSettings settings) {
        super(settings, "SEARCH_CLASS_IN_CENTRAL_PATTERN_TABLE.json", "SEARCH_CLASS_IN_CENTRAL_ARTIFACT_TABLE.json");
    }
}
