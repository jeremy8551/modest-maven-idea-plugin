package cn.org.expect.intellij.idea.plugin.maven;

import com.intellij.ide.actions.BigPopupUI;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManagerImpl;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereSpellingCorrector;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI;
import com.intellij.ide.lightEdit.LightEdit;
import com.intellij.ide.lightEdit.LightEditCompatible;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.SearchTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBInsets;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class SearchEverywhereUIFactory {

    protected JBPopup myBalloon;

    protected Dimension myBalloonFullSize;

    protected final Map<String, String> myTabsShortcutsMap;

    public SearchEverywhereUIFactory() {
        this.myTabsShortcutsMap = createShortcutsMap();
    }

    public SearchEverywhereUI createView(Project project, List<SearchEverywhereContributor<?>> contributors, @Nullable SearchEverywhereSpellingCorrector spellingCorrector) {
        if (LightEdit.owns(project)) {
            contributors = ContainerUtil.filter(contributors, (contributor) -> contributor instanceof LightEditCompatible);
        }
        SearchEverywhereUI view = new SearchEverywhereUI(project, contributors, myTabsShortcutsMap::get, spellingCorrector);

        view.setSearchFinishedHandler(() -> {
        });

        view.addViewTypeListener(viewType -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (myBalloon == null || myBalloon.isDisposed()) return;

                Dimension minSize = view.getMinimumSize();
                JBInsets.addTo(minSize, myBalloon.getContent().getInsets());
                myBalloon.setMinimumSize(minSize);

                if (viewType == BigPopupUI.ViewType.SHORT) {
                    myBalloonFullSize = myBalloon.getSize();
                    JBInsets.removeFrom(myBalloonFullSize, myBalloon.getContent().getInsets());
                    myBalloon.pack(false, true);
                } else {
                    if (myBalloonFullSize == null) {
                        myBalloonFullSize = view.getPreferredSize();
                        JBInsets.addTo(myBalloonFullSize, myBalloon.getContent().getInsets());
                    }
                    myBalloonFullSize.height = Integer.max(myBalloonFullSize.height, minSize.height);
                    myBalloonFullSize.width = Integer.max(myBalloonFullSize.width, minSize.width);
                    myBalloon.setSize(myBalloonFullSize);
                }
            });
        });

        DumbAwareAction.create(__ -> {
        }).registerCustomShortcutSet(SearchTextField.SHOW_HISTORY_SHORTCUT, view);

        DumbAwareAction.create(__ -> {
        }).registerCustomShortcutSet(SearchTextField.ALT_SHOW_HISTORY_SHORTCUT, view);

        return view;
    }

    protected static Map<String, String> createShortcutsMap() {
        Map<String, String> res = new HashMap<>();
        res.put(SearchEverywhereManagerImpl.ALL_CONTRIBUTORS_GROUP_ID, "Double Shift");
        addShortcut(res, "ClassSearchEverywhereContributor", "GotoClass");
        addShortcut(res, "FileSearchEverywhereContributor", "GotoFile");
        addShortcut(res, "SymbolSearchEverywhereContributor", "GotoSymbol");
        addShortcut(res, "ActionSearchEverywhereContributor", "GotoAction");
        addShortcut(res, "DbSETablesContributor", "GotoDatabaseObject");
        addShortcut(res, "TextSearchContributor", "TextSearchAction");
        return res;
    }

    protected static void addShortcut(Map<String, String> map, String contributorID, String actionID) {
        KeyboardShortcut shortcut = ActionManager.getInstance().getKeyboardShortcut(actionID);
        if (shortcut != null) map.put(contributorID, KeymapUtil.getShortcutText(shortcut));
    }
}
