package cn.org.expect.maven.repository.central;

import java.util.Comparator;

import cn.org.expect.maven.Artifact;
import cn.org.expect.util.DateComparator;

public class CentralComparator implements Comparator<Artifact> {

    public int compare(Artifact a1, Artifact a2) {
        // 版本数倒序
        int vv = a2.getVersionCount() - a1.getVersionCount();
        if (vv != 0) {
            return vv;
        }

        // 发布时间倒序
        int tv = DateComparator.INSTANCE.compare(a2.getTimestamp(), a1.getTimestamp());
        if (tv != 0) {
            return tv;
        }

        // 正序
        int gv = a1.getGroupId().compareTo(a2.getGroupId());
        if (gv != 0) {
            return gv;
        }

        // 正序
        return a1.getArtifactId().compareTo(a2.getArtifactId());
    }
}
