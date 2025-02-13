package cn.org.expect.intellij.idea.plugin.maven;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import cn.org.expect.intellij.idea.plugin.maven.concurrent.MavenPluginDisplayJob;
import cn.org.expect.intellij.idea.plugin.maven.listener.MavenSearchPluginListener;
import cn.org.expect.intellij.idea.plugin.maven.menu.SearchResultMenu;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationAliyun;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationArtifact;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationCVE;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationClass;
import cn.org.expect.intellij.idea.plugin.maven.navigation.SearchNavigationGradlePlugin;
import cn.org.expect.intellij.idea.plugin.maven.settings.MavenPluginSettings;
import cn.org.expect.jdk.JavaDialectFactory;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.MavenMessage;
import cn.org.expect.maven.MavenRuntimeException;
import cn.org.expect.maven.concurrent.ArtifactDownloadJob;
import cn.org.expect.maven.concurrent.SearchExtraJob;
import cn.org.expect.maven.concurrent.SearchPomJob;
import cn.org.expect.maven.impl.SearchNavigationCollectionImpl;
import cn.org.expect.maven.repository.SearchResult;
import cn.org.expect.maven.repository.aliyun.AliyunMavenRepository;
import cn.org.expect.maven.repository.clazz.ClassRepository;
import cn.org.expect.maven.repository.cve.CveInfo;
import cn.org.expect.maven.repository.cve.CveRepository;
import cn.org.expect.maven.repository.gradle.GradlePlugin;
import cn.org.expect.maven.repository.gradle.GradlePluginRepository;
import cn.org.expect.maven.repository.local.LocalRepositorySettings;
import cn.org.expect.maven.search.AbstractSearch;
import cn.org.expect.maven.search.ArtifactSearchNotification;
import cn.org.expect.maven.search.ArtifactSearchStatusMessageType;
import cn.org.expect.maven.search.SearchNavigation;
import cn.org.expect.maven.search.SearchNavigationCollection;
import cn.org.expect.util.StringUtils;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 搜索接口的实现类
 */
public class MavenSearchPlugin extends AbstractSearch implements MavenSearch, Disposable {
    private final static Log log = LogFactory.getLog(MavenSearchPlugin.class);

    /** Idea查询对话框对象 */
    private final IdeaSearchUI ideaUI;

    private final IdeaMavenPlugin ideaMavenPlugin;

    private final MavenSearchPluginContext context;

    private final MavenSearchPluginContributor contributor;

    private final MavenPluginSettings settings;

    private final MavenSearchPluginListener listener;

    private final SearchResultMenu resultMenu;

    public MavenSearchPlugin(AnActionEvent event) {
        super(MavenSearchPluginApplication.get());
        this.settings = this.getIoc().getBean(MavenPluginSettings.class);
        this.ideaUI = new IdeaSearchUI();
        this.ideaMavenPlugin = new IdeaMavenPlugin();
        this.ideaMavenPlugin.load(this.getIoc().getBean(LocalRepositorySettings.class), event); // 加载 Maven官方插件中配置的本地仓库参数
        this.setRepository(this.settings.getRepositoryId());

        // 初始化顺序不能调整
        this.context = new MavenSearchPluginContextImpl(event);
        this.contributor = new MavenSearchPluginContributor(this);
        this.listener = new MavenSearchPluginListener(this);
        this.resultMenu = new SearchResultMenu(this);
    }

    public MavenSearchPluginListener getSearchListener() {
        return listener;
    }

    public SearchResultMenu getResultMenu() {
        return resultMenu;
    }

    public IdeaSearchUI getIdeaUI() {
        return this.ideaUI;
    }

    public IdeaMavenPlugin getIdeaMavenPlugin() {
        return ideaMavenPlugin;
    }

    public MavenSearchPluginContext getContext() {
        return this.context;
    }

    public MavenPluginSettings getSettings() {
        return this.settings;
    }

    public LocalRepositorySettings getLocalRepositorySettings() {
        LocalRepositorySettings settings = this.getIoc().getBean(LocalRepositorySettings.class);
        if (this.ideaMavenPlugin.isMavenPluginEnable()) {
            this.ideaMavenPlugin.load(settings, this.getContext().getActionEvent());
        }

        if (settings.getRepository() == null) {
            log.warn("No Maven local repository found!");
        }
        return settings;
    }

    public void download(Artifact artifact) {
        this.aware(new ArtifactDownloadJob(artifact)).run();
    }

    public void asyncDownload(Artifact artifact) {
        this.async(new ArtifactDownloadJob(artifact));
    }

    /**
     * 返回搜索贡献者
     *
     * @return 搜索贡献者
     */
    public MavenSearchPluginContributor getContributor() {
        return this.contributor;
    }

    public void asyncSearch(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return;
        }

        this.context.setSelectedNavigation(null);

        // 更新等待信息与状态栏
        this.setProgress("maven.search.progress.text", this.getRepositoryInfo().getDisplayName());
        this.setStatusBar(ArtifactSearchStatusMessageType.RUNNING, "maven.search.pattern.text", StringUtils.escapeLineSeparator(pattern), this.getRepositoryInfo().getDisplayName());
        try {
            this.getInput().search(this, pattern);
        } catch (Throwable e) {
            throw new MavenRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public void asyncSearch(String groupId, String artifactId) {
        this.setStatusBar(ArtifactSearchStatusMessageType.RUNNING, "maven.search.extra.text", groupId, artifactId, this.getRepositoryInfo().getDisplayName());
        this.async(new SearchExtraJob(groupId, artifactId));
    }

    public SearchNavigationCollection toNavigationCollection(SearchResult result) {
        if (result == null) {
            return null;
        }

        // 阿里云
        if (result.isRepository(AliyunMavenRepository.class)) {
            List<SearchNavigation> list = new ArrayList<>();
            for (Object object : result.getList()) {
                list.add(new SearchNavigationAliyun((Artifact) object));
            }
            return new SearchNavigationCollectionImpl(list, result.getFoundNumber(), result.isHasMore());
        }

        // CVE 信息
        if (result.isRepository(CveRepository.class)) {
            List<SearchNavigation> list = new ArrayList<>();
            for (Object object : result.getList()) {
                list.add(new SearchNavigationCVE((CveInfo) object));
            }
            return new SearchNavigationCollectionImpl(list, result.getFoundNumber(), result.isHasMore());
        }

        // 类名搜索
        if (result.isRepository(ClassRepository.class)) {
            List<SearchNavigation> list = new ArrayList<>();
            for (Object object : result.getList()) {
                list.add(new SearchNavigationClass((Artifact) object));
            }
            return new SearchNavigationCollectionImpl(list, result.getFoundNumber(), result.isHasMore());
        }

        // Gradle 插件
        if (result.isRepository(GradlePluginRepository.class)) {
            List<SearchNavigation> list = new ArrayList<>();
            for (Object object : result.getList()) {
                list.add(new SearchNavigationGradlePlugin((GradlePlugin) object));
            }
            return new SearchNavigationCollectionImpl(list, result.getFoundNumber(), result.isHasMore());
        }

        // 工件
        List<SearchNavigation> list = new ArrayList<>();
        for (Object object : result.getList()) {
            list.add(new SearchNavigationArtifact((Artifact) object));
        }
        return new SearchNavigationCollectionImpl(list, result.getFoundNumber(), result.isHasMore());
    }

    public void asyncPom(Artifact artifact) {
        this.async(new SearchPomJob(artifact));
    }

    public void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    public void sendNotification(ArtifactSearchNotification type, String text, Object... array) {
        String message = MavenMessage.toString(text, array);
        NotificationType notificationType = ArtifactSearchNotification.toIdea(type);
        Project project = context.getActionEvent().getProject();
        if (project != null) {
            Notification notification = new Notification(this.getSettings().getId(), this.getSettings().getName(), message, notificationType);
            Notifications.Bus.notify(notification, project);
        }
    }

    public void sendNotification(ArtifactSearchNotification type, String text, String actionName, File file, Object... textParams) {
        Project project = context.getActionEvent().getProject();
        if (project != null) {
            NotificationType notificationType = ArtifactSearchNotification.toIdea(type);
            Notification notification = new Notification(this.getSettings().getId(), this.getSettings().getName(), MavenMessage.toString(text, textParams), notificationType);
            notification.addAction(new NotificationAction(MavenMessage.get(actionName)) {

                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    if (file.exists()) {
                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                        if (virtualFile != null) {
                            FileEditorManager.getInstance(project).openFile(virtualFile, true); // 使用 IDE 的文件编辑器打开文件
                        }
                    }
                }
            });
            Notifications.Bus.notify(notification, project);
        }
    }

    /**
     * 判断当前 Tab标签页 是否是自身
     *
     * @return 返回true表示是 false表示不是
     */
    public boolean isSelfTab() {
        if (this.getIdeaUI().getSearchEverywhereUI() == null) {
            return false;
        }

        String tabID = this.getIdeaUI().getSelectedTabID();
        return this.contributor.getSearchProviderId().equals(tabID);
    }

    /**
     * 判断当前 Tab标签页 是否是 All
     *
     * @return 返回true表示是 false表示不是
     */
    public boolean isAllTab() {
        if (this.getIdeaUI().getSearchEverywhereUI() == null) {
            return false;
        }

        String tabID = this.getIdeaUI().getSelectedTabID();
        return tabID.endsWith("." + this.settings.getAllTabName());
    }

    /**
     * 是否能执行查询
     *
     * @return 返回true表示支持查询 false表示不支持
     */
    public boolean canSearch() {
        if (this.getIdeaUI().getSearchEverywhereUI() == null) {
            return false;
        }

        String tabID = this.getIdeaUI().getSelectedTabID();
        return (this.getSettings().isSearchInAllTab() && tabID.endsWith("." + this.settings.getAllTabName())) || this.contributor.getSearchProviderId().equals(tabID);
    }

    /**
     * 在 Tab 页上添加提示信息
     */
    public void updateTabTooltip() {
        SearchEverywhereManager manager = SearchEverywhereManager.getInstance(this.context.getActionEvent().getProject());
        Map<String, String> map = JavaDialectFactory.get().getField(manager, "myTabsShortcutsMap");
        if (map != null) {
            String text = MavenMessage.get("maven.search.tab.tooltip.text", this.getSettings().getName(), this.getShortcutText("pressed F2"));
            map.put(this.contributor.getSearchProviderId(), text);
        }
    }

    /**
     * 返回快捷键的文本
     *
     * @param keystroke 快捷键信息，如：press shift
     * @return 快捷键的文本，如: ⇧
     */
    protected String getShortcutText(String keystroke) {
        try {
            KeyStroke shiftKeyStroke = KeyStroke.getKeyStroke(keystroke);
            Shortcut shiftShortcut = new KeyboardShortcut(shiftKeyStroke, null);
            return KeymapUtil.getShortcutText(shiftShortcut);
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage(), e);
            }
            return keystroke;
        }
    }

    public void setStatusBar(ArtifactSearchStatusMessageType type, String message, Object... messageParams) {
        if (this.isSelfTab()) {
            this.getIdeaUI().setStatusBar(type, MavenMessage.toString(message, messageParams));
        } else { // 如果标签页不是自身，则将状态栏恢复到原来的样式
            this.getIdeaUI().setStatusBar(null, "");
        }
    }

    public void display() {
        this.aware(new MavenPluginDisplayJob(this.context.getNavigationCollection())).run();
    }

    public void asyncDisplay() {
        this.async(new MavenPluginDisplayJob(this.context.getNavigationCollection()));
    }

    public void setProgress(String message, Object... array) {
        if (this.isSelfTab()) {
            this.getIdeaUI().getDisplay().setProgress(MavenMessage.toString(message, array));
        }
    }

    public void dispose() {
    }
}
