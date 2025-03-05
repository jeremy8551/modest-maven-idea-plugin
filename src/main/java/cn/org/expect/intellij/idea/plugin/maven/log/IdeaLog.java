package cn.org.expect.intellij.idea.plugin.maven.log;

import java.util.ArrayList;
import java.util.List;

import cn.org.expect.log.LevelLogger;
import cn.org.expect.log.LogContext;
import cn.org.expect.util.StringUtils;
import com.intellij.openapi.diagnostic.Logger;

public class IdeaLog extends LevelLogger {

    /** 行的集合 */
    private final List<String> list;

    /** 类信息 */
    protected Class<?> type;

    /** 日志接口 */
    protected Logger log;

    public IdeaLog(LogContext context, Class<?> type) {
        super(context);
        this.list = new ArrayList<>();
        this.type = type;
        this.log = Logger.getInstance(type);
    }

    public String getName() {
        return this.type.getName();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isFatalEnabled() {
        return true;
    }

    public void trace(String message, Throwable cause) {
        boolean hasLines = StringUtils.contains(message, '\r', '\n');
        if (hasLines) {
            synchronized (this.list) {
                this.list.clear();
                StringUtils.splitLines(message, this.list);
                int size = this.list.size() - 1;
                for (int i = 0; i < size; i++) {
                    log.trace(this.list.get(i));
                }
                message = this.list.get(size);
            }
        }

        log.trace(message);
        if (cause != null) {
            log.trace(cause);
        }
    }

    public void debug(String message, Throwable cause) {
        boolean hasLines = StringUtils.contains(message, '\r', '\n');
        if (hasLines) {
            synchronized (this.list) {
                this.list.clear();
                StringUtils.splitLines(message, this.list);
                int size = this.list.size() - 1;
                for (int i = 0; i < size; i++) {
                    log.debug(this.list.get(i));
                }
                message = this.list.get(size);
            }
        }

        if (cause == null) {
            log.debug(message);
        } else {
            log.debug(message, cause);
        }
    }

    public void info(String message, Throwable cause) {
        boolean hasLines = StringUtils.contains(message, '\r', '\n');
        if (hasLines) {
            synchronized (this.list) {
                this.list.clear();
                StringUtils.splitLines(message, this.list);
                int size = this.list.size() - 1;
                for (int i = 0; i < size; i++) {
                    log.info(this.list.get(i));
                }
                message = this.list.get(size);
            }
        }

        if (cause == null) {
            log.info(message);
        } else {
            log.info(message, cause);
        }
    }

    public void warn(String message, Throwable cause) {
        boolean hasLines = StringUtils.contains(message, '\r', '\n');
        if (hasLines) {
            synchronized (this.list) {
                this.list.clear();
                StringUtils.splitLines(message, this.list);
                int size = this.list.size() - 1;
                for (int i = 0; i < size; i++) {
                    log.warn(this.list.get(i));
                }
                message = this.list.get(size);
            }
        }

        if (cause == null) {
            log.warn(message);
        } else {
            log.warn(message, cause);
        }
    }

    public void error(String message, Throwable cause) {
        boolean hasLines = StringUtils.contains(message, '\r', '\n');
        if (hasLines) {
            synchronized (this.list) {
                this.list.clear();
                StringUtils.splitLines(message, this.list);
                int size = this.list.size() - 1;
                for (int i = 0; i < size; i++) {
                    log.error(this.list.get(i));
                }
                message = this.list.get(size);
            }
        }

        if (cause == null) {
            log.error(message);
        } else {
            log.error(message, cause);
        }
    }

    public void fatal(String message, Throwable cause) {
        this.error(message, cause);
    }
}
