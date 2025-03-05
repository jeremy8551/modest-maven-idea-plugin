package cn.org.expect.maven.script;

public interface VersionControlSystem {

    /**
     * 返回脚本引擎下载脚本文件的 HTTP 地址
     *
     * @param fileName 文件名
     * @return HTTP 地址
     */
    String getRawHttpUrl(String fileName);
}
