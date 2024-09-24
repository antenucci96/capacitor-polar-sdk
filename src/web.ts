import { WebPlugin } from '@capacitor/core';

import type { PolarSdkPlugin } from './definitions';

export class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
