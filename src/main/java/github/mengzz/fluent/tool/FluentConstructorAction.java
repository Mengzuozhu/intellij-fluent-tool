package github.mengzz.fluent.tool;

import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import github.mengzz.fluent.tool.dialog.ConstructorMemberDialog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
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
    private static final String ANNOTATION_PREFIX = "@";

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
            if (nameIdentifier == null) {
                continue;
            }
            String text = nameIdentifier.getText();
            constructedParamNames.add(text);
            PsiType psiType = parameter.getType();
            String typeAndName = MessageFormat.format("{0} {1}", psiType.getPresentableText(), text);
            typeAndName = fillAnnotationsIfNeed(parameter, typeAndName);
            inParamNames.add(typeAndName);
        }
        String constructedParam = JOINER.join(constructedParamNames);
        String inParam = JOINER.join(inParamNames);
        String generic = PluginUtil.getGenericText(psiClass);
        if (StringUtils.isNotEmpty(generic)) {
            return buildGenericConstruct(methodName, className, constructedParam, inParam, generic);
        }
        return MessageFormat.format("public static {0} {1}({2}) '{'return new {0}({3});'}'",
                className, methodName, inParam, constructedParam);
    }

    private String fillAnnotationsIfNeed(PsiParameter parameter, String typeAndName) {
        PsiAnnotation[] annotations = parameter.getAnnotations();
        if (annotations.length > 0) {
            String annotationStr = Arrays.stream(annotations)
                    .map(PsiAnnotation::getQualifiedName)
                    .map(name -> ANNOTATION_PREFIX + name)
                    .collect(Collectors.joining(" "));
            return MessageFormat.format("{0} {1}", annotationStr, typeAndName);
        }
        return typeAndName;
    }

    private String buildGenericConstruct(String methodName, String className, String constructedParam, String inParam,
                                         String generic) {
        return MessageFormat.format("public static {0} {1}{0} {2}({3}) '{'return new {1}<>({4});'}'",
                generic, className, methodName, inParam, constructedParam);
    }

}
