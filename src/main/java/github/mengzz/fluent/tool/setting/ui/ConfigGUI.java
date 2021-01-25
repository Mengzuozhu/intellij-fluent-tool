package github.mengzz.fluent.tool.setting.ui;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.IdeBorderFactory;
import github.mengzz.fluent.tool.FluentToolConstant;
import github.mengzz.fluent.tool.setting.FluentToolSetting;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * The type Config gui.
 *
 * @author mengzz
 */
public class ConfigGUI extends JPanel implements SearchableConfigurable {
    private static final String DISPLAY_NAME = "Fluent Tool";
    private FluentToolSetting settings;

    private JPanel mainPanel;
    private JPanel fluentStaticConstructPanel;
    private JPanel fluentSetterPanel;
    private JPanel fluentCallPanel;
    private JPanel fluentMethodPanel;

    private JTextField fluentConstructNameText;
    private JTextField fluentSetterPrefixText;
    private JTextField fluentMethodNameText;
    private JCheckBox addBuildMethodIfExistCheck;

    public ConfigGUI() {
        super();
        settings = FluentToolSetting.getInstance();
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        setBorder();
    }

    @Override
    public @Nullable JComponent createComponent() {
        return this;
    }

    @Override
    public boolean isModified() {
        boolean modified = isTextModified(fluentConstructNameText, settings.getConstructMethodName());
        modified = modified || isTextModified(fluentSetterPrefixText, settings.getFluentSetterPrefix());
        modified = modified || isTextModified(fluentMethodNameText, settings.getFluentMethodName());
        modified = modified || addBuildMethodIfExistCheck.isSelected() != settings.isAddBuildMethodIfExist();
        return modified;
    }

    @Override
    public void apply() {
        settings.setConstructMethodName(fluentConstructNameText.getText());
        settings.setFluentSetterPrefix(fluentSetterPrefixText.getText());
        settings.setFluentMethodName(fluentMethodNameText.getText());
        settings.setAddBuildMethodIfExist(addBuildMethodIfExistCheck.isSelected());
    }

    @Override
    public void reset() {
        fluentConstructNameText.setText(settings.getConstructMethodName());
        fluentSetterPrefixText.setText(settings.getFluentSetterPrefix());
        fluentMethodNameText.setText(settings.getFluentMethodName());
        addBuildMethodIfExistCheck.setSelected(settings.isAddBuildMethodIfExist());
    }

    @Override
    public @NotNull String getId() {
        return DISPLAY_NAME;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return DISPLAY_NAME;
    }

    private boolean isTextModified(JTextField textField, String config) {
        return !textField.getText().equals(config);
    }

    private void setBorder() {
        fluentStaticConstructPanel.setBorder(IdeBorderFactory.createTitledBorder("Fluent Static Constructor", false));
        fluentSetterPanel.setBorder(IdeBorderFactory.createTitledBorder("Fluent Setter", false));
        fluentMethodPanel.setBorder(IdeBorderFactory.createTitledBorder("Fluent Method", false));
        fluentCallPanel.setBorder(IdeBorderFactory.createTitledBorder(FluentToolConstant.FLUENT_CALL, false));
    }

}
