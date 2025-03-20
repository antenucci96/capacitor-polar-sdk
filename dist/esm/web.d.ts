import { WebPlugin } from '@capacitor/core';
import type { PolarSdkPlugin } from './definitions';
export declare class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
    connectPolar(): Promise<{
        value: boolean;
        message?: string;
    }>;
    streamHR(): Promise<{
        value: boolean;
    }>;
    streamEcg(): Promise<{
        value: boolean;
    }>;
    streamAcc(): Promise<{
        value: boolean;
    }>;
    stopEcg(): Promise<{
        value: boolean;
    }>;
    stopHR(): Promise<{
        value: boolean;
    }>;
    stopAcc(): Promise<{
        value: boolean;
    }>;
    disconnectPolar(): Promise<{
        value: boolean;
    }>;
}
