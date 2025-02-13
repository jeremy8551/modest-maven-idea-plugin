package cn.org.expect.maven.concurrent;

import cn.org.expect.concurrent.BaseJob;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.repository.Repository;
import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.ArtifactSearchAware;
import cn.org.expect.maven.search.ArtifactSearchNotification;
import cn.org.expect.maven.search.ArtifactSearchStatusMessageType;
import cn.org.expect.util.Dates;
import cn.org.expect.util.Ensure;
import cn.org.expect.util.StringUtils;

public abstract class MavenJob extends BaseJob implements Runnable, ArtifactSearchAware {
    protected final static Log log = LogFactory.getLog(MavenJob.class);

    /** 线程池 */
    protected volatile MavenService service;

    /** 搜索接口 */
    private Search search;

    /** Maven仓库接口 */
    private volatile Repository remoteRepository;

    /** true表示任务执行完毕，false表示未执行完毕 */
    private volatile boolean finish;

    /** true表示任务正在运行 false表示任务没有运行 */
    private volatile boolean running;

    /** 抛出的异常 */
    private volatile Throwable throwable;

    /** 任务描述信息 */
    protected String description;

    public MavenJob(String description, Object... descriptionParams) {
        this.terminate = false;
        this.description = MavenMessage.toString(description, descriptionParams);
        this.setName(this.getClass().getName() + "-" + Dates.format17());
        this.finish = false;
        this.running = false;
    }

    public void setService(MavenService service) {
        this.service = Ensure.notNull(service);
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Search getSearch() {
        return this.search;
    }

    protected Repository getRepository() {
        if (this.remoteRepository == null) {
            synchronized (this) {
                if (this.remoteRepository == null) {
                    this.remoteRepository = search.getRepository();
                }
            }
        }
        return this.remoteRepository;
    }

    public void terminate() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} terminated!", this.getName());
        }

        super.terminate();
        if (this.remoteRepository != null) {
            this.remoteRepository.terminate();
        }
    }

    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("maven.search.thread.start", this.getName());
        }

        this.running = true;
        this.finish = false;
        try {
            if (!this.terminate) {
                this.execute();
            }
        } catch (Throwable e) {
            this.throwable = e;
            String message = e.getLocalizedMessage();
            this.getSearch().setProgress(message);
            this.getSearch().setStatusBar(ArtifactSearchStatusMessageType.ERROR, message);
            this.sendNotification();
            log.error(message, e);
        } finally {
            this.finish = true;
            this.running = false;

            // 任务执行完毕，从线程池移除
            if (this.service != null) {
                try {
                    this.service.removeJob(this);
                } catch (Throwable e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("maven.search.thread.finish", this.getName());
            }
        }
    }

    /**
     * 发送通知
     */
    protected void sendNotification() {
        if (StringUtils.isNotBlank(this.description)) {
            this.getSearch().sendNotification(ArtifactSearchNotification.ERROR, "maven.search.error.execute.job.error", this.description);
        }
    }

    /**
     * 发生的异常信息
     *
     * @return 异常信息
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    /**
     * 判断是否正在运行
     *
     * @return 返回true表示正在运行，false表示未运行
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * 判断是否运行完毕
     *
     * @return 返回true表示运行完毕，false表示未运行完毕
     */
    public boolean isFinish() {
        return this.finish;
    }
}
