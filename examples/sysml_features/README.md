# SysML v2 Features Example

This is a separate, feature-oriented SysML v2 example. Unlike
[sysml_variants](../sysml_variants), which models product configurations, this example grows one
small autonomous sensor system by adding different SysML language capabilities.

The variants are intentionally cumulative:

| Variant | `.config` | Added SysML capabilities |
|---------|-----------|--------------------------|
| `V1` | `CORE.1` | packages, part definitions/usages, attributes |
| `V2` | `CORE.1, ITEMS.1` | item definitions and item usages |
| `V3` | `CORE.1, ITEMS.1, PORTS.1` | port definitions, ports, and connections |
| `V4` | `CORE.1, ITEMS.1, PORTS.1, BEHAVIOR.1` | action definitions, parameters, and flows |
| `V5` | `CORE.1, ITEMS.1, PORTS.1, BEHAVIOR.1, ANALYSIS.1` | requirements, constraints, states, and transitions |

The model demonstrates:

* structural modeling with `package`, `part def`, and part usages;
* typed `attribute` values and reusable `item def` payloads;
* interaction modeling with `port def`, ports, `connection`, and `flow`;
* behavior modeling with `action def`, `in`/`out` items, and action usages;
* analysis and verification concepts with `requirement def`, `constraint def`, `satisfy`,
  `state def`, and transitions.

Check the variants into an ECCO repository in order and check out the configuration strings to
compose the feature history.
