---
title: Serialization
---

The CDM is expressed in the Rune DSL, and CDM objects are serialized to JSON following the Rune serialization standard.

**From CDM version 7 onwards, the default JSON serialization format has been updated.** The new format includes all relevant object information and improves efficiency, readability, maintainability and interoperability.

## What has changed

The top level of every serialized document now identifies the model, model version and fully qualified type of the object, using the special attributes `@model`, `@version` and `@type`:

``` json
{
  "@model": "cdm",
  "@version": "7.0.0",
  "@type": "cdm.event.common.TradeState"
}
```

Metadata — keys, references, schemes and their associated data — are serialized as compact `@`-prefixed special attributes, replacing the nested `meta`/`value` wrappers used prior to CDM 7:

| Prior to CDM v7 | From CDM v7 onwards |
| --- | --- |
| `globalKey` / `globalReference` | `@key` /  `@ref` |
| `location, scope` / `address, scope`  | `@key:scoped`  / `@ref:scoped` |
| `externalKey` / `externalReference`  | `@key:external` / `@ref:external`  |
| `value` (alongside `meta`) | `@data` |
| `meta.scheme` | `@scheme` |

For example, `Trade`'s `tradeDate` attribute, which is annotated with `[metadata id]`:

**Prior to CDM v7**

``` json
   "trade": {
     "tradeDate": {
        "value": "2017-12-18",
        "meta": {
          "globalKey": "3f0b12"
        }
      }
    }
```

**From CDM v7 onwards**

``` json
   "trade": {
     "tradeDate": {
        "@key": "3f0b12",
        "@data": "2017-12-18"
      }
    }
```

## Full specification

The serialization standard is defined by the Rune DSL and applies to all Rune-based models, including the CDM. The full specification — covering the design principles, the complete set of special attributes, generation, ingestion and validation behaviour, and worked examples — is documented on the Rune DSL website:

- [Rune Serialization documentation](https://rune.finos.org/docs/serialization)
