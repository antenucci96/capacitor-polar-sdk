# capacitor-polar-sdk

Ionic capacitor to connect Polar H10 band to ionic app

## Install

```bash
npm install capacitor-polar-sdk
npx cap sync
```

## API

<docgen-index>

* [`connectPolar()`](#connectpolar)
* [`addListener('hrData', ...)`](#addlistenerhrdata-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### connectPolar()

```typescript
connectPolar() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### addListener('hrData', ...)

```typescript
addListener(eventName: 'hrData', data: any) => Promise<PluginListenerHandle>
```

| Param           | Type                  |
| --------------- | --------------------- |
| **`eventName`** | <code>'hrData'</code> |
| **`data`**      | <code>any</code>      |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
