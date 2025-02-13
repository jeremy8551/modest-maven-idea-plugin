package cn.org.expect.maven.concurrent;

import java.io.File;

import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.repository.ArtifactDownloader;
import cn.org.expect.maven.repository.local.LocalRepositoryNotFoundException;
import cn.org.expect.maven.repository.local.LocalRepositorySettings;
import cn.org.expect.maven.search.Search;
import cn.org.expect.maven.search.ArtifactSearchNotification;
import cn.org.expect.util.FileUtils;

public class ArtifactDownloadJob extends MavenArtifactJob {

    private volatile ArtifactDownloader downloader;

    public ArtifactDownloadJob(Artifact artifact) {
        super(artifact, "maven.search.job.download.artifact.description", artifact.toMavenId());
    }

    public int execute() throws Exception {
        Search plugin = this.getSearch();
        LocalRepositorySettings settings = plugin.getLocalRepositorySettings();
        File parent = plugin.getLocalRepository().getParent(this.getArtifact());
        FileUtils.createDirectory(parent, true);
        String way = plugin.getSettings().getDownloadWay();

        try {
            this.downloader = plugin.aware(plugin.getIoc().getBean(ArtifactDownloader.class, way));
            this.downloader.execute(this.getArtifact(), parent, settings.isDownloadSourcesAutomatically(), settings.isDownloadDocsAutomatically(), settings.isDownloadAnnotationsAutomatically());
        } catch (LocalRepositoryNotFoundException e) {
            plugin.sendNotification(ArtifactSearchNotification.ERROR, "maven.search.error.cannot.found.local.repository");
        }
        return 0;
    }

    public void terminate() throws Exception {
        super.terminate();
        if (this.downloader != null && !this.downloader.isTerminate()) {
            this.downloader.terminate();
        }
    }
}
