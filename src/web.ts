import { WebPlugin } from '@capacitor/core';

import type { PolarSdkPlugin } from './definitions';

export class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean, message?: string }> {
    throw new Error('Method not implemented on web.');
  }

  streamHR(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  streamEcg(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  streamAcc(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  stopEcg(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  stopHR(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  stopAcc(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }

  disconnectPolar(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }
}
