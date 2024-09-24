import { WebPlugin } from '@capacitor/core';
import type { PolarSdkPlugin } from './definitions';
export declare class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
    connectPolar(): Promise<{
        value: boolean;
    }>;
}
