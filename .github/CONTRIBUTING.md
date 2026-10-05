## How to contribute to Minestom
#### **Did you find a bug?**
* Open a new GitHub issue if it's not already reported.

* Explain it clearly, with steps (or code) to reproduce it. 

#### **Did you write some code that fixes a bug?**
* Open a new GitHub pull-request with the commits if it hasn't already been proposed.

* Ensure the PR description clearly describes the problem and solution. Include the relevant issue number if applicable.

#### **Do you intend to add a new feature or change an existing one?**
* Do not open a pull-request on GitHub until you have collected positive feedback about the change from a maintainer.

#### **Does your change break existing code?**
* Breaking changes need a maintainer's approval. Once approved, a maintainer labels the pull-request as `breaking`, which lets it pass the binary compatibility check.

* Add a `## Migration` section to the PR description. Give each breaking change its own block: a one line description, the code before the change, a `:arrow_down:` line, then the code after it. These blocks are copied into the release notes.

* This also applies to changes that only break source compatibility, or that remove internal API people are known to use, even when the binary compatibility check passes.

* When something is removed without a replacement, say so in one line instead of a block.

````markdown
## Migration
Registry keys are created with `of`.
```java
RegistryKey<Biome> key = RegistryKey.unsafeOf("minecraft:plains");
```
:arrow_down:
```java
RegistryKey<Biome> key = RegistryKey.of("minecraft:plains");
```
````

#### **Do you have questions about the source code?**
* Ask any question about how to use Minestom in the GitHub issues section or the community portals.

#### **Do you want to contribute to the Minestom documentation?**
* Feel free to do so! Just make sure to conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification when editing the README.md.

#### **Naming Tests**
* Plain unit tests are `*Test`.

* Tests using the `@RegistriesTest` registry snapshot are `*RegistriesTest`.

* Tests using the `@EnvTest` server are `*IntegrationTest`.

* Prefer the lightest fixture that covers the behavior, and make sure your test class passes when run alone. The `testNamingCheck` task enforces the naming as part of `check`.

## General Contribution Rules
* By contributing to the Minestom project your code/contribution will be licensed under the [Apache Version 2.0](../LICENSE) license.

Minestom is a community project. We encourage you to contribute! :)

Thanks! :heart: :heart: :heart:

~Minestom Community