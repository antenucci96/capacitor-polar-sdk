export interface PolarSdkPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
