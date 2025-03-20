import type { PluginListenerHandle } from '@capacitor/core';

export interface PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean, message?: string }>;

  streamHR(): Promise<{ value: boolean }>;

  streamEcg(): Promise<{ value: boolean }>;

  streamAcc(): Promise<{ value: boolean }>;

  stopHR(): Promise<{ value: boolean }>;

  stopEcg(): Promise<{ value: boolean }>;

  stopAcc(): Promise<{ value: boolean }>;

  disconnectPolar(): Promise<{ value: boolean }>;

  addListener(
    eventName: 'hrData',
    data: any,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'ecgData',
    data: any,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'accData',
    data: any,
  ): Promise<PluginListenerHandle>;

  addListener(
    eventName: 'disconnected',
    data: any,
  ): Promise<PluginListenerHandle>;

  removeAllListeners(): Promise<void>;
}
