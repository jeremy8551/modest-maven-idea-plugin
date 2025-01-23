package cn.org.expect.maven.repository.cve;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabaseEngine;
import cn.org.expect.maven.search.SearchSettings;
import org.json.JSONObject;

@EasyBean(singleton = true)
public class CveRepositoryDatabaseEngine extends SimpleArtifactRepositoryDatabaseEngine {

    public CveRepositoryDatabaseEngine(SearchSettings settings) {
        super(settings, "CVE_PATTERN_TABLE.json", "CVE_ARTIFACT_TABLE.json");
    }

    protected Object toItem(JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        String description = jsonObject.getString("description");
        String href = jsonObject.getString("href");

        return new DefaultCveInfo(name, description, href);
    }
}
