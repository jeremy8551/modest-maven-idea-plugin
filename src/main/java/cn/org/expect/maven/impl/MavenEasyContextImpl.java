package cn.org.expect.maven.impl;

import cn.org.expect.ioc.DefaultEasyContext;
import cn.org.expect.maven.MavenEasyContext;

public class MavenEasyContextImpl extends DefaultEasyContext implements MavenEasyContext {

    public MavenEasyContextImpl(ClassLoader classLoader, String... args) {
        super(classLoader, args);
    }
}
