// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0
// license that can be found in the LICENSE file.

package github.mengzz.fluent.tool.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author mengzz
 */
@State(name = "github.mengzz.fluent.tool.setting.FluentToolSettings",
        storages = {@Storage("intellij-fluent-tool.xml")}
)
public class FluentToolSetting implements PersistentStateComponent<FluentToolSetting> {

    private String constructMethodName = "of";
    private String fluentSetterPrefix;
    private boolean addBuildMethodIfExist = true;
    private String fluentMethodName = "with";

    public static FluentToolSetting getInstance() {
        return ServiceManager.getService(FluentToolSetting.class);
    }

    public String getFluentMethodName() {
        return fluentMethodName;
    }

    public void setFluentMethodName(String fluentMethodName) {
        this.fluentMethodName = fluentMethodName;
    }

    public boolean isAddBuildMethodIfExist() {
        return addBuildMethodIfExist;
    }

    public void setAddBuildMethodIfExist(boolean addBuildMethodIfExist) {
        this.addBuildMethodIfExist = addBuildMethodIfExist;
    }

    public String getFluentSetterPrefix() {
        return fluentSetterPrefix;
    }

    public void setFluentSetterPrefix(String fluentSetterPrefix) {
        this.fluentSetterPrefix = fluentSetterPrefix;
    }

    public String getConstructMethodName() {
        return constructMethodName;
    }

    public void setConstructMethodName(String constructMethodName) {
        this.constructMethodName = constructMethodName;
    }

    @Nullable
    @Override
    public FluentToolSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FluentToolSetting state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
