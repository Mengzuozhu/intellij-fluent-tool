package github.mengzz.fluent.tool.dialog;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.JBTextField;
import github.mengzz.fluent.tool.setting.FluentToolSetting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author zuozhu.meng
 */
public class SetterMemberDialog<T extends NavigationItem> extends ListMemberDialog<T> {
    private static final String TITLE = "Fluent Setter Fields";
    private static final String PREFIX_LABEL = "Method Prefix";
    private JBTextField prefixField;

    public SetterMemberDialog(@NotNull Project project, List<T> fields) {
        super(project, fields, TITLE);
    }

    public String getPrefix() {
        return prefixField.getText();
    }

    @Override
    protected void customInit() {
        addComponent(getPrefixComponent());
    }

    @NotNull
    private Component getPrefixComponent() {
        prefixField = new JBTextField(FluentToolSetting.getInstance().getFluentSetterPrefix());
        LabeledComponent<JBTextField> labeledComponent = LabeledComponent.create(prefixField, PREFIX_LABEL);
        labeledComponent.getLabel().setDisplayedMnemonic(KeyEvent.VK_P);
        return labeledComponent;
    }
}
