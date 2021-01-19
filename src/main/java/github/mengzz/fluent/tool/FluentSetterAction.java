package github.mengzz.fluent.tool;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import github.mengzz.fluent.tool.dialog.SetterMemberDialog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * @author mengzz
 **/
public class FluentSetterAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiClass psiClass = PluginUtil.getPsiClass(e);
        e.getPresentation().setEnabled(psiClass != null && !PluginUtil.findSetterFields(psiClass).isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass psiClass = PluginUtil.getPsiClass(e);
        if (psiClass == null) {
            return;
        }
        SetterMemberDialog<PsiField> dialog = new SetterMemberDialog<>(psiClass.getProject(),
                PluginUtil.findSetterFields(psiClass));
        if (!dialog.showAndGet()) {
            return;
        }
        Project project = psiClass.getProject();
        List<PsiField> fields = dialog.getRemainedMembers();
        String prefix = dialog.getPrefix();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            Set<String> existedMethods = PluginUtil.getMethodNames(psiClass);
            for (PsiField field : fields) {
                String methodName = buildMethodName(field, prefix);
                if (existedMethods.contains(methodName)) {
                    continue;
                }
                String setterMethod = buildSetterMethod(field, psiClass, methodName);
                psiClass.add(elementFactory.createMethodFromText(setterMethod, psiClass));
            }
        });
    }

    private String buildSetterMethod(PsiField field, PsiClass psiClass, String methodName) {
        String className = psiClass.getName();
        String generic = PluginUtil.getGenericText(psiClass);
        if (StringUtils.isNotEmpty(generic)) {
            className += generic;
        }
        String fieldName = field.getName();
        String presentableText = field.getType().getPresentableText();
        return MessageFormat.format("public {0} {1}({2} {3}) '{'this.{3} = {3};return this;'}'",
                className, methodName, presentableText, fieldName);
    }

    private String buildMethodName(PsiField field, String prefix) {
        String name = field.getName();
        if (StringUtils.isBlank(prefix) || name == null) {
            return name;
        }
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
