package cn.org.expect.maven.repository;

import java.util.ArrayList;
import java.util.List;

import cn.org.expect.maven.Artifact;
import cn.org.expect.util.NetUtils;
import cn.org.expect.util.StringUtils;
import cn.org.expect.util.Terminate;

/**
 * 工件仓库
 */
public interface Repository extends Terminate {

    /**
     * 返回权限接口
     *
     * @return 权限接口
     */
    Permission getPermission();

    /**
     * 返回数据库
     *
     * @return 数据库
     */
    RepositoryDatabase getDatabase();

    /**
     * 返回 Maven 仓库的 URL 地址
     *
     * @return URL地址
     */
    String getAddress();

    /**
     * 模糊查询
     *
     * @param pattern 字符串
     * @param start   起始记录位置，从1开始
     * @return 查询结果
     * @throws Exception 模糊查询发生错误
     */
    SearchResult query(String pattern, int start) throws Exception;

    /**
     * 精确查询
     *
     * @param groupId    工件的域名
     * @param artifactId 工件的名
     * @return 查询结果
     * @throws Exception 精确查询发生错误
     */
    SearchResult query(String groupId, String artifactId) throws Exception;

    /**
     * 生成工件的 URI
     *
     * @param artifact 工件
     * @return URI
     */
    default String toURI(Artifact artifact) {
        List<String> list = new ArrayList<>();
        list.add(this.getAddress());
        StringUtils.split(artifact.getGroupId(), '.', list);
        list.add(artifact.getArtifactId());
        list.add(artifact.getVersion());
        return NetUtils.joinUri(list.toArray(new String[list.size()]));
    }
}
