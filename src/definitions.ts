import type { PluginListenerHandle } from '@capacitor/core';

export interface PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean }>;

  addListener(
    eventName: 'hrData',
    data: any,
  ): Promise<PluginListenerHandle>;

  removeAllListeners(): Promise<void>;
}
