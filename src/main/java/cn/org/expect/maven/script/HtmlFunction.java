package cn.org.expect.maven.script;

import java.util.ArrayList;
import java.util.List;

import cn.org.expect.script.annotation.EasyVariableExtension;
import cn.org.expect.util.CharsetName;
import cn.org.expect.util.StringUtils;
import cn.org.expect.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@EasyVariableExtension
public class HtmlFunction {

    public static Document newDocument(String xml) {
        return XMLUtils.newDocument(xml, CharsetName.UTF_8); // 将字符串转为 Node
    }

    public static String readTag(String variable, String tagName, int from) {
        return XMLUtils.readTag(variable, tagName, from, false);
    }

    public static String readTag(String variable, String tagName, long from) {
        return readTag(variable, tagName, (int) from);
    }

    public static List<Node> filter(List<Node> nodeList, String... expressions) {
        List<String[]> filters = new ArrayList<>();
        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            String[] property = StringUtils.splitProperty(expression);
            if (property == null) {
                throw new IllegalArgumentException(expression);
            } else {
                filters.add(property);
            }
        }

        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);

            boolean match = true;
            for (String[] filter : filters) {
                String name = filter[0];
                String value = filter[1];

                String attribute = XMLUtils.getAttribute(node, name, "");
                if (!value.equals(attribute)) {
                    match = false;
                }
            }

            if (match) {
                list.add(node);
            }
        }

        return list;
    }

    public static String get(Node node, String name) {
        return XMLUtils.getAttribute(node, name, "");
    }

    public static List<Node> getChildNodes(Node node, String... array) {
        NodeList nodeList = node.getChildNodes();
        int length = nodeList.getLength();
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Node item = nodeList.item(i);
            if (array.length == 0 || StringUtils.inArrayIgnoreCase(item.getNodeName(), array)) {
                list.add(item);
            }
        }
        return list;
    }

    public static String getName(Node node) {
        return node.getNodeName();
    }

    public static String getText(Node node) {
        return node.getTextContent();
    }
}
