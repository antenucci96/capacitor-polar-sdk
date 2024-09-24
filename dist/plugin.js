var capacitorPolarSdk = (function (exports, core) {
    'use strict';

    const PolarSdk = core.registerPlugin('PolarSdk', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.PolarSdkWeb()),
    });

    class PolarSdkWeb extends core.WebPlugin {
        connectPolar() {
            // Logica per simulare la connessione
            console.log('Connecting to Polar device');
            return Promise.resolve({ value: true });
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        PolarSdkWeb: PolarSdkWeb
    });

    exports.PolarSdk = PolarSdk;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
