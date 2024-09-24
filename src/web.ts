import { WebPlugin } from '@capacitor/core';

import type { PolarSdkPlugin } from './definitions';

export class PolarSdkWeb extends WebPlugin implements PolarSdkPlugin {
  connectPolar(): Promise<{ value: boolean }> {
    // Logica per simulare la connessione
    console.log('Connecting to Polar device');
    return Promise.resolve({ value: true });
  }
}
