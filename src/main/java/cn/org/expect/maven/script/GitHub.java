package cn.org.expect.maven.script;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.util.StringUtils;

@EasyBean("Github")
public class GitHub implements VersionControlSystem {

    public String getRawHttpUrl(String fileName) {
        return StringUtils.replaceIndexHolder("https://raw.githubusercontent.com/jeremy8551/modest-maven-plugin-config/refs/heads/main/{0}", fileName);
    }
}
