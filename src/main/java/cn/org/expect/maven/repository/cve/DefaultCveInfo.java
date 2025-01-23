package cn.org.expect.maven.repository.cve;

public class DefaultCveInfo implements CveInfo {

    private final String name;
    private final String description;
    private final String href;

    public DefaultCveInfo(String name, String description, String href) {
        this.name = name;
        this.description = description;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHref() {
        return href;
    }
}
