package github.mengzz.fluent.tool.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Called member dialog.
 *
 * @author mengzz
 */
public class CalledMemberDialog extends ListMemberDialog<PsiMethod> {
    private static final String DEPRECATED_ANNOTATION = Deprecated.class.getCanonicalName();
    private static final String TITLE = "Select Called Methods";
    private static final String IGNORE_DEPRECATED_LABEL = "Ignore deprecated";
    private static final String DEFAULT_VALUE_LABEL = "With default value for builder";
    private JBCheckBox defaultValueCheckbox;

    public CalledMemberDialog(@NotNull Project project, List<PsiMethod> members) {
        super(project, members, TITLE);
    }

    public boolean isWithDefaultValue() {
        return defaultValueCheckbox.isSelected();
    }

    @Override
    protected void customInit() {
        addComponent(getDeprecatedCheck());
        defaultValueCheckbox = new JBCheckBox(DEFAULT_VALUE_LABEL);
        addComponent(defaultValueCheckbox);
    }

    @NotNull
    private Component getDeprecatedCheck() {
        JBCheckBox deprecatedCheck = new JBCheckBox(IGNORE_DEPRECATED_LABEL);
        deprecatedCheck.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                List<PsiMethod> methods = members.stream()
                        .filter(this::isNotDeprecatedMethod)
                        .collect(Collectors.toList());
                updateModelItems(methods);
            } else {
                updateModelItems(members);
            }
        });
        deprecatedCheck.setSelected(true);
        return deprecatedCheck;
    }

    private boolean isNotDeprecatedMethod(PsiMethod psiMethod) {
        return !psiMethod.hasAnnotation(DEPRECATED_ANNOTATION);
    }

}
