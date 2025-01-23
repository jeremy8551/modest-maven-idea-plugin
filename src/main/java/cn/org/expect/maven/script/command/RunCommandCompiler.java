package cn.org.expect.maven.script.command;

import java.io.IOException;

import cn.org.expect.script.UniversalScriptAnalysis;
import cn.org.expect.script.UniversalScriptContext;
import cn.org.expect.script.UniversalScriptParser;
import cn.org.expect.script.UniversalScriptReader;
import cn.org.expect.script.UniversalScriptSession;
import cn.org.expect.script.annotation.EasyCommandCompiler;
import cn.org.expect.script.command.AbstractTraceCommand;
import cn.org.expect.script.command.AbstractTraceCommandCompiler;
import cn.org.expect.script.io.ScriptFileExpression;

@EasyCommandCompiler(name = "run", keywords = {"run"})
public class RunCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        String expression = analysis.trim(command.substring("run".length()), 0, 1);
        return new RunCommand(this, command, ScriptFileExpression.parse(session, context, expression));
    }
}
