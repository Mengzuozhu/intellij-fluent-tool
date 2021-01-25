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
    private static final String LABEL = "Ignore Deprecated";

    public CalledMemberDialog(@NotNull Project project, List<PsiMethod> members) {
        super(project, members, TITLE);
    }

    @Override
    protected void customInit() {
        addComponent(getDeprecatedCheck());
    }

    @NotNull
    private Component getDeprecatedCheck() {
        JBCheckBox deprecatedCheck = new JBCheckBox(LABEL);
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
