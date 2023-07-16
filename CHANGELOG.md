## Changelog
Not a "proper" versioned changelog, just a list of the changes from Minestom master.
Some of these are pending, some deserve PRs, others are just minor tweaks

* **breaking** Delete extensions (`mworzala/Minestom` @ `no_more_extensions`)
* **breaking** Block face in digging events (`mworzala/Minestom` @ `block_break_face`)
* Change `Entity#getInstance` to @UnknownNullability
* Support custom component translator for serverside translation
* **breaking** Remove tinylog and MinestomTerminal implementation
* Add `Tag.Transient`
* Optionally allow multiple parents in event nodes
* **breaking** Add sender to argument parsing chain
  * This allows for argument parsing based on the sender, such as in argument map. This was already present for suggestions, but not for parsing.
  * This is a breaking change because it changes the signature of `Argument#parse`, but most use cases should not be affected.
    Support has been maintained for the old argument map signature, so only completely custom arguments will be affected.
* **breaking** [Placement rule api changes](https://github.com/hollow-cube/minestom-ce/pull/20)
* **breaking** Block update rework
  * Block updates are optional, placements in instances can be done without triggering updates (will not call placement rule place or update events) 
  * Block updates are not always triggered by a block place (only if a neighbor update triggers one back)
  * Block updates now only update adjacent blocks, not diagonals. This is inline with vanilla behvaior
  * Block placement rules can dictate a max range where updates will be applied. Defaults to 10 to be more compatible with prior behavior.
  * Block placement updates receive the block face that triggered the update
* Optionally use reworked chunk sending algorithm (`minestom.use-new-chunk-sending` system property)
