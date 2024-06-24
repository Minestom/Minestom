# Minestom Scratch
Scratch is exposed as an alternative to minestom the framework - reusing the same logic (protocol, inventory handling, physics, pathfinding etc...) but unopinionated when it comes to control flow.

Benefits are multiple:
* Stay the strongest to our principles: It is better to add what you need than to remove what you don't
* Less reliance on maintainers: tools are simple enough for you to copy and start maintaining yourself
* More flexibility: you can have your own threading system, own world/entity/player classes.

# How to start
Copy the template the closest to your project from the `template` package and modify it as you wish.

# How to contribute
Few rules/guidelines:
* Each feature must be self-contained within a single file
* To complement on the point above, no scratch tool should have dependencies on other scratch tools
* Must be as small as reasonably possible (no > 1000 lines)
* Must do a single thing and do it well
* No complex hierarchies, duplicated code over different tools is accepted