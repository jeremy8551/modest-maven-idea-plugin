package cn.org.expect.maven;

import cn.org.expect.intellij.idea.plugin.maven.MavenSearchPluginApplication;
import cn.org.expect.util.ResourcesUtils;
import cn.org.expect.util.StringUtils;
import com.intellij.CommonBundle;

public class MavenMessage {

    static {
        // 使用中文的资源文件
        if ("取消".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "zh_CN");
        } else if ("Cancelación".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "es_ES");
        } else if ("Annulation".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "fr_FR");
        } else if ("キャンセル".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "ja_JP");
        } else if ("취소".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "ko_KR");
        } else if ("Отменить".equals(CommonBundle.getCancelButtonText())) {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "ru_RU");
        } else {
            System.setProperty(ResourcesUtils.PROPERTY_LOCALE, "");
        }
        ResourcesUtils.getRepository().load(MavenSearchPluginApplication.get().getClassLoader());
    }

    private MavenMessage() {
    }

    /**
     * 判断属性是否存在
     *
     * @param key 属性名
     * @return 返回true表示属性存在，false表示属性不存在
     */
    public static boolean contains(String key) {
        return StringUtils.isNotBlank(key) && ResourcesUtils.existsMessage(key);
    }

    /**
     * 返回国际化信息
     *
     * @param key  资源编号
     * @param args 参数数组
     * @return 字符串
     */
    public static String get(String key, Object... args) {
        return ResourcesUtils.getMessage(key, args);
    }

    /**
     * 转为字符串
     *
     * @param text  文本信息
     * @param array 文本的参数
     * @return 字符串
     */
    public static String toString(String text, Object... array) {
        if (MavenMessage.contains(text)) {
            return MavenMessage.get(text, array);
        } else {
            return StringUtils.replaceEmptyHolder(text, array);
        }
    }
}
