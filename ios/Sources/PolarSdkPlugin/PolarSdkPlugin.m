#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(PolarSdk, "PolarSdk",
           CAP_PLUGIN_METHOD(connectPolar, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(disconnectPolar, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(streamHR, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(streamEcg, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopEcg, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopHR, CAPPluginReturnPromise);
)
