package cn.org.expect.maven.impl;

import cn.org.expect.maven.MavenOption;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.util.Ensure;

public class ArtifactOptionImpl implements MavenOption {

    private final String key;

    private final String name;

    public ArtifactOptionImpl(String key) {
        this.key = Ensure.notNull(key);
        this.name = MavenMessage.get("maven.search.repository.option." + key);
    }

    public String value() {
        return key;
    }

    public String getDisplayName() {
        return name;
    }

    public boolean equals(Object obj) {
        return obj instanceof MavenOption && ((MavenOption) obj).value().equals(this.value());
    }

    public String toString() {
        return this.name;
    }
}
