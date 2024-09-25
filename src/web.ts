import { WebPlugin } from '@capacitor/core';

import type { PolarSdkPlugin } from './definitions';

export class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented on web.');
  }
}
