var capacitorPolarSdk = (function (exports, core) {
    'use strict';

    const PolarSdk = core.registerPlugin('PolarSdk', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.PolarSdkWeb()),
    });

    class PolarSdkWeb extends core.WebPlugin {
        connectPolar() {
            throw new Error('Method not implemented on web.');
        }
        streamHR() {
            throw new Error('Method not implemented on web.');
        }
        streamEcg() {
            throw new Error('Method not implemented on web.');
        }
        streamAcc() {
            throw new Error('Method not implemented on web.');
        }
        stopEcg() {
            throw new Error('Method not implemented on web.');
        }
        stopHR() {
            throw new Error('Method not implemented on web.');
        }
        stopAcc() {
            throw new Error('Method not implemented on web.');
        }
        disconnectPolar() {
            throw new Error('Method not implemented on web.');
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
