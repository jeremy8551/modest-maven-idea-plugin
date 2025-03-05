package cn.org.expect.intellij.idea.plugin.maven.navigation;

import java.awt.*;
import javax.swing.*;

import cn.org.expect.util.StringUtils;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class SearchNavigationListCellRenderer extends ColoredListCellRenderer<Object> {

    private final String text;

    private final Icon icon;

    private final int style;

    private final Color fgColor;

    private String smallText;

    private int smallTextStyle;

    private Color smallTextColor;

    public SearchNavigationListCellRenderer(Icon icon, String text, int style, Color fgColor) {
        super();
        this.icon = icon;
        this.text = StringUtils.coalesce(text, "");
        this.style = style;
        this.fgColor = fgColor;
    }

    public SearchNavigationListCellRenderer(Icon icon, String text, int style, Color fgColor, String smallText, int smallTextStyle, Color smallTextColor) {
        super();
        this.icon = icon;
        this.text = StringUtils.coalesce(text, "");
        this.style = style;
        this.fgColor = fgColor;
        this.smallText = smallText;
        this.smallTextStyle = smallTextStyle;
        this.smallTextColor = smallTextColor;
    }

    protected void customizeCellRenderer(@NotNull JList<?> list, Object value, int index, boolean selected, boolean hasFocus) {
        this.myIconTextGap = 7;
        this.getIpad().left = 0;
        this.setIcon(this.icon == null ? IconUtil.getEmptyIcon(false) : this.icon);
        this.append(this.text, new SimpleTextAttributes(this.style, this.fgColor));

        if (StringUtils.isNotBlank(this.smallText)) {
            this.append(" " + this.smallText, new SimpleTextAttributes(this.smallTextStyle, this.smallTextColor));
        }

        Color bgColor = UIUtil.getListBackground();
        this.setBackground(selected ? UIUtil.getListSelectionBackground(true) : bgColor);
    }

    public int computeWith() {
        return (int) this.computePreferredSize(false).getWidth();
    }

    public int getGapWidth() {
        return this.getIpad().left + this.getIcon().getIconWidth() + this.getIconTextGap();
    }
}
