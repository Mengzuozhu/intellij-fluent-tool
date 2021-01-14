package github.mengzz.fluent.tool;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Plugin util.
 *
 * @author mengzz
 */
public class PluginUtil {
    /**
     * Convert as.
     *
     * @param <T> the type parameter
     * @param obj the obj
     * @param cls the cls
     * @return the t
     */
    public static <T> T convertAs(Object obj, Class<T> cls) {
        if (cls.isInstance(obj)) {
            return cls.cast(obj);
        }
        return null;
    }

    /**
     * Find match setter fields.
     *
     * @param psiClass the psi class
     * @return the list
     */
    static List<PsiField> findSetterFields(PsiClass psiClass) {
        return Arrays.stream(psiClass.getFields())
                .filter(PluginUtil::canSetter)
                .collect(Collectors.toList());
    }

    /**
     * Gets constructors.
     *
     * @param psiClass the psi class
     * @return the constructors
     */
    static List<PsiMethod> getConstructors(PsiClass psiClass) {
        return Arrays.stream(psiClass.getConstructors())
                .collect(Collectors.toList());
    }

    /**
     * Gets method names.
     *
     * @param psiClass the psi class
     * @return the method names
     */
    static Set<String> getMethodNames(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .map(PsiMethod::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets psi type.
     *
     * @param element the element
     * @return the psi type
     */
    @Nullable
    static PsiType getPsiType(PsiElement element) {
        PsiExpression expression = convertAs(element, PsiExpression.class);
        if (expression != null) {
            return expression.getType();
        }
        PsiLocalVariable localVariable = convertAs(element, PsiLocalVariable.class);
        if (localVariable != null) {
            return localVariable.getType();
        }
        return null;
    }

    /**
     * Is convertible from.
     *
     * @param psiType    the psi type
     * @param returnType the return type
     * @return the boolean
     */
    static boolean isConvertibleFrom(PsiType psiType, PsiType returnType) {
        return returnType != null && psiType != null && psiType.isConvertibleFrom(returnType);
    }

    /**
     * Gets psi class.
     *
     * @param e the e
     * @return the psi class
     */
    static PsiClass getPsiClass(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null || psiFile == null) {
            return null;
        }
        PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        return PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
    }

    /**
     * Gets generic text.
     *
     * @param psiClass the psi class
     * @return the generic
     */
    static String getGenericText(PsiClass psiClass) {
        PsiTypeParameterList typeParameterList = psiClass.getTypeParameterList();
        return typeParameterList != null ? typeParameterList.getText() : null;
    }

    /**
     * Can setter.
     *
     * @param psiField the psi field
     * @return the boolean
     */
    private static boolean canSetter(@NotNull PsiField psiField) {
        return !psiField.hasModifier(JvmModifier.FINAL) &&
                !psiField.hasModifier(JvmModifier.STATIC);
    }
}
