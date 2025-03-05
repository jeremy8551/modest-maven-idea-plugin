package cn.org.expect.maven.repository.impl;

import cn.org.expect.maven.repository.Permission;

public class PermissionAdaptor implements Permission {

    private final boolean supportExtraSearch;
    private final boolean supportOpenInCentralRepository;
    private final boolean supportDownload;
    private final boolean supportDelete;
    private final boolean supportOpenInFileSystem;
    private final boolean supportCopyMavenDependency;
    private final boolean supportCopyGradleDependency;
    private final boolean supportOpenPomFile;

    public PermissionAdaptor(boolean supportExtraSearch, boolean supportOpenInCentralRepository, boolean supportDownload, boolean supportDelete, boolean supportOpenInFileSystem, boolean supportCopyMavenDependency, boolean supportCopyGradleDependency, boolean supportOpenPomFile) {
        this.supportExtraSearch = supportExtraSearch;
        this.supportOpenInCentralRepository = supportOpenInCentralRepository;
        this.supportDownload = supportDownload;
        this.supportDelete = supportDelete;
        this.supportOpenInFileSystem = supportOpenInFileSystem;
        this.supportCopyMavenDependency = supportCopyMavenDependency;
        this.supportCopyGradleDependency = supportCopyGradleDependency;
        this.supportOpenPomFile = supportOpenPomFile;
    }

    public boolean supportExtraSearch() {
        return this.supportExtraSearch;
    }

    public boolean supportOpenInCentralRepository() {
        return this.supportOpenInCentralRepository;
    }

    public boolean supportDownload() {
        return this.supportDownload;
    }

    public boolean supportDelete() {
        return this.supportDelete;
    }

    public boolean supportOpenInFileSystem() {
        return this.supportOpenInFileSystem;
    }

    public boolean supportCopyMavenDependency() {
        return this.supportCopyMavenDependency;
    }

    public boolean supportCopyGradleDependency() {
        return this.supportCopyGradleDependency;
    }

    public boolean supportOpenPomFile() {
        return this.supportOpenPomFile;
    }
}
