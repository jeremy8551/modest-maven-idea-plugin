package cn.org.expect.maven.script;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.util.StringUtils;

@EasyBean("Gitee")
public class Gitee implements VersionControlSystem {

    public String getRawHttpUrl(String fileName) {
        return StringUtils.replaceIndexHolder("https://gitee.com/jeremy8551/modest-maven-plugin-config/raw/master/{0}", fileName);
    }
}
