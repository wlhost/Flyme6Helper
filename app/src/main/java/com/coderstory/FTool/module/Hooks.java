package com.coderstory.FTool.module;

import android.content.Context;
import android.net.Uri;

import com.coderstory.FTool.plugins.IModule;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hooks implements IModule {

    private static XC_LoadPackage.LoadPackageParam loadPackageParam;

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        loadPackageParam = lpparam;
        try {
            patchcode();
        } catch (ClassNotFoundException e) {
            XposedBridge.log(e);
        }
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
    }

    private void patchcode() throws ClassNotFoundException {

        XSharedPreferences prefs = new XSharedPreferences("com.coderstory.FTool", "UserSettings");
        prefs.makeWorldReadable();
        prefs.reload();

        if (loadPackageParam.packageName.equals("com.meizu.customizecenter") && prefs.getBoolean("enableThemePatch", false)) {

            XposedBridge.log("crack by coderstory");

            //device_states | doCheckState
            //6.6.1
            findAndHookMethod("com.meizu.customizecenter.utils.ak", loadPackageParam.classLoader, "h", Context.class, XC_MethodReplacement.returnConstant(0));
            //6.7.0
            findAndHookMethod("com.meizu.customizecenter.h.al", loadPackageParam.classLoader, "h", Context.class, XC_MethodReplacement.returnConstant(0));
            findAndHookMethod("com.meizu.statsapp.util.Utils", loadPackageParam.classLoader, "isRoot", Context.class, XC_MethodReplacement.returnConstant(false));

            //resetToSystemTheme
            // 6.0.7 6.1.0 6.2.0 6.3.2
            findAndHookMethod("com.meizu.customizecenter.common.theme.common.theme.a", loadPackageParam.classLoader, "e", XC_MethodReplacement.returnConstant(false));
            //6.9.0
            findAndHookMethod("com.meizu.customizecenter.common.theme.common.theme.b", loadPackageParam.classLoader, "a", XC_MethodReplacement.returnConstant(false));

            //data/data/com.meizu.customizecenter/font/   system_font
            //6.0.7 6.1.0 6.2.0
            findAndHookMethod("com.meizu.customizecenter.common.font.FontManager", loadPackageParam.classLoader, "a", XC_MethodReplacement.returnConstant(true));
            //6.7.0
            findAndHookMethod("com.meizu.customizecenter.common.font.c", loadPackageParam.classLoader, "a", XC_MethodReplacement.returnConstant(true));
             //6.8.0
            findAndHookMethod("com.meizu.customizecenter.common.font.c", loadPackageParam.classLoader, "e", XC_MethodReplacement.returnConstant(""));
            findAndHookMethod("com.meizu.customizecenter.common.font.f", loadPackageParam.classLoader, "b", XC_MethodReplacement.returnConstant(false));

            //主题混搭 ThemeContentProvider query Unknown URI
            findAndHookMethod("com.meizu.customizecenter.common.dao.ThemeContentProvider", loadPackageParam.classLoader, "query", Uri.class, String[].class, String.class, String[].class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Object[] objs = param.args;
                    String Tag = "(ITEMS LIKE";
                    String Tag2 = "%zklockscreen;%";
                    String Tag3 = "%com.meizu.flyme.weather;%";
                    //XposedBridge.log("开始");
                    boolean result = false;
                    for (Object obj : objs) {
                       // XposedBridge.log(obj==null?"":obj.toString());
                        if (obj instanceof String && (((String) obj).contains(Tag) || obj.equals(Tag2) || obj.equals(Tag3)  )) {
                            result = true;
                        }
                    }
                   // XposedBridge.log("结束");
                    if (result) {
                        for (Object obj : objs) {
                            if (obj instanceof String[]) {
                                for (int j = 0; j < ((String[]) obj).length; j++) {
                                    if (((String[]) obj)[j].contains("/storage/emulated/0/Customize/Themes")) {
                                        ((String[]) obj)[j] = "/storage/emulated/0/Customize%";
                                    } else if (((String[]) obj)[j].contains("/storage/emulated/0/Customize/TrialThemes")) {
                                        ((String[]) obj)[j] = "NONE";
                                    }
                                }
                            }
                        }
                    }
                    super.beforeHookedMethod(param);
                }
            });
        }

        if (loadPackageParam.packageName.equals("com.android.packageinstaller") && prefs.getBoolean("enableCheckInstaller", false)){
            findAndHookMethod("com.meizu.common.app.PermissionDialogBuilder", loadPackageParam.classLoader, "isProductInternational",XC_MethodReplacement.returnConstant(true));
            findAndHookMethod("com.android.packageinstaller.InstallFlowAnalytics", loadPackageParam.classLoader, "isSystemApp",XC_MethodReplacement.returnConstant(true));
        }
    }

    private static void findAndHookMethod(String p1, ClassLoader lpparam, String p2, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(p1, lpparam, p2, parameterTypesAndCallback);
        } catch (Throwable localString3) {
            XposedBridge.log(localString3);
        }
    }

}


