package github.mengzz.fluent.tool.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.components.JBTextField;
import github.mengzz.fluent.tool.setting.FluentToolSetting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author mengzz
 */
public class ConstructorMemberDialog<T extends PsiNamedElement> extends ListMemberDialog<T> {
    private static final String TITLE = "Fluent Static Constructor Methods";
    private static final String LABEL = "Method Name";
    private JBTextField nameField;

    public ConstructorMemberDialog(@NotNull Project project, List<T> fields) {
        super(project, fields, TITLE, null);
    }

    public String getText() {
        return nameField.getText();
    }

    @Override
    protected void customInit() {
        addComponent(getMethodNameComponent());
    }

    @NotNull
    private Component getMethodNameComponent() {
        nameField = new JBTextField(FluentToolSetting.getInstance().getConstructMethodName());
        LabeledComponent<JBTextField> labeledComponent = LabeledComponent.create(nameField, LABEL);
        labeledComponent.getLabel().setDisplayedMnemonic(KeyEvent.VK_N);
        return labeledComponent;
    }
}
