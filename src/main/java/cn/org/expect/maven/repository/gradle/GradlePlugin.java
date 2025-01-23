package cn.org.expect.maven.repository.gradle;

import java.util.Date;

import cn.org.expect.util.Dates;
import cn.org.expect.util.StringUtils;

public class GradlePlugin {

    private String id;
    private String version;
    private Date date;
    private String href;
    private String description;

    public GradlePlugin(String id, String version, Date date, String href, String description) {
        this.id = id;
        this.version = version;
        this.date = date;
        this.href = href;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getHref() {
        return href;
    }

    public String getDescription() {
        return description;
    }

    public Date parseDate(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        try {
            return Dates.parse(StringUtils.trim(str, '(', ')'));
        } catch (Throwable e) {
            return null;
        }
    }
}
