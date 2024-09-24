import { PolarSdk } from 'capacitor-polar-sdk';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    PolarSdk.echo({ value: inputValue })
}
