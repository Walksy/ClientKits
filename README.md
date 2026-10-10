# Store, edit and load your custom kits!

Save kits from your inventory, build them by hand in a config screen, and load them back on
singleplayer or any multiplayer server you can use creative on.

Kits are stored and edited through **WalksyLib**: each kit is a named entry with its own full
41‑slot inventory, and every slot has a real item editor behind it (count, damage, unbreakable,
custom name, lore, enchantments).

### Commands

- **/ck save `<name>`** — save your current inventory as a new kit.
- **/ck load `<name>`** — equip a kit. If you are not in creative, ClientKits briefly switches
  you there and back (turn this off with *Auto Switch To Creative* in the config).
- **/ck delete `<name>`** — delete a kit.
- **/ck preview `<name>`** — open a read‑only view of a kit.
- **/ck list** — list your kits.
- **/ck config** — open the ClientKits screen to add, rename and edit kits slot by slot.

The screen is also reachable from **Mod Menu** → ClientKits.

### Notes

- Loading a kit needs creative access (singleplayer, or a server that lets you use it).
  Previewing, saving, editing and deleting work in any mode.
- Kits live in `.minecraft/config/clientkits.json` — one file, easy to back up or share.
- Deletion is permanent; keep a copy of the file if you want backups.

### Requires

Fabric Loader, Fabric API and **WalksyLib**. (During development WalksyLib is consumed from
`libs/` — see `build.gradle`.)
