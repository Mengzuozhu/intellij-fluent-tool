package github.mengzz.fluent.tool;

import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import github.mengzz.fluent.tool.dialog.ConstructorMemberDialog;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Fluent constructor action.
 *
 * @author mengzz
 */
public class FluentConstructorAction extends AnAction {
    private static final String COMMA = ", ";
    private static final Joiner JOINER = Joiner.on(COMMA);

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiClass psiClass = PluginUtil.getPsiClass(e);
        e.getPresentation().setEnabled(psiClass != null && !PluginUtil.getConstructors(psiClass).isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass psiClass = PluginUtil.getPsiClass(e);
        if (psiClass == null) {
            return;
        }
        ConstructorMemberDialog<PsiMethod> dialog = new ConstructorMemberDialog<>(psiClass.getProject(),
                PluginUtil.getConstructors(psiClass));
        if (!dialog.showAndGet()) {
            return;
        }
        Project project = psiClass.getProject();
        List<PsiMethod> members = dialog.getRemainedMembers();
        String methodName = dialog.getText();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            for (PsiMethod method : members) {
                String setterMethod = buildStaticConstruct(method, psiClass, methodName);
                psiClass.add(elementFactory.createMethodFromText(setterMethod, psiClass));
            }
        });
    }

    private String buildStaticConstruct(PsiMethod method, PsiClass psiClass, String methodName) {
        String className = psiClass.getName();
        PsiParameter[] parameters = method.getParameterList().getParameters();
        List<String> constructedParamNames = new ArrayList<>();
        List<String> inParamNames = new ArrayList<>();
        for (PsiParameter parameter : parameters) {
            PsiIdentifier nameIdentifier = parameter.getNameIdentifier();
            PsiClassReferenceType referenceType = PluginUtil.convertAs(parameter.getType(),
                    PsiClassReferenceType.class);
            if (nameIdentifier == null || referenceType == null) {
                continue;
            }
            String text = nameIdentifier.getText();
            constructedParamNames.add(text);
            String classAndName = String.format("%s %s", referenceType.getClassName(), text);
            inParamNames.add(classAndName);
        }
        String constructedParam = JOINER.join(constructedParamNames);
        String inParam = JOINER.join(inParamNames);
        String generic = getGeneric(psiClass);
        if (StringUtils.isNotEmpty(generic)) {
            return buildGenericConstruct(methodName, className, constructedParam, inParam, generic);
        }
        return String.format("public static %s %s(%s) {" +
                        "return new %s(%s);" +
                        "}", className, methodName, inParam,
                className, constructedParam);
    }

    private String buildGenericConstruct(String methodName, String className, String constructedParam, String inParam,
                                         String generic) {
        return String.format("public static %s %s%s %s(%s) {" +
                        "return new %s<>(%s);" +
                        "}", generic, className, generic, methodName, inParam,
                className, constructedParam);
    }

    private String getGeneric(PsiClass psiClass) {
        PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
        if (ArrayUtils.isNotEmpty(typeParameters)) {
            return Arrays.stream(typeParameters)
                    .map(PsiTypeParameter::getName)
                    .collect(Collectors.joining(COMMA, "<", ">"));
        }
        return null;
    }

}
