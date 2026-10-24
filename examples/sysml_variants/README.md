# SysML Variants

An evolution history of a small SysML v2 vehicle model. Each variant contains a `.config` file
that records the features present in that model. Check the variants into an ECCO repository in
order (`V1` through `V10`), then check out feature combinations to compose different vehicle
products.

## Model

Every variant contains the `CORE` feature: a `Vehicle` part definition and a `myVehicle` instance.
The optional features add independent model elements:

* **SEATING** adds a passenger-capacity attribute.
* **POWERTRAIN** adds an `Engine` part definition and an engine part inside `Vehicle`.
* **BATTERY** adds a battery-capacity attribute.
* **SAFETY** adds a safety requirement and satisfies it from `Vehicle`.

The ten variants intentionally include both cumulative and non-cumulative combinations. This
shows that variants are feature selections rather than ten unrelated complete models.

| Variant | `.config` | Features |
|---------|-----------|----------|
| `V1` | `CORE.1` | core vehicle |
| `V2` | `CORE.1, SEATING.1` | core + seating |
| `V3` | `CORE.1, POWERTRAIN.1` | core + powertrain |
| `V4` | `CORE.1, SEATING.1, POWERTRAIN.1` | core + seating + powertrain |
| `V5` | `CORE.1, BATTERY.1` | core + battery |
| `V6` | `CORE.1, SEATING.1, BATTERY.1` | core + seating + battery |
| `V7` | `CORE.1, SAFETY.1` | core + safety |
| `V8` | `CORE.1, POWERTRAIN.1, BATTERY.1` | core + powertrain + battery |
| `V9` | `CORE.1, SEATING.1, POWERTRAIN.1, BATTERY.1` | core + seating + powertrain + battery |
| `V10` | `CORE.1, SEATING.1, POWERTRAIN.1, BATTERY.1, SAFETY.1` | all features |

The model deliberately uses SysML constructs supported by the SysML v2 adapter: packages, part
definitions and usages, attributes, requirements, documentation, and satisfy relationships.
