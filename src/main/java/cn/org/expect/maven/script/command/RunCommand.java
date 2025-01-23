package cn.org.expect.maven.script.command;

import java.io.File;

import cn.org.expect.ioc.EasyContext;
import cn.org.expect.maven.repository.HttpClient;
import cn.org.expect.maven.repository.Repository;
import cn.org.expect.maven.script.VersionControlSystem;
import cn.org.expect.maven.search.SearchSettings;
import cn.org.expect.script.UniversalCommandCompiler;
import cn.org.expect.script.UniversalScriptContext;
import cn.org.expect.script.UniversalScriptEngine;
import cn.org.expect.script.UniversalScriptException;
import cn.org.expect.script.UniversalScriptSession;
import cn.org.expect.script.UniversalScriptStderr;
import cn.org.expect.script.UniversalScriptStdout;
import cn.org.expect.script.command.AbstractTraceCommand;
import cn.org.expect.script.io.ScriptFileExpression;
import cn.org.expect.util.ArrayUtils;
import cn.org.expect.util.CharsetName;
import cn.org.expect.util.FileUtils;
import cn.org.expect.util.StringUtils;

/**
 * 运行脚本文件解析响应 Json 与 Html 文本
 */
public class RunCommand extends AbstractTraceCommand {

    /** 客户端 */
    private final HttpClient client;

    /** 脚本引擎 */
    private UniversalScriptEngine engine;

    /** 脚本文件 */
    private final ScriptFileExpression file;

    public RunCommand(UniversalCommandCompiler compiler, String command, ScriptFileExpression file) {
        super(compiler, command);
        this.file = file;
        this.client = new HttpClient();
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        this.engine = context.getEngine();
        EasyContext ioc = context.getContainer();
        SearchSettings settings = ioc.getBean(SearchSettings.class);
        File parent = settings.getWorkHome();

        String filename = this.file.getName();
        String parameters = " " + StringUtils.join(ArrayUtils.subArray(this.file.getParameters(), 1, this.file.getParameters().length), " ");
        String script = ". " + this.getFilepath(parent, filename) + parameters;
        try {
            session.putValue(this.engine.evaluate(script));
        } catch (Throwable e) {
            if (settings.isDownScriptRepository()) {
                log.error(e.getLocalizedMessage(), e);
                FileUtils.assertCreateDirectory(parent);

                String downUrl = ioc.getBean(VersionControlSystem.class, settings.getScriptRepositoryName()).getRawHttpUrl(filename);
                if (log.isWarnEnabled()) {
                    log.warn("An error occurred while executing {} to parse Response Body, attempting to retrieve it from {} Download the latest script file for parsing ..", filename, downUrl);
                }

                // 从 SCM 下载最新的脚本文件
                String responseBody = this.client.sendRequest(downUrl);
                if (StringUtils.isNotBlank(responseBody)) {
                    File scriptfile = new File(parent, filename);
                    FileUtils.write(scriptfile, CharsetName.UTF_8, false, responseBody);
                    session.putValue(this.engine.evaluate(". " + scriptfile.getAbsolutePath() + parameters));
                    return 0;
                }
            }

            throw new UniversalScriptException(script, e);
        }

        return 0;
    }

    public void terminate() throws Exception {
        super.terminate();

        // 终止 Http
        if (this.client != null) {
            this.client.terminate();
        }

        // 终止脚本
        if (this.engine != null) {
            this.engine.evaluate("terminate");
        }
    }

    protected String getFilepath(File parent, String filename) {
        if (FileUtils.isDirectory(parent)) {
            File config = new File(parent, filename);
            if (FileUtils.isFile(config)) {
                return config.getAbsolutePath();
            }
        }

        return "classpath:/" + Repository.class.getPackage().getName().replace('.', '/') + "/" + filename;
    }
}
