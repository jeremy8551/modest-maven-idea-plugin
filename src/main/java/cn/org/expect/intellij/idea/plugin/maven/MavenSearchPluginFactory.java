package cn.org.expect.intellij.idea.plugin.maven;

import cn.org.expect.intellij.idea.plugin.maven.action.MavenSearchPluginPinAction;
import cn.org.expect.intellij.idea.plugin.maven.concurrent.MavenPluginJob;
import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class MavenSearchPluginFactory implements SearchEverywhereContributorFactory<Object> {
    private final static Log log = LogFactory.getLog(MavenSearchPluginFactory.class);

    public SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent event) {
        try {
            MavenSearchPluginPinAction.PIN.dispose();
            MavenSearchPluginContributor contributor = this.create(event);
            contributor.getPlugin().async(new MavenPluginJob());
            return contributor;
        } catch (Throwable e) {
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public @NotNull MavenSearchPluginContributor create(@NotNull AnActionEvent event) {
        MavenSearchPlugin plugin = new MavenSearchPlugin(event);
        plugin.updateTabTooltip();
        return plugin.getContributor();
    }
}
