import { registerPlugin } from '@capacitor/core';
const PolarSdk = registerPlugin('PolarSdk', {
    web: () => import('./web').then(m => new m.PolarSdkWeb()),
});
export * from './definitions';
export { PolarSdk };
//# sourceMappingURL=index.js.map