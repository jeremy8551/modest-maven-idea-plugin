package cn.org.expect.maven.repository.central;

import java.util.Comparator;

import cn.org.expect.maven.Artifact;
import cn.org.expect.util.DateComparator;

public class CentralVersionComparator implements Comparator<Artifact> {

    public int compare(Artifact m1, Artifact m2) {
        int dv = DateComparator.INSTANCE.compare(m2.getTimestamp(), m1.getTimestamp());
        if (dv != 0) {
            return dv;
        }
        return m2.getVersion().compareTo(m1.getVersion());
    }
}
