package cn.org.expect.maven.repository.coderead;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabaseEngine;
import cn.org.expect.maven.search.SearchSettings;

@EasyBean(singleton = true)
public class CodeReadRepositoryDatabaseEngine extends SimpleArtifactRepositoryDatabaseEngine {

    public CodeReadRepositoryDatabaseEngine(SearchSettings settings) {
        super(settings, "CODEREAD_PATTERN_TABLE.json", "CODEREAD_ARTIFACT_TABLE.json");
    }
}
