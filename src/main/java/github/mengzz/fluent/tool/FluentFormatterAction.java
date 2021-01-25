package github.mengzz.fluent.tool;

import com.google.common.base.Splitter;
import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The type Fluent formatter action.
 *
 * @author mengzz
 */
public class FluentFormatterAction extends BaseElementAtCaretIntentionAction {
    private static final int DOT_LIMIT = 2;
    private static final String DOT = ".";
    private static final String LINE = "\n";
    private static final Splitter LINE_SPLITTER = Splitter.on(LINE);

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (editor == null) {
            return false;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        // when any line contains more than one dot
        return selectionModel.hasSelection() && splitByLine(selectionModel.getSelectedText())
                .anyMatch(text -> countMatches(text, DOT, DOT_LIMIT) >= DOT_LIMIT);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        formatFluentStyle(editor, element);
    }

    @NotNull
    @Override
    public String getText() {
        return FluentToolConstant.FLUENT_FORMAT;
    }

    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    String getFamilyName() {
        return getText();
    }

    private void formatFluentStyle(Editor editor, @NotNull PsiElement element) {
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        if (primaryCaret.getSelectedText() == null) {
            return;
        }
        reformat(editor, element.getContainingFile());
        primaryCaret.removeSelection();
    }

    private void reformat(Editor editor, PsiFile file) {
        CommonCodeStyleSettings commonSettings = CodeStyle.getSettings(file).getCommonSettings("Java");
        int preWap = commonSettings.METHOD_CALL_CHAIN_WRAP;
        commonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
        // 仅修改配置，使用IDEA内部格式化工具
        new ReformatCodeProcessor(file, editor.getSelectionModel()).runWithoutProgress();
        // 复位原配置
        commonSettings.METHOD_CALL_CHAIN_WRAP = preWap;
    }

    private Stream<String> splitByLine(String selectedText) {
        return StreamSupport.stream(LINE_SPLITTER.split(selectedText).spliterator(), false);
    }

    private int countMatches(String str, String sub, int limit) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            if (count >= limit) {
                return count;
            }
            index += sub.length();
        }
        return count;
    }
}
