# Storage Barrel

The Storage Barrel is a general-purpose item container separate from the
processing Barrel. It accepts arbitrary item stacks while open and applies the
existing `revivalages:preserved` food trait while sealed.

The server configuration controls its enabled state, slot capacity, automation,
and expired-food materialization interval. Slot capacity is rounded down to a
complete row of nine and requires a restart. All registry objects remain present
when disabled.

Sealing requires a Barrel Lid on the top face. A sealed barrel blocks its menu
and all item capability operations, including capability objects obtained before
sealing. Empty-hand interaction on the top face returns the lid and removes the
temporary preservation trait. Food continues to materialize while sealed, so
preservation slows spoilage rather than freezing it.

## Optional integration assessment

| Integration | Status | Reason |
| --- | --- | --- |
| Jade | applicable | Sealed state, capacity, and preservation are inspectable block state. |
| KubeJS | not applicable | The container exposes no custom recipes or script operation. |
| JEI | not applicable | It has no processing recipes or viewer category. |
| EMI | not applicable | It has no processing recipes or viewer category. |
| Curios | not applicable | It is not wearable or player-bound equipment. |
| Progressive Stages | applicable | Its stable crafting recipe ID can be gated. |
| Biomes O' Plenty | not applicable | Behavior is not biome-sensitive. |
| Serene Seasons | not applicable | Behavior is not seasonal or climate-sensitive. |
| Ecliptic Seasons | not applicable | Behavior is not seasonal or climate-sensitive. |
