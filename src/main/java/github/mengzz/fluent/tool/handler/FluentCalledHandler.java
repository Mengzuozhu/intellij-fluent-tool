package github.mengzz.fluent.tool.handler;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import github.mengzz.fluent.tool.setting.FluentToolSetting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mengzz
 **/
public class FluentCalledHandler {
    private static final String BUILD_METHOD_NAME = "build";
    private static final String DEFAULT_PARAM = "()";
    private static Map<String, String> typeAndDefaultValue = new HashMap<String, String>() {
        {
            put("byte", "(byte)0");
            put("short", "(short)0");
            put("int", "0");
            put("long", "0L");
            put("float", "0.0f");
            put("double", "0.0d");
            put("char", "' '");
            put("boolean", "false");
        }
    };

    public StringBuilder buildCalledMethod(PsiClass psiClass, List<PsiMethod> members, boolean withDefaultValue) {
        StringBuilder builder = new StringBuilder();
        for (PsiMethod psiMethod : members) {
            String param = DEFAULT_PARAM;
            if (withDefaultValue && isContainBuildMethod(psiClass)) {
                param = buildParameter(psiMethod);
            }
            builder.append("\n.")
                    .append(psiMethod.getName())
                    .append(param);
        }
        addBuildMethodIfNeed(psiClass, builder);
        return builder;
    }

    private String buildParameter(PsiMethod psiMethod) {
        String param = DEFAULT_PARAM;
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (parameters.length == 1) {
            String type = parameters[0].getType().getCanonicalText();
            String defaultValue = String.valueOf(typeAndDefaultValue.get(type));
            param = String.format("(%s)", defaultValue);
        }
        return param;
    }

    private void addBuildMethodIfNeed(PsiClass psiClass, StringBuilder builder) {
        if (FluentToolSetting.getInstance().isAddBuildMethodIfExist() && isContainBuildMethod(psiClass)) {
            builder.append("\n.build()");
        }
    }

    private boolean isContainBuildMethod(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .anyMatch(method -> BUILD_METHOD_NAME.equals(method.getName()));
    }

}
