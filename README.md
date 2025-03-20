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
* [`streamHR()`](#streamhr)
* [`streamEcg()`](#streamecg)
* [`streamAcc()`](#streamacc)
* [`stopHR()`](#stophr)
* [`stopEcg()`](#stopecg)
* [`stopAcc()`](#stopacc)
* [`disconnectPolar()`](#disconnectpolar)
* [`addListener('hrData', ...)`](#addlistenerhrdata-)
* [`addListener('ecgData', ...)`](#addlistenerecgdata-)
* [`addListener('accData', ...)`](#addlisteneraccdata-)
* [`addListener('disconnected', ...)`](#addlistenerdisconnected-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### connectPolar()

```typescript
connectPolar() => Promise<{ value: boolean; message?: string; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; message?: string; }&gt;</code>

--------------------


### streamHR()

```typescript
streamHR() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### streamEcg()

```typescript
streamEcg() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### streamAcc()

```typescript
streamAcc() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### stopHR()

```typescript
stopHR() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### stopEcg()

```typescript
stopEcg() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### stopAcc()

```typescript
stopAcc() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### disconnectPolar()

```typescript
disconnectPolar() => Promise<{ value: boolean; }>
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


### addListener('ecgData', ...)

```typescript
addListener(eventName: 'ecgData', data: any) => Promise<PluginListenerHandle>
```

| Param           | Type                   |
| --------------- | ---------------------- |
| **`eventName`** | <code>'ecgData'</code> |
| **`data`**      | <code>any</code>       |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('accData', ...)

```typescript
addListener(eventName: 'accData', data: any) => Promise<PluginListenerHandle>
```

| Param           | Type                   |
| --------------- | ---------------------- |
| **`eventName`** | <code>'accData'</code> |
| **`data`**      | <code>any</code>       |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('disconnected', ...)

```typescript
addListener(eventName: 'disconnected', data: any) => Promise<PluginListenerHandle>
```

| Param           | Type                        |
| --------------- | --------------------------- |
| **`eventName`** | <code>'disconnected'</code> |
| **`data`**      | <code>any</code>            |

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
