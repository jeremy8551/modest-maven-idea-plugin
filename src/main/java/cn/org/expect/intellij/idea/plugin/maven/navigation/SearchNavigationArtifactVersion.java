package cn.org.expect.intellij.idea.plugin.maven.navigation;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import cn.org.expect.collection.CaseSensitivSet;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPlugin;
import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginIcon;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.concurrent.ArtifactDownloadJob;
import cn.org.expect.maven.concurrent.MavenJob;
import cn.org.expect.maven.pom.Developer;
import cn.org.expect.maven.pom.License;
import cn.org.expect.maven.pom.Parent;
import cn.org.expect.maven.pom.Pom;
import cn.org.expect.maven.pom.PomRepository;
import cn.org.expect.maven.pom.Scm;
import cn.org.expect.util.ArrayUtils;
import cn.org.expect.util.Dates;
import cn.org.expect.util.StringUtils;

public class SearchNavigationArtifactVersion extends AbstractSearchNavigation {

    public SearchNavigationArtifactVersion(Artifact artifact) {
        super(artifact);
        this.setDepth(2);
        this.setPresentableText(artifact.getVersion());
        this.setLocationString("");
        this.setMiddleText(artifact.getTimestamp() == null ? "" : StringUtils.replaceAll(StringUtils.left(Dates.format19(artifact.getTimestamp()), 16), " 00:00", ""));
        this.setLeftIcon(null);
        this.setRightIcon(MavenSearchPluginIcon.RIGHT_REMOTE);
        this.setRightText("");
    }

    public boolean supportMenu() {
        return true;
    }

    public void displayMenu(JPopupMenu topMenu, int selectedIndex) {
        MavenSearchPlugin plugin = this.getSearch();
        plugin.getResultMenu().displayItemMenu(plugin, this, topMenu, selectedIndex, 30);
    }

    public boolean supportUnfold() {
        Artifact artifact = this.getResult();
        return this.getSearch().getLocalRepository().exists(artifact);
    }

    public boolean supportFold() {
        return true;
    }

    public void setUnfold(Runnable command) {
        this.setFold(false);
        MavenSearchPlugin plugin = this.getSearch();
        if (this.childList.size() == 0) {
            setLeftIcon(MavenSearchPluginIcon.LEFT_WAITING);
            Artifact artifact = this.getResult();
            PomRepository pomRepository = plugin.getPomRepository();
            Pom pom = pomRepository.getDatabase().select(artifact);
            if (pom != null) {
                try {
                    this.addChilds(plugin, artifact, pom);
                } finally {
                    this.setLeftIcon(null);
                    if (command != null) {
                        command.run();
                    }
                }
                return;
            }

            plugin.async(new MavenJob("") {
                public int execute() throws Exception {
                    try {
                        Pom pom = pomRepository.query(plugin, artifact);
                        addChilds(plugin, artifact, pom);
                    } finally {
                        setLeftIcon(null);
                        if (command != null) {
                            command.run();
                        }
                    }
                    return 0;
                }
            });
        }
    }

    public void setFold() {
        this.setFold(true);
        this.setLeftIcon(null);
    }

    public void update() {
        Artifact artifact = this.getResult();
        MavenSearchPlugin search = this.getSearch();

        // 如果正在下载工件，则更新图标
        if (search.getService().isRunning(ArtifactDownloadJob.class, job -> job.getArtifact().equals(artifact))) { // 正在下载
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_DOWNLOAD);
            return;
        }

        // 如果工件已下载，则更新图标
        if (search.getLocalRepository().exists(artifact)) {
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_LOCAL);
        } else {
            this.setRightIcon(MavenSearchPluginIcon.RIGHT_REMOTE);
        }
    }

    /**
     * 解析时区字符串为 ZoneId。
     *
     * @param timezone 时区字符串，例如 "+8", "-05:30", "Asia/Shanghai"
     * @return ZoneId 对象，解析失败返回系统默认时区
     */
    private String parseTimezone(String timezone) {
        if (StringUtils.isBlank(timezone)) {
            return null;
        }

        try {
            ZoneId zoneId = ZoneId.of(timezone);
            return zoneId + " " + this.getZone(zoneId); // 尝试解析为标准时区 ID
        } catch (Throwable ex) {
            try {
                ZoneId zoneId = ZoneId.ofOffset("UTC", ZoneOffset.of(timezone)); // 如果解析失败，尝试解析为 UTC 偏移量
                return zoneId + " " + this.getZone(zoneId);
            } catch (Throwable e) {
                return timezone;
            }
        }
    }

    private String getZone(ZoneId offset) {
        CaseSensitivSet names = new CaseSensitivSet();
        Set<String> set = ZoneId.getAvailableZoneIds();
        for (String zoneIdStr : set) {
            ZoneId zoneId = ZoneId.of(zoneIdStr);
            ZonedDateTime zdt = ZonedDateTime.now(zoneId);
            if (zdt.getOffset().equals(offset)) {
                String str = StringUtils.trimBlank(zoneId.toString()); // GMT +08:00: Asia/Kuching
                String name = ArrayUtils.first(StringUtils.split(str, '/'));
                if (name.equals("PRC")) {
                    names.add("China");
                    continue;
                }

                if (this.match(name)) {
                    names.add(name);
                }
            }
        }

        if (names.isEmpty()) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        buf.append('(');
        for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
            buf.append(it.next());
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(')');
        return buf.toString();
    }

    private void addChilds(MavenSearchPlugin plugin, Artifact artifact, Pom pom) {
        if (pom != null) {
            Parent parent = pom.getParent();
            if (StringUtils.isNotBlank(parent.getGroupId()) && StringUtils.isNotBlank(parent.getArtifactId())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_PARENT, "", parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion(), "Parent"));
            }

            if (StringUtils.isNotBlank(pom.getDescription())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_PROJECT, "", pom.getDescription(), "Description"));
            }

            if (StringUtils.isNotBlank(pom.getProjectUrl())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_PROJECT, "", pom.getProjectUrl(), "URL"));
            }

            // 源代码管理系统
            Scm scm = pom.getScm();
            if (StringUtils.isNotBlank(scm.getConnection())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_SCM, "", scm.getConnection(), "Connection"));
            }

            if (StringUtils.isNotBlank(scm.getDeveloperConnection())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_SCM, "", scm.getDeveloperConnection(), "DeveloperConnection"));
            }

            if (StringUtils.isNotBlank(scm.getUrl())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_SCM, "", scm.getUrl(), "URL"));
            }

            if (StringUtils.isNotBlank(scm.getTag())) {
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_SCM, "", scm.getTag(), "Tag"));
            }

            // 开发人员
            List<Developer> developers = pom.getDevelopers();
            for (int i = 0; i < developers.size(); i++) {
                Developer developer = developers.get(i);
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", developer.getName(), "Name"));

                if (StringUtils.isNotBlank(developer.getEmail())) {
                    childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", developer.getEmail(), "Email"));
                }

                if (StringUtils.isNotBlank(developer.getOrganization())) {
                    childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", developer.getOrganization(), "Organization"));
                }

                if (StringUtils.isNotBlank(developer.getOrganizationUrl())) {
                    childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", developer.getOrganizationUrl(), "OrganizationUrl"));
                }

                if (!developer.getRoles().isEmpty()) {
                    for (String role : developer.getRoles()) {
                        childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", role, "Role"));
                    }
                }

                if (StringUtils.isNotBlank(developer.getTimezone())) {
                    String timezone = parseTimezone(developer.getTimezone()); // 测试 org.apache.maven:maven-parent
                    if (StringUtils.isNotBlank(timezone)) {
                        childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_DEVELOPER, "", timezone, "Timezone"));
                    }
                }
            }

            // 开源许可证
            List<License> licenses = pom.getLicenses();
            for (int i = 0; i < licenses.size(); i++) {
                License license = licenses.get(i);
                childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_LICENSE, "", license.getName(), "Name"));

                if (StringUtils.isNotBlank(license.getUrl())) {
                    childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_LICENSE, "", license.getUrl(), "URL"));
                }

                if (StringUtils.isNotBlank(license.getComments())) {
                    childList.add(plugin, new SearchNavigationArtifactDetail(artifact, MavenSearchPluginIcon.RIGHT_LICENSE, "", license.getComments(), "Comment"));
                }
            }
        }
    }

    private boolean match(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }

        if (StringUtils.inArrayIgnoreCase(str, "etc", "cet", "systemv")) {
            return false;
        }

        // 不能有数字
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!StringUtils.isLetter(c)) {
                return false;
            }
        }

        // 是否是全大写
        boolean litte = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isUpperCase(c)) {
                litte = true;
            }
        }
        return litte;
    }
}
