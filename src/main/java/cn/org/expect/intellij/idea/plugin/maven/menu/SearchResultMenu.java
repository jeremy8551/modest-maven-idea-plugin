package cn.org.expect.intellij.idea.plugin.maven.menu;

import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.SearchDisplay;
import cn.org.expect.intellij.idea.plugin.maven.concurrent.MavenPluginEDTJob;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationRenderer;
import cn.org.expect.ioc.annotation.EasyBean;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.concurrent.ArtifactDownloadJob;
import cn.org.expect.maven.concurrent.SearchMoreJob;
import cn.org.expect.maven.repository.Permission;
import cn.org.expect.maven.repository.central.CentralRepository;
import cn.org.expect.maven.repository.gradle.GradlePluginRepository;
import cn.org.expect.maven.search.ArtifactSearchNotification;
import cn.org.expect.maven.search.ArtifactSearchStatusMessageType;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.util.FileUtils;
import cn.org.expect.util.StringUtils;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

/**
 * 点击搜索结果弹出的菜单
 */
public class SearchResultMenu extends AbstractMenu {

    private final JPopupMenu topMenu = new JPopupMenu();
    private final JMenuItem copyMaven = new JMenuItem(MavenMessage.get("maven.search.btn.copy.maven.dependency.text")); // 复制 Maven 依赖
    private final JMenuItem copyGradle = new JMenuItem(MavenMessage.get("maven.search.btn.copy.gradle.dependency.text")); // 复制 Gradle 依赖
    private final JMenuItem openInCentralRepository = new JMenuItem(MavenMessage.get("maven.search.btn.open.in.browser.text")); // 在浏览器中打开
    private final JMenuItem openFileSystem = new JMenuItem(MavenMessage.get("maven.search.btn.open.in.filesystem.text")); // 打开本地仓库目录
    private final JMenuItem downloadFile = new JMenuItem(MavenMessage.get("maven.search.btn.download.local.repository.text")); // 下载按钮
    private final JMenuItem cancelDownload = new JMenuItem(MavenMessage.get("maven.search.btn.cancel.download.local.repository.text")); // 取消下载按钮
    private final JMenuItem deleteFile = new JMenuItem(MavenMessage.get("maven.search.btn.delete.local.repository.text")); // 删除本地仓库中的文件
    private final JMenuItem openPomFile = new JMenuItem(MavenMessage.get("maven.search.btn.open.pom.text")); // 打开 POM 文件
    private final JMenuItem copyDetail = new JMenuItem(MavenMessage.get("maven.search.btn.navigation.copy.detail.text")); // 复制详细信息
    private final JMenuItem openUrl = new JMenuItem(MavenMessage.get("maven.search.btn.navigation.open.url.text")); // 打开URL

    public SearchResultMenu(MavenSearchPlugin plugin) {
        super(plugin);
        this.addAction(plugin);
    }

    public void mousePressed(MouseEvent e) {
        MavenSearchPlugin plugin = this.getPlugin();
        SearchDisplay display = plugin.getIdeaUI().getDisplay();
        int selectedIndex = display.locationToIndex(e.getPoint()); // 计算鼠标点击的位置

        // 左键点击 more 按钮
        if (e.getButton() == MouseEvent.BUTTON1 && plugin.canSearch() && selectedIndex != -1 && display.isMore(selectedIndex)) {
            if (log.isDebugEnabled()) {
                log.debug("Click more button {}", selectedIndex);
            }

            String pattern = plugin.getIdeaUI().getSearchField().getText();
            plugin.async(new SearchMoreJob(pattern));
            return;
        }

        // 右键点击
        if (e.getButton() == MouseEvent.BUTTON3) {
            Object selectedObject = display.getElement(selectedIndex);
            if (selectedObject instanceof SearchNavigation) {
                SearchNavigation navigation = (SearchNavigation) selectedObject;
                if (navigation.supportMenu()) {
                    plugin.aware(navigation);
                    plugin.getContext().setSelectedNavigation(navigation); // 保存选中的导航记录
                    display.setSelectedIndex(selectedIndex); // 选中导航记录
                    navigation.displayMenu(this.topMenu, selectedIndex);
                }
            }
        }
    }

    protected void addAction(MavenSearchPlugin plugin) {
        // 复制 Maven 依赖
        copyMaven.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                plugin.copyToClipboard(artifact.toMavenPomDependency());
                plugin.sendNotification(ArtifactSearchNotification.NORMAL, copyMaven.getText());
            }
        });

        // 复制 Gradle 依赖
        copyGradle.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                String text;
                boolean isGradlePlugin = GradlePluginRepository.class.getAnnotation(EasyBean.class).value().equals(plugin.getRepositoryInfo().value());
                Artifact artifact = navigation.getResult();

                String filepath = plugin.getContext().getActionEvent().getProject() == null ? null : plugin.getContext().getActionEvent().getProject().getBasePath();
                if (FileUtils.isDirectory(filepath)) {
                    File dir = new File(filepath);
                    boolean isKotlinDSL = !FileUtils.find(dir, "build.gradle.kts").isEmpty();
                    if (isGradlePlugin) {
                        if (isKotlinDSL) {
                            text = artifact.toGradlePluginKotlinDependency();
                        } else {
                            text = artifact.toGradlePluginGroovyDependency();
                        }
                    } else {
                        if (isKotlinDSL) {
                            text = artifact.toGradleKotlinDependency();
                        } else {
                            text = artifact.toGradleGroovyDependency();
                        }
                    }
                } else {
                    if (isGradlePlugin) {
                        text = artifact.toGradlePluginGroovyDependency();
                    } else {
                        text = artifact.toGradleGroovyDependency();
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("isGradlePlugin: {}, Copy: {}", isGradlePlugin, text);
                }

                plugin.copyToClipboard(text);
                plugin.sendNotification(ArtifactSearchNotification.NORMAL, copyGradle.getText());
            }
        });

        // 在 Maven 中央仓库浏览
        openInCentralRepository.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                String id = CentralRepository.class.getAnnotation(EasyBean.class).value();
                BrowserUtil.browse(plugin.getIoc().getBean(CentralRepository.class, id).toURI(artifact));
            }
        });

        // 打开文件系统目录
        openFileSystem.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                File parent = plugin.getLocalRepository().getParent(artifact);
                if (FileUtils.isDirectory(parent)) {
                    BrowserUtil.browse(parent);
                }
            }
        });

        // 下载文件
        downloadFile.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                plugin.setStatusBar(ArtifactSearchStatusMessageType.RUNNING, "maven.search.download.url", artifact.toMavenId());
                plugin.asyncDownload(artifact);
                plugin.display();
            }
        });

        // 取消下载
        cancelDownload.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) throws Exception {
                Artifact artifact = navigation.getResult();
                plugin.getService().terminate(ArtifactDownloadJob.class, job -> job.getArtifact().equals(artifact));
                plugin.display();
            }
        });

        // 打开 POM 文件
        openPomFile.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                plugin.async(new MavenPluginEDTJob(() -> { // 必须使用 EDT 线程执行
                    File pomfile = plugin.getLocalRepository().getFile(artifact, "pom");
                    if (!FileUtils.isDirectory(pomfile.getParentFile()) || !FileUtils.isFile(pomfile)) {
                        plugin.download(artifact);
                    }

                    VirtualFile vf = null;
                    if (FileUtils.isFile(pomfile)) {
                        vf = LocalFileSystem.getInstance().findFileByIoFile(pomfile);
                    } else {
                        File jarfile = plugin.getLocalRepository().getJarfile(artifact);
                        if (FileUtils.isFile(jarfile)) {
                            vf = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarfile.getAbsolutePath() + "!/META-INF/maven/" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/pom.xml");
                        }
                    }

                    if (vf != null) {
                        FileEditorManager.getInstance(plugin.getContext().getActionEvent().getProject()).openFile(vf, true);
                    } else {
                        plugin.sendNotification(ArtifactSearchNotification.ERROR, "maven.search.error.cannot.open.pom.file");
                    }
                }, "maven.search.error.cannot.open.pom.file"));
                plugin.display();
            }
        });

        // 删除文件
        deleteFile.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                Artifact artifact = navigation.getResult();
                File parent = plugin.getLocalRepository().getParent(artifact);
                if (FileUtils.isDirectory(parent)) {
                    if (log.isDebugEnabled()) {
                        log.debug("delete local repository {} ..", parent.getAbsolutePath());
                    }

                    FileUtils.delete(parent);
                }

                plugin.getPomRepository().getDatabase().delete(artifact); // 删除 PomInfo 缓存
                navigation.clearChildNavigation(); // 删除导航记录
                plugin.display();
            }
        });

        // 复制详细信息
        copyDetail.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                plugin.copyToClipboard(navigation.getLocationString());
                plugin.sendNotification(ArtifactSearchNotification.NORMAL, copyDetail.getText() + " " + navigation.getRightText());
            }
        });

        // 打开URL链接
        openUrl.addActionListener(new MenuItemAction(plugin) {
            public void execute(SearchNavigation navigation) {
                BrowserUtil.browse(navigation.getLocationString());
            }
        });
    }

    public void displayItemMenu(MavenSearchPlugin plugin, SearchNavigation navigation, JPopupMenu topMenu, int selectedIndex, int offset) {
        topMenu.removeAll();

        // 复制Maven依赖
        Permission permission = plugin.getRepository().getPermission();
        if (permission.supportCopyMavenDependency()) {
            topMenu.add(copyMaven);
        } else {
            topMenu.remove(copyMaven);
        }

        // 复制Gradle依赖
        if (permission.supportCopyGradleDependency()) {
            topMenu.add(copyGradle);
        } else {
            topMenu.remove(copyGradle);
        }

        if (permission.supportOpenInCentralRepository()) {
            topMenu.add(openInCentralRepository);
        } else {
            topMenu.remove(openInCentralRepository);
        }

        if (permission.supportOpenInFileSystem()) {
            topMenu.add(openFileSystem);
        } else {
            topMenu.remove(openFileSystem);
        }

        if (permission.supportDelete()) {
            topMenu.add(deleteFile);
        } else {
            topMenu.remove(deleteFile);
        }

        if (permission.supportDownload()) {
            topMenu.add(downloadFile);
        } else {
            topMenu.remove(downloadFile);
        }

        if (permission.supportDownload()) {
            topMenu.add(cancelDownload);
        } else {
            topMenu.remove(cancelDownload);
        }

        Artifact artifact = navigation.getResult();
        File pomfile = plugin.getLocalRepository().getFile(artifact, "pom");
        if (permission.supportOpenPomFile() && FileUtils.isFile(pomfile)) {
            topMenu.add(openPomFile);
        } else {
            topMenu.remove(openPomFile);
        }

        // 工件在本地仓库中存在
        if (plugin.getLocalRepository().exists(artifact)) {
            topMenu.remove(downloadFile);
            topMenu.remove(cancelDownload);
        } else {
            topMenu.remove(openFileSystem);
            topMenu.remove(deleteFile);

            if (plugin.getService().isRunning(ArtifactDownloadJob.class, job -> job.getArtifact().equals(artifact))) {
                topMenu.remove(downloadFile);
            } else {
                topMenu.remove(cancelDownload);
            }
        }

        // 显示导航记录的菜单
        plugin.getIdeaUI().getDisplay().displayMenu(topMenu, selectedIndex, SearchNavigationRenderer.navigation1);
    }

    public void displayDetailMenu(MavenSearchPlugin plugin, SearchNavigation navigation, JPopupMenu topMenu, int selectedIndex) {
        topMenu.removeAll();

        // 复制文本
        topMenu.add(copyDetail);

        // 打开URL
        if (StringUtils.indexOf(navigation.getRightText(), "URL", 0, true) != -1) {
            topMenu.add(openUrl);
        }

        // 显示导航记录的菜单
        plugin.getIdeaUI().getDisplay().displayMenu(topMenu, selectedIndex, navigation.getMenuPosition());
    }
}
