import { WebPlugin } from '@capacitor/core';
export class PolarSdkWeb extends WebPlugin {
    connectPolar() {
        // Logica per simulare la connessione
        console.log('Connecting to Polar device');
        return Promise.resolve({ value: true });
    }
}
//# sourceMappingURL=web.js.map