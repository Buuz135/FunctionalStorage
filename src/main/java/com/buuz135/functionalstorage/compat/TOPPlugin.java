//package com.buuz135.functionalstorage.compat;
//
//import com.buuz135.functionalstorage.compat.top.FunctionalDrawerProvider;
//import com.hrznstudio.titanium.annotation.plugin.FeaturePlugin;
//import com.hrznstudio.titanium.event.handler.EventManager;
//import com.hrznstudio.titanium.plugin.FeaturePluginInstance;
//import com.hrznstudio.titanium.plugin.PluginPhase;
//import net.neoforged.fml.InterModComms;
//import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
//
//@FeaturePlugin(value = "theoneprobe", type = FeaturePlugin.FeaturePluginType.MOD)
//public class TOPPlugin implements FeaturePluginInstance {
//    @Override
//    public void execute(PluginPhase phase) {
//        if (phase == PluginPhase.CONSTRUCTION) {
//            EventManager.mod(InterModEnqueueEvent.class).process(interModEnqueueEvent -> {
//                InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> FunctionalDrawerProvider.REGISTER);
//            }).subscribe();
//        }
//    }
//
//}
