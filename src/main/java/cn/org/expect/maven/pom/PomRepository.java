package cn.org.expect.maven.pom;

import java.io.ByteArrayInputStream;

import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.pom.impl.SimplePom;
import cn.org.expect.maven.search.Search;
import cn.org.expect.util.XMLUtils;
import org.w3c.dom.Node;

@EasyBean(singleton = true)
public class PomRepository {
    protected final static Log log = LogFactory.getLog(PomRepository.class);

    /** 搜索结果 */
    protected final PomDatabase database;

    /** 分析器 */
    protected final PomRepositoryAnalysis analysis;

    public PomRepository() {
        this.database = new PomDatabase();
        this.analysis = new PomRepositoryAnalysis();
    }

    public Pom query(Search search, Artifact artifact) throws Exception {
        Pom pom = this.database.select(artifact);
        if (pom != null) {
            return pom;
        }

        byte[] bytes = this.analysis.readPomXml(search, artifact);
        if (bytes == null) {
            pom = new SimplePom();
            this.database.insert(artifact, pom); // 保存搜索结果
            return pom;
        }

        // 解析 POM 信息
        Node root = XMLUtils.newDocument(new ByteArrayInputStream(bytes));
        Node project = XMLUtils.getChildNode(root, "project");
        pom = this.analysis.parsePomXml(project); // 解析 POM
        this.database.insert(artifact, pom); // 保存搜索结果
        return pom;
    }

    public PomDatabase getDatabase() {
        return database;
    }
}
