package cn.org.expect.maven.repository.gradle;

import java.util.Comparator;

import cn.org.expect.util.DateComparator;

public class GradlePluginComparator implements Comparator<GradlePlugin> {

    public int compare(GradlePlugin g1, GradlePlugin g2) {
        int dv = DateComparator.INSTANCE.compare(g2.getDate(), g1.getDate());
        if (dv != 0) {
            return dv;
        } else {
            return g2.getVersion().compareTo(g1.getVersion());
        }
    }
}
