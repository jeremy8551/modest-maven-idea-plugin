package cn.org.expect.maven.repository.central;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabaseEngine;
import cn.org.expect.maven.search.SearchSettings;

@EasyBean(singleton = true)
public class CentralRepositoryDatabaseEngine extends SimpleArtifactRepositoryDatabaseEngine {

    public CentralRepositoryDatabaseEngine(SearchSettings settings) {
        super(settings, "CENTRAL_PATTERN_TABLE.json", "CENTRAL_ARTIFACT_TABLE.json");
    }
}
