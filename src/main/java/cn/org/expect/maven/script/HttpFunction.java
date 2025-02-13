package cn.org.expect.maven.script;

import java.net.UnknownHostException;

import cn.org.expect.maven.repository.HttpClient;
import cn.org.expect.script.UniversalScriptEngine;
import cn.org.expect.script.annotation.EasyVariableExtension;

@EasyVariableExtension
public class HttpFunction {

    public static String sendHttp(UniversalScriptEngine engine, CharSequence str, Object... array) throws UnknownHostException {
        HttpClient client = new HttpClient();
        return client.sendRequest(str.toString(), array);
    }
}
