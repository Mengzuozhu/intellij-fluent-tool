package github.mengzz.fluent.tool.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.Function;
import github.mengzz.fluent.tool.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The type List member dialog.
 *
 * @param <T> the type parameter
 * @author mengzz
 */
public class ListMemberDialog<T extends PsiNamedElement> extends DialogWrapper {
    protected final List<T> members;
    private JPanel mainPanel = new JPanel();
    private JBList<T> jFieldList;
    private CollectionListModel<T> listModel;
    private Comparator<T> comparator;

    public ListMemberDialog(@NotNull Project project, List<T> members, String title, Comparator<T> comparator) {
        super(project);
        this.members = members;
        this.comparator = comparator;
        setTitle(title);
        initDialog(members);
    }

    public ListMemberDialog(@NotNull Project project, List<T> members, String title) {
        this(project, members, title, (o1, o2) -> StringUtil.naturalCompare(o1.getName(), o2.getName()));
    }

    /**
     * Gets remained members.
     *
     * @return the remained members
     */
    public List<T> getRemainedMembers() {
        return listModel.getItems();
    }

    /**
     * Add component.
     *
     * @param comp the comp
     */
    protected void addComponent(Component comp) {
        mainPanel.add(comp);
    }

    /**
     * Update model items.
     *
     * @param items the items
     */
    protected void updateModelItems(List<T> items) {
        listModel = new CollectionListModel<>(items);
        jFieldList.setModel(listModel);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    /**
     * Custom init.
     */
    protected void customInit() {
    }

    private void initDialog(List<T> members) {
        mainPanel.setLayout(new VerticalLayout(1));
        if (comparator != null) {
            mainPanel.add(getGroupToolbar());
        }
        mainPanel.add(buildListModelPanel(members));
        customInit();
        super.init();
    }

    @NotNull
    private JComponent getGroupToolbar() {
        SortListAction sortListAction = new SortListAction();
        sortListAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.ALT_DOWN_MASK)), mainPanel);
        DefaultActionGroup group = new DefaultActionGroup(sortListAction);
        return ActionManager.getInstance().createActionToolbar("ListMemberDialog", group, true).getComponent();
    }

    @SuppressWarnings("unchecked")
    private JPanel buildListModelPanel(List<T> members) {
        listModel = new CollectionListModel<>(members);
        jFieldList = new JBList<>(listModel);
        jFieldList.setCellRenderer(getCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jFieldList)
                .disableAddAction();
        addListSpeedSearch(jFieldList);
        return decorator.createPanel();
    }

    @NotNull
    private DefaultPsiElementCellRenderer getCellRenderer() {
        return new DefaultPsiElementCellRenderer();
    }

    private void addListSpeedSearch(JBList<T> jFieldList) {
        DefaultPsiElementCellRenderer cellRenderer = getCellRenderer();
        new ListSpeedSearch<>(jFieldList, (Function<Object, String>) obj -> {
            PsiElement element = PluginUtil.convertAs(obj, PsiElement.class);
            if (element != null) {
                return cellRenderer.getElementText(element);
            }
            return obj.toString();
        });
    }

    private class SortListAction extends ToggleAction {
        private List<T> beforeSortedItems;
        private boolean isSelectedSort = false;

        public SortListAction() {
            super(IdeBundle.message("action.sort.alphabetically"),
                    IdeBundle.message("action.sort.alphabetically"), AllIcons.ObjectBrowser.Sorted);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent event) {
            return isSelectedSort;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent event, boolean flag) {
            isSelectedSort = flag;
            if (isSelectedSort) {
                beforeSortedItems = listModel.getItems();
                List<T> sortItems = new ArrayList<>(listModel.getItems());
                sortItems.sort(comparator);
                updateModelItems(sortItems);
            } else if (beforeSortedItems != null) {
                updateModelItems(beforeSortedItems);
                beforeSortedItems = null;
            }
        }
    }

}
