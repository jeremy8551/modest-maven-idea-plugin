package cn.org.expect.maven.pom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import cn.org.expect.log.Log;
import cn.org.expect.log.LogFactory;
import cn.org.expect.maven.Artifact;
import cn.org.expect.maven.concurrent.ArtifactDownloadJob;
import cn.org.expect.maven.pom.impl.SimpleDeveloper;
import cn.org.expect.maven.pom.impl.SimpleLicense;
import cn.org.expect.maven.pom.impl.SimplePom;
import cn.org.expect.maven.repository.local.LocalRepository;
import cn.org.expect.maven.search.Search;
import cn.org.expect.util.CharsetName;
import cn.org.expect.util.FileUtils;
import cn.org.expect.util.IO;
import cn.org.expect.util.XMLUtils;
import org.w3c.dom.Node;

public class PomRepositoryAnalysis {
    protected final static Log log = LogFactory.getLog(PomRepositoryAnalysis.class);

    public PomRepositoryAnalysis() {
    }

    public SimplePom parsePomXml(Node project) {
        SimplePom info = new SimplePom();
        XMLUtils.runChildNodes(project, (i, node) -> {
            if ("url".equalsIgnoreCase(node.getNodeName())) {
                info.setUrl(node.getTextContent());
            }

            if ("packaging".equalsIgnoreCase(node.getNodeName())) {
                info.setPackaging(node.getTextContent());
            }

            if ("description".equalsIgnoreCase(node.getNodeName())) {
                info.setDescription(node.getTextContent());
            }

            if ("parent".equalsIgnoreCase(node.getNodeName())) {
                XMLUtils.runChildNodes(node, (j, item) -> {
                    if ("groupId".equalsIgnoreCase(item.getNodeName())) {
                        info.getParent().setGroupId(item.getTextContent());
                    } else if ("artifactId".equalsIgnoreCase(item.getNodeName())) {
                        info.getParent().setArtifactId(item.getTextContent());
                    } else if ("version".equalsIgnoreCase(item.getNodeName())) {
                        info.getParent().setVersion(item.getTextContent());
                    }
                });
            }

            if ("scm".equalsIgnoreCase(node.getNodeName())) {
                XMLUtils.runChildNodes(node, (j, item) -> {
                    if ("url".equalsIgnoreCase(item.getNodeName())) {
                        info.getScm().setUrl(item.getTextContent());
                    } else if ("connection".equalsIgnoreCase(item.getNodeName())) {
                        info.getScm().setConnection(item.getTextContent());
                    } else if ("developerConnection".equalsIgnoreCase(item.getNodeName())) {
                        info.getScm().setDeveloperConnection(item.getTextContent());
                    } else if ("tag".equalsIgnoreCase(item.getNodeName())) {
                        info.getScm().setTag(item.getTextContent());
                    }
                });
            }

            if ("issueManagement".equalsIgnoreCase(node.getNodeName())) {
                XMLUtils.runChildNodes(node, (j, item) -> {
                    if ("system".equalsIgnoreCase(item.getNodeName())) {
                        info.getIssue().setSystem(item.getTextContent());
                    } else if ("url".equalsIgnoreCase(item.getNodeName())) {
                        info.getIssue().setUrl(item.getTextContent());
                    }
                });
            }

            if ("licenses".equalsIgnoreCase(node.getNodeName())) {
                XMLUtils.runChildNodes(node, (j, item) -> {
                    if ("license".equalsIgnoreCase(item.getNodeName())) {
                        SimpleLicense license = new SimpleLicense();
                        XMLUtils.runChildNodes(item, (k, property) -> {
                            if ("name".equalsIgnoreCase(property.getNodeName())) {
                                license.setName(property.getTextContent());
                            } else if ("url".equalsIgnoreCase(property.getNodeName())) {
                                license.setUrl(property.getTextContent());
                            } else if ("distribution".equalsIgnoreCase(property.getNodeName())) {
                                license.setDistribution(property.getTextContent());
                            } else if ("comments".equalsIgnoreCase(property.getNodeName())) {
                                license.setComments(property.getTextContent());
                            }
                        });
                        info.getLicenses().add(license);
                    }
                });
            }

            if ("developers".equalsIgnoreCase(node.getNodeName())) {
                XMLUtils.runChildNodes(node, (j, item) -> {
                    if ("developer".equalsIgnoreCase(item.getNodeName())) {
                        SimpleDeveloper developer = new SimpleDeveloper();
                        XMLUtils.runChildNodes(item, (k, property) -> {
                            if ("id".equalsIgnoreCase(property.getNodeName())) {
                                developer.setId(property.getTextContent());
                            } else if ("name".equalsIgnoreCase(property.getNodeName())) {
                                developer.setName(property.getTextContent());
                            } else if ("email".equalsIgnoreCase(property.getNodeName())) {
                                developer.setEmail(property.getTextContent());
                            } else if ("timezone".equalsIgnoreCase(property.getNodeName())) {
                                developer.setTimezone(property.getTextContent());
                            } else if ("organization".equalsIgnoreCase(property.getNodeName())) {
                                developer.setOrganization(property.getTextContent());
                            } else if ("organizationUrl".equalsIgnoreCase(property.getNodeName())) {
                                developer.setOrganizationUrl(property.getTextContent());
                            } else if ("roles".equalsIgnoreCase(property.getNodeName())) {
                                XMLUtils.runChildNodes(property, (m, role) -> {
                                    if ("role".equalsIgnoreCase(role.getNodeName())) {
                                        developer.getRoles().add(role.getTextContent());
                                    }
                                });
                            }
                        });
                        info.getDevelopers().add(developer);
                    }
                });
            }
        });
        return info;
    }

    public byte[] readPomXml(Search search, Artifact artifact) throws IOException {
        LocalRepository repository = search.getLocalRepository();
        File pomFile = repository.getFile(artifact, "pom");
        if (FileUtils.isFile(pomFile)) {
            return FileUtils.readline(pomFile, CharsetName.UTF_8, 0).getBytes(StandardCharsets.UTF_8);
        }

        // 尝试解析jar文件
        File jarfile = repository.getJarfile(artifact);
        if (FileUtils.isFile(jarfile)) {
            return this.readPomXmlFromJar(artifact, jarfile);
        }

        // 下载 jar 文件
        search.asyncDownload(artifact);
        search.display();
        search.getService().waitFor(ArtifactDownloadJob.class, job -> job.getArtifact().equals(artifact), 0);

        // 再尝试 pom 文件
        if (FileUtils.isFile(pomFile)) {
            return FileUtils.readline(pomFile, CharsetName.UTF_8, 0).getBytes(StandardCharsets.UTF_8);
        }
        log.warn("{} {} not exists!", artifact.toMavenId(), pomFile);

        // 尝试解析 jar 文件
        jarfile = repository.getJarfile(artifact);
        if (FileUtils.isFile(jarfile)) {
            byte[] bytes = this.readPomXmlFromJar(artifact, jarfile);
            if (bytes != null) {
                return bytes;
            }
        }

        log.warn("{} POM not found in {}", artifact.toMavenId(), jarfile);
        return null;
    }

    private byte[] readPomXmlFromJar(Artifact artifact, File jarfile) throws IOException {
        try (JarFile jarFile = new JarFile(jarfile.getAbsolutePath())) {
            JarEntry entry = jarFile.getJarEntry("META-INF/maven/" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/pom.xml");
            if (entry != null) {
                return IO.read(jarFile.getInputStream(entry));
            }
        }
        return null;
    }
}
