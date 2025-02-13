package cn.org.expect.maven;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.org.expect.ioc.EasyBeanEntry;
import cn.org.expect.ioc.EasyContext;
import cn.org.expect.maven.impl.ArtifactOptionImpl;
import cn.org.expect.maven.repository.ArtifactDownloader;
import cn.org.expect.maven.repository.Repository;
import cn.org.expect.maven.repository.aliyun.AliyunMavenRepository;
import cn.org.expect.maven.repository.cve.CveRepository;
import cn.org.expect.maven.repository.gradle.GradlePluginRepository;
import cn.org.expect.util.ClassUtils;

public interface MavenEasyContext extends EasyContext {

    List<String> REPOSITORY = new Vector<>();

    default Repository getRepository(String name) {
        List<EasyBeanEntry> list = this.getRepository();
        for (EasyBeanEntry entry : list) {
            if (entry.getName().equalsIgnoreCase(name)) {
                return this.newInstance(entry.getType());
            }
        }
        return null;
    }

    default List<EasyBeanEntry> getRepository() {
        List<EasyBeanEntry> list = this.getBeanEntryCollection(Repository.class).values();
        Map<String, EasyBeanEntry> map = new LinkedHashMap<>();
        for (EasyBeanEntry entry : list) {
            map.put(entry.getName(), entry);
        }

        for (int i = 0; i < list.size(); i++) {
            EasyBeanEntry entry = list.get(i);
            if (ClassUtils.inArray(entry.getType(), CveRepository.class, GradlePluginRepository.class, AliyunMavenRepository.class)) {
                list.remove(entry);
            }
        }

        for (String repositoryId : MavenEasyContext.REPOSITORY) {
            if (repositoryId.startsWith("-")) {
                String name = repositoryId.substring(1);
                for (int i = 0; i < list.size(); i++) {
                    EasyBeanEntry entry = list.get(i);
                    if (entry.getName().equalsIgnoreCase(name)) {
                        list.remove(entry);
                    }
                }
            } else {
                EasyBeanEntry entry = map.get(repositoryId);
                if (entry != null && !list.contains(entry)) {
                    list.add(entry);
                }
            }
        }

        return list;
    }

    /**
     * 已注册工件仓库的数组
     *
     * @return 仓库数组
     */
    default MavenOption[] getRepositoryOptions() {
        List<EasyBeanEntry> list = this.getRepository();
        list.sort((b1, b2) -> b2.getOrder() - b1.getOrder());
        return MavenEasyContext.toOptionArray(list);
    }

    /**
     * 已注册工件仓库的数组
     *
     * @return 仓库数组
     */
    default MavenOption[] getDownloaderOptions() {
        List<EasyBeanEntry> list = this.getBeanEntryCollection(ArtifactDownloader.class).values();
        list.sort((b1, b2) -> b2.getOrder() - b1.getOrder());
        return MavenEasyContext.toOptionArray(list);
    }

    static MavenOption[] toOptionArray(List<EasyBeanEntry> list) {
        int size = list.size();
        List<MavenOption> optionList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            try {
                optionList.add(new ArtifactOptionImpl(list.get(i).getName()));
            } catch (Throwable ignored) {
            }
        }
        return optionList.toArray(new MavenOption[optionList.size()]);
    }
}
