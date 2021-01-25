package github.mengzz.fluent.tool;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import github.mengzz.fluent.tool.dialog.FluentMethodDialog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/**
 * The type Fluent method action.
 *
 * @author mengzz
 */
public class FluentMethodAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(PluginUtil.getPsiClass(e) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass psiClass = PluginUtil.getPsiClass(e);
        if (psiClass == null) {
            return;
        }
        FluentMethodDialog dialog = new FluentMethodDialog(psiClass.getProject());
        if (!dialog.showAndGet()) {
            return;
        }
        String methodName = dialog.getName();
        if (StringUtils.isBlank(methodName)) {
            return;
        }
        Project project = psiClass.getProject();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            String methodBody = buildMethodBody(psiClass, methodName);
            psiClass.add(elementFactory.createMethodFromText(methodBody, psiClass));
        });
    }

    private String buildMethodBody(PsiClass psiClass, String methodName) {
        String className = psiClass.getName();
        String generic = PluginUtil.getGenericText(psiClass);
        if (StringUtils.isNotEmpty(generic)) {
            className += generic;
        }
        return MessageFormat.format("public {0} {1}() '{'return this;'}'",
                className, methodName);
    }

}
