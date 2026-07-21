# Client-Only Rules

This subtree is the physical-client boundary.

- Client classes may import `net.minecraft.client.*`; no code outside a `client`
  subtree may import or reference these classes.
- Register screens, renderers, model layers, color handlers, key mappings, and
  client payload handlers only from client-scoped mod events or a dist-specific
  entry point.
- Rendering reads synchronized state; it never owns gameplay truth or mutates
  server state.
- Input handlers send minimal intent to the server and implement local prediction
  only when reconciliation is defined.
- Do not access `Minecraft.getInstance()` from static initializers.
- Treat missing worlds, players, menus, and render entities as normal transient
  states. Handle them without crashing.
- Verify resource reloads, GUI scale, localization expansion, and both light/dark
  visual contexts where relevant.
