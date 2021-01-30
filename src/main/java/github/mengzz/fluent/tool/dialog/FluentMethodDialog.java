package github.mengzz.fluent.tool.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.JBTextField;
import github.mengzz.fluent.tool.setting.FluentToolSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * The type Fluent method dialog.
 *
 * @author mengzz
 */
public class FluentMethodDialog extends DialogWrapper {
    private static final String TITLE = "Fluent Method Name";
    private static final String PREFIX_LABEL = "Method Name";
    private JBTextField methodNameField;

    public FluentMethodDialog(@Nullable Project project) {
        super(project);
        setTitle(TITLE);
        super.init();
    }

    public String getName() {
        return methodNameField.getText();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return getPrefixComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return methodNameField;
    }

    @NotNull
    private JComponent getPrefixComponent() {
        methodNameField = new JBTextField(FluentToolSetting.getInstance().getFluentMethodName());
        LabeledComponent<JBTextField> labeledComponent = LabeledComponent.create(methodNameField, PREFIX_LABEL);
        labeledComponent.getLabel().setDisplayedMnemonic(KeyEvent.VK_N);
        return labeledComponent;
    }
}
