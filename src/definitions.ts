export interface PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean }>;
}
