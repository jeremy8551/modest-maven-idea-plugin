package cn.org.expect.maven.search;

import cn.org.expect.concurrent.ThreadSource;
import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.MavenEasyContext;
import cn.org.expect.maven.MavenOption;
import cn.org.expect.maven.concurrent.MavenService;
import cn.org.expect.maven.concurrent.SearchInputJob;
import cn.org.expect.maven.impl.ArtifactOptionImpl;
import cn.org.expect.maven.pom.PomRepository;
import cn.org.expect.maven.repository.Repository;
import cn.org.expect.maven.repository.RepositoryDatabase;
import cn.org.expect.maven.repository.local.DefaultLocalRepository;
import cn.org.expect.maven.repository.local.LocalRepository;
import cn.org.expect.maven.repository.local.LocalRepositorySettings;
import cn.org.expect.util.Ensure;

public abstract class AbstractSearch implements Search {

    /** 容器上下文信息 */
    private final MavenEasyContext ioc;

    /** 仓库接口 */
    private Repository repository;

    /** 选择的仓库 */
    private MavenOption selectRepository;

    /** 本地Maven仓库接口 */
    private volatile LocalRepository localRepository;

    /** 文本处理器 */
    private final ArtifactSearchPattern pattern;

    public AbstractSearch(MavenEasyContext ioc) {
        this.pattern = new ArtifactSearchPattern();
        this.ioc = Ensure.notNull(ioc);
    }

    public ArtifactSearchPattern getPattern() {
        return this.pattern;
    }

    public void setRepository(String repositoryId) {
        this.repository = this.ioc.getRepository(repositoryId);
        if (this.repository == null) {
            repositoryId = DefaultLocalRepository.class.getAnnotation(EasyBean.class).value();
            this.repository = this.ioc.getRepository(repositoryId);
        }

        Ensure.notNull(this.repository, repositoryId);
        this.selectRepository = new ArtifactOptionImpl(repositoryId);
    }

    public MavenEasyContext getIoc() {
        return this.ioc;
    }

    public synchronized void async(Runnable command) {
        this.ioc.getBean(ThreadSource.class).getExecutorService().execute(this.aware(command));
    }

    public MavenService getService() {
        return this.ioc.getBean(MavenService.class);
    }

    public MavenOption getRepositoryInfo() {
        return this.selectRepository;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public LocalRepository getLocalRepository() {
        if (this.localRepository == null) {
            synchronized (this) {
                if (this.localRepository == null) {
                    this.localRepository = this.ioc.getBean(LocalRepository.class);
                }
            }
        }
        return this.localRepository;
    }

    public LocalRepositorySettings getLocalRepositorySettings() {
        return this.localRepository.getSettings();
    }

    /**
     * 返回模糊查询工具
     *
     * @return 模糊查询工具
     */
    public synchronized SearchInputJob getInput() {
        SearchInputJob job = this.getService().getFirst(SearchInputJob.class, first -> true); // 只能是单例模式
        if (job == null) {
            job = new SearchInputJob();
            this.async(job);

            // 等待任务启动
            this.getService().waitFor(SearchInputJob.class, command -> !command.isRunning(), 10 * 1000);
        }
        return job;
    }

    public RepositoryDatabase getDatabase() {
        return this.getRepository().getDatabase();
    }

    public PomRepository getPomRepository() {
        return this.ioc.getBean(PomRepository.class);
    }
}
