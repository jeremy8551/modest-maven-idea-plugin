package cn.org.expect.maven.repository.gradle;

import java.util.Date;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabaseEngine;
import cn.org.expect.maven.search.SearchSettings;
import org.json.JSONObject;

@EasyBean(singleton = true)
public class GradlePluginRepositoryDatabaseEngine extends SimpleArtifactRepositoryDatabaseEngine {

    public GradlePluginRepositoryDatabaseEngine(SearchSettings settings) {
        super(settings, "GRADLE_PATTERN_TABLE.json", "GRADLE_ARTIFACT_TABLE.json");
    }

    protected Object toItem(JSONObject jsonObject) {
        String id = jsonObject.optString("id");
        String version = jsonObject.optString("version");
        long date = jsonObject.optLong("date", -1);
        String href = jsonObject.optString("href");
        String description = jsonObject.optString("description", "");

        return new GradlePlugin(id, version, date == -1 ? null : new Date(date), href, description);
    }
}
