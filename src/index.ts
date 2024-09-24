import { registerPlugin } from '@capacitor/core';

import type { PolarSdkPlugin } from './definitions';

const PolarSdk = registerPlugin<PolarSdkPlugin>('PolarSdk', {
  web: () => import('./web').then(m => new m.PolarSdkWeb()),
});

export * from './definitions';
export { PolarSdk };
