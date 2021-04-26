/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package github.mengzz.fluent.tool;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import github.mengzz.fluent.tool.dialog.CalledMemberDialog;
import github.mengzz.fluent.tool.setting.FluentToolSetting;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Fluent call action.
 *
 * @author mengzz
 */
public class FluentCalledAction extends BaseElementAtCaretIntentionAction {
    private static final String BUILD_METHOD_NAME = "build";
    private static final String SEMICOLON = ";";

    @NotNull
    @Override
    public String getText() {
        return FluentToolConstant.FLUENT_CALL;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return isPrevSiblingContainBuilder(element) || isCursorInBuilderName(element);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement builderElement = getBuilderElement(element);
        if (builderElement == null) {
            return;
        }
        PsiType psiType = PluginUtil.getPsiType(builderElement);
        if (psiType == null) {
            return;
        }
        Optional.ofNullable(PsiTypesUtil.getPsiClass(psiType))
                .ifPresent(psiClass -> {
                    // use invokeLater() to avoid "AWT Events are not allowed"
                    ApplicationManager.getApplication().invokeLater(() -> {
                        PsiFile containingFile = builderElement.getContainingFile();
                        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
                        Document document = manager.getDocument(containingFile);
                        if (document == null) {
                            return;
                        }
                        StringBuilder builderText = getBuilderText(psiType, psiClass);
                        if (builderText == null) {
                            return;
                        }
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            int endOffset = getEndOffset(builderElement);
                            TextRange changeRange = TextRange.create(endOffset, endOffset + builderText.length());
                            document.insertString(endOffset, builderText);
                            manager.commitDocument(document);
                            new ReformatCodeProcessor(project, containingFile, changeRange, false)
                                    .runWithoutProgress();
                        });
                    });
                });
    }

    private int getEndOffset(PsiElement builderElement) {
        PsiElement methodCallParent = getMethodCallParent(builderElement);
        if (methodCallParent != null) {
            builderElement = methodCallParent;
        }
        TextRange textRange = builderElement.getTextRange();
        int endOffset = textRange.getEndOffset();
        PsiElement lastChild = builderElement.getLastChild();
        if (containSemicolon(lastChild)) {
            endOffset--;
        }
        return endOffset;
    }

    private PsiElement getMethodCallParent(PsiElement element) {
        while (element != null && !(element instanceof PsiMethodCallExpression)) {
            element = element.getParent();
        }
        return element;
    }

    private boolean containSemicolon(PsiElement lastChild) {
        return lastChild != null && SEMICOLON.equals(lastChild.getText());
    }

    private StringBuilder getBuilderText(PsiType psiType, PsiClass psiClass) {
        List<PsiMethod> fluentMethods = getFluentMethods(psiType, psiClass);
        CalledMemberDialog listMemberDialog = new CalledMemberDialog(psiClass.getProject(), fluentMethods);
        if (!listMemberDialog.showAndGet()) {
            return null;
        }
        List<PsiMethod> members = listMemberDialog.getRemainedMembers();
        StringBuilder builder = new StringBuilder();
        for (PsiMethod psiMethod : members) {
            builder.append("\n.").append(psiMethod.getName()).append("()");
        }
        addBuildMethodIfNeed(psiClass, builder);
        return builder;
    }

    private List<PsiMethod> getFluentMethods(PsiType psiType, PsiClass psiClass) {
        if (psiType == null || psiClass == null) {
            return new ArrayList<>();
        }
        // ignore static method
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> !PluginUtil.isStaticMethod(psiMethod)
                        && PluginUtil.isConvertibleFrom(psiType, psiMethod.getReturnType()))
                .collect(Collectors.toList());
    }

    private PsiElement getBuilderElement(@NotNull PsiElement element) {
        PsiElement builderElement;
        if (isPrevSiblingContainBuilder(element)) {
            builderElement = getPrevFirstChild(element).orElse(null);
        } else {
            builderElement = getParentOfParent(element).orElse(null);
        }
        return builderElement;
    }

    private void addBuildMethodIfNeed(PsiClass psiClass, StringBuilder builder) {
        if (FluentToolSetting.getInstance().isAddBuildMethodIfExist()) {
            boolean containBuild = Arrays.stream(psiClass.getMethods())
                    .anyMatch(method -> BUILD_METHOD_NAME.equals(method.getName()));
            if (containBuild) {
                builder.append("\n.build()");
            }
        }
    }

    private Boolean isCursorInBuilderName(@NotNull PsiElement element) {
        return containFluentMethod(getParentOfParent(element));
    }

    private boolean isPrevSiblingContainBuilder(@NotNull PsiElement element) {
        Optional<PsiElement> prevFirstChild = getPrevFirstChild(element);
        return containFluentMethod(prevFirstChild);
    }

    private Optional<PsiElement> getParentOfParent(@NotNull PsiElement element) {
        return Optional.ofNullable(element.getParent())
                .map(PsiElement::getParent);
    }

    private Optional<PsiElement> getPrevFirstChild(@NotNull PsiElement element) {
        Optional<PsiElement> prevFirstChild = Optional.ofNullable(element.getPrevSibling())
                .map(PsiElement::getFirstChild);
        PsiElement newExpression = getNewExpression(prevFirstChild);
        if (newExpression != null) {
            return Optional.of(newExpression);
        }
        return prevFirstChild;
    }

    private PsiElement getNewExpression(Optional<PsiElement> prevFirstChild) {
        return prevFirstChild.map(data -> data instanceof PsiKeyword ? ((PsiKeyword) data) : null)
                .map(PsiElement::getParent)
                .orElse(null);
    }

    private boolean containFluentMethod(Optional<PsiElement> elementOptional) {
        if (!elementOptional.isPresent()) {
            return false;
        }
        PsiElement element = elementOptional.get();
        PsiType psiType = PluginUtil.getPsiType(element);
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiType);
        return CollectionUtils.isNotEmpty(getFluentMethods(psiType, psiClass));
    }

}
