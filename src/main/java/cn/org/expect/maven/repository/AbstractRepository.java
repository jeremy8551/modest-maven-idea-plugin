package cn.org.expect.maven.repository;

import cn.org.expect.ioc.EasyContext;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.repository.impl.SimpleArtifactRepositoryDatabase;
import cn.org.expect.script.UniversalScriptEngine;
import cn.org.expect.script.UniversalScriptEngineFactory;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.Terminator;

public abstract class AbstractRepository extends Terminator implements Repository {

    /** 容器上下文信息 */
    private final MavenEasyContext ioc;

    /** 数据库接口 */
    protected RepositoryDatabase database;

    /** 脚本引擎 */
    private volatile UniversalScriptEngine engine;

    public AbstractRepository(MavenEasyContext ioc, Class<? extends RepositoryDatabaseEngine> cls) {
        this.ioc = Ensure.notNull(ioc);
        this.database = new SimpleArtifactRepositoryDatabase(ioc, cls);
    }

    public EasyContext getEasyContext() {
        return this.ioc;
    }

    public RepositoryDatabase getDatabase() {
        return this.database;
    }

    public UniversalScriptEngine getEngine() {
        if (this.engine == null) {
            synchronized (this) {
                if (this.engine == null) {
                    this.engine = this.getEasyContext().getBean(UniversalScriptEngineFactory.class).getScriptEngine();
                }
            }
        }
        return this.engine;
    }

    public void terminate() throws Exception {
        super.terminate();
        if (this.engine != null) {
            this.engine.evaluate("terminate");
        }
    }
}
