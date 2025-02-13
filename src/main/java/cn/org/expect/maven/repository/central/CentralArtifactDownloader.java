package cn.org.expect.maven.repository.central;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.ioc.EasyContext;
import cn.org.expect.maven.repository.AbstractArtifactDownloader;

@EasyBean(value = "download.use.central", order = Integer.MAX_VALUE - 1)
public class CentralArtifactDownloader extends AbstractArtifactDownloader {

    public CentralArtifactDownloader(EasyContext ioc) {
        super(ioc);
    }

    public String getAddress() {
        return "https://repo1.maven.org/maven2/";
    }
}
