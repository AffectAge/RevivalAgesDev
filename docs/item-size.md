# Item Size

Item Size is a data-driven inventory compatibility layer. It classifies stacks
as `tiny`, `very_small`, `small`, `normal`, `large`, `very_large`, or `huge`.
It does not change stack limits, player carrying capacity, movement, attributes,
or status effects.

The module is controlled by the default-enabled, restart-required
`itemSize.enabled` setting in `revivalages-item-size-server.toml`. Registry and
data-map identities remain available while the module is disabled. Disabling it
removes the tooltip and makes every built-in adapter accept stacks without a size
check.

## Size resolution

`SizeApi.getSize(ItemStack)` resolves one effective value in this order:

1. `ItemSizeProvider` implemented by the stack's item;
2. `ItemSizeProvider` implemented by the block of a `BlockItem`;
3. the synchronized `revivalages:item_size` item data map;
4. the built-in fallback.

The fallback assigns full-size tools, buckets, and armor to `large`, block items
to `small`, and other items to `very_small`. Datagen explicitly assigns every
public Revival Ages item, so this fallback is primarily for third-party content.
Datagen fails when a public Revival Ages item lacks an explicit assignment.

The item data-map file uses the standard NeoForge data-map format:

```json
{
  "values": {
    "examplemod:small_part": "small",
    "#c:tools": "very_large"
  }
}
```

Both exact item IDs and item tags are supported. The data map is synchronized to
clients during datapack synchronization.

## Container policies

The synchronized `revivalages:size_container` data map exists for both item and
block registries. Its value is inclusive:

```json
{
  "values": {
    "examplemod:storage_box": {
      "max_size": "normal"
    }
  }
}
```

Adding a policy does not modify an arbitrary container by itself. The container
must call `SizeApi.canInsert(...)` or provide an adapter that does so. This keeps
the API explicit and prevents data from claiming compatibility that runtime code
does not enforce.

The built-in adapters are:

- normal, double, and trapped chests, accepting through `large` by default;
- bundles, accepting through `normal` by default;
- Pit Kiln input capacity, with up to four items through `large` and one item
  above `large` by default.

Chest checks cover manual clicks, shift-click, single and double chest menus,
container insertion, and NeoForge item-handler automation. Existing oversized
stacks are preserved and may be extracted. Bundle checks cover both vanilla
stacked-on-item insertion gestures and preserve existing contents. The Pit Kiln
applies the effective capacity only to new insertion; reload does not eject an
input or interrupt an active firing.

Rejected player insertion attempts show an action-bar explanation and play a
short, low-pitched sound. Feedback is server-authoritative and rate-limited per
player; slot eligibility queries and automation do not emit it.

Barrels, Soaking Pots, and machine processing slots intentionally remain
recipe-driven.

## Configuration and synchronization

The server configuration exposes:

- `itemSize.chestMaximumSize`;
- `itemSize.bundleMaximumSize`;
- `itemSize.pitKiln.batchableMaximumSize`;
- `itemSize.pitKiln.batchSize`;
- `itemSize.pitKiln.oversizedBatchSize`;
- `itemSize.rejectionFeedback.actionbarEnabled`;
- `itemSize.rejectionFeedback.soundEnabled`;
- `itemSize.rejectionFeedback.cooldownTicks`;
- `itemSize.rejectionFeedback.soundVolume`;
- `itemSize.rejectionFeedback.soundPitch`;
- `itemSize.containerOverrides`.

Override entries use `block|namespace:id=size` or `item|namespace:id=size`.
They only affect containers with an installed adapter. Effective settings are
sent to clients on login, datapack synchronization, and configuration reload, so
tooltips and client presentation use the server's policy.

## Public API

`Size`, `ItemSizeProvider`, `ContainerSizePolicy`, `ItemSizeDataMaps`, and
`SizeApi` are public under `com.protyvkultury.revivalages.api.size`. Dynamic
providers should return a value derived only from the supplied stack. Container
adapters must call the common API for manual insertion and automation and must
never delete a pre-existing invalid stack.

The KubeJS binding `RevivalAgesItemSize` exposes read-only size resolution and
container checks. Scripts still define data-map values through ordinary
codec-validated datapack JSON.

## Optional integration assessment

| Catalog entry | Status | Version / side / notes |
| --- | --- | --- |
| KubeJS | applicable | Optional common-side read-only binding; base mod loads without KubeJS |
| Jade | applicable | Optional client display for supported chest policies and Pit Kiln input capacity |
| EMI | not applicable | The module defines no recipes or recipe UI |
| JEI | not applicable | The module defines no recipes or recipe UI |
| Curios | not applicable | The module defines no equipment or accessory slots |
| Progressive Stages | not applicable | The module defines no progression gate |
| Biomes O' Plenty | not applicable | Size resolution is not biome-sensitive |
| Serene Seasons | not applicable | Size resolution is not seasonal |
| Ecliptic Seasons | not applicable | Size resolution is not seasonal |
