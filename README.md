# Introduction
Fluidity is a game resource storage and transport API for the Fabric toolchain. 

## Purpose
Provide a consistent and comprehensive API for storage and transport of any quantifiable and uniquely identifiable in-game resource, with excellent performance and flexiblity of scale and usage.

## Features
* Resource-type registry with pre-registered types for Items and Fluids
* Unified interfaces for all types of resources
* High-resolution, large-scale fraction implementation for lossless accounting of fluids and other resources that are continuous instead of discrete, or which have sub-unit quantization
* Registry for discovery and access to specific storage/transport implementations
* Transactions with support for heterogeneous resource types, nesting and concurrency (with limitations)
* Many base classes and helpers to support implementations

## License
Fluidity is licensed under the Apache 2.0 license for easy and unrestricted inclusion or modification in other projects.

## Status
Fluidity is still in active development and breaking changes are likely. The author recommends that usage be restricted to testing and evaluation for now.  All public-facing interfaces and classes are (or should be) annotated with `@API(status = Status.EXPERIMENTAL)`.  These annotations will be updated as the library stabilizes.

Note that issue reports and pull requests are welcome and encouraged.

## Relations

### Fabric API
Fluidity is designed, coded and licensed so that some or all of it could be incorporated into the Fabric API if wanted.  However, it is much more extensive than the Fabric project would likely want to take on. And unless or until Fluidity stabilizes and emerges as some sort of <em>de-facto</em> community standard there is no particular justification for incorporating a subset of it into the core Fabric API.

It seems more likely that Fabric API will eventually incorporate some less comprehensive set of interfaces that may be imfluenced by or derived from this and other community efforts, such as LBA (mentioned below.). In that case, the author intends to make Fluidity support and extend relevant "official" APIs as they emerge.

### LibBlockAttributes
Fluidity necessarily covers some of the same ground as [LBA](https://github.com/AlexIIL/LibBlockAttributes).  The author intentionally did not closely study LBA while Fluidity was being developed, both in order avoid making a derivative work and to ensure a fresh perspective.  That said, cross-compatibility is an explict goal and will be pursued when Fludity stablizes and as time permits.  From admittedly superficial observation, this appears attainable with reasonable effort.

### Cardinal Components
Fludity Device Components (explained below) may be seen to overlap somewhat with [Cardinal Components API](https://github.com/NerdHubMC/Cardinal-Components-API). However, this apparent redundancy is superficial.  

Fluidity Device Component Types focus on discovery and retrieval of implementations and does not provide dynamically extensible data attributes, serialization or other facilities offered by CCA.  Indeed, CCA may prove to be quite complimentary to some Fluidity implementations.    

### Vanilla Minecraft
Fluidity currently has no Mixins and makes no (intentional) changes to vanilla Minecraft behaviors.  Implementations are expected to make their own choices regarding their compatibility with vanilla mechanics.  The poor encapsulation of `Inventory` and it's sub-types is particularly problematic for large-scale storage networks that aim to be performant, and there is no clear best answer for how compatible vs how optimized any given block or item ought to be - it depends on how it will be used.

The library offers some base implementations and helpers to more easily support `Inventory` when that is the desired outcome, along with a way to register handlers for buckets, bottles and similar items.  Even so, vanilla mechanics received cursory attention at best in the initial development and the author considers this an area of opportunity for future improvement. 

# Overview

## How Fluidity is Organized
The fludity source tree is divided into four packages as follows:
* [**`grondag.fludity.api`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/api)
Public interfaces for use and implementation by mods.  These are the only types an implementation *must* use or be aware of.

* [**`grondag.fludity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base)
Base implementations and helpers.  These are extensive and probably of interest to most implementations but their use is entirely optional.

* [**`grondag.fludity.impl`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/impl)
Internal implementations and helpers.  Mods should not directly reference anything in this sub-tree.

* [**`grondag.fludity.wip`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/wip)
Work-in-process code that will *probably* become part of the library in the near future but is more experimental than even the API Guarding `EXPERIMENTAL` annotation would indicate.  Mods are welcome to look at it, test and provide feedback but should have no expectation of stability. This sub-tree replicates the api/base/impl divisions of the main API to indicate where the code will eventually land.

## Articles
An `Article` is a game resource that can be uniquely identified, quantified and serialized. An `ArticleType` defines the class of resource and provides packet and NBT serialization functions.  

Fluidity pre-defines two article types: `ArticleType.ITEM` and `ArticleType.FLUID` to represent in-game items and fluids.  However, any class can be used as an article type via `ArticleType.Builder` and `ArticleTypeRegistry`.  Some possible uses would include non-fluid bulk crafting resources, XP, power systems, or mana.  

Note that no restriction is made against defining new article types that also use `Item` and `Fluid` resources.  However, for compatibility it is recommended that mods adopt the predefined `ITEM` and `FLUID` article types for inter-mod resource storage and transport.

### Discrete vs Bulk Articles
The creator of an article type chooses if the article is *discrete* or *bulk*.  Discrete articles are meant to be counted as individual, atomic units.  Bulk articles are divisible into some unit less than one or fully continuous and thus meant to be measured using fractions. (More on those in a bit.)

However, this distinction is *purely advisory.*  Fluidity is designed so that *any* article type can be measured using either sort of accounting.  Whole numbers, after all, are simply a sub-set of the rational numbers.  The main benefit to using integers over fractions is slightly better performance and memory efficiency.  But if you want to build an "item tank" that stores fractional pick-axes, this is the library for you.

### Getting an Article Instance
Use `Article.of(articleType, resource)` to get a custom article.  `Article.of(item)` and `Article.of(fluid)` are more concise for those common types.  `Article` also exposes static methods for de-serializing any article from an NBT tag or packet buffer.

Retrieving an article is generally non-allocating after the first retrieval because all article instances are interned or cached. This means article instances can be compared using `==` *unless* they contain an NBT tag. (See below)  For this reason, Articles should always be compared using `.equals()` unless the situation absolutely ensures no tags are present on any article being compared.

### Article NBT Tags
An `Article` *may* have an NBT CompoundTag value associated with it.  Currently this functionality is only exposed for `ITEM` articles (because Minecraft requires it) but may be opened up for other article types in a future release.  However, mod authors are *strongly* advised to avoid using tags in favor of simply creating a larger number of distinct resource instances.

When an article has a non-null tag value there can be a virtually infinite number of distinct instances. Interning such articles would create the risk of excessive memory allocation.  Thus, article instances with tag values are held in a fixed-capacity cache and evicted as needed, making them slightly less efficient than articles without tags.

To ensure that articles are immutable, an article's tag instance is not directly exposed. The `copyTag()` method is the only way to get the tag content, but results in a new allocation every time.  If you only need to test for tag existence or test for tag equality use `hasTag()` or `doesTagMatch()`.

### Stored Articles
When an article is being stored or transfered we need additional information: quantity and, sometimes, a location. A core design principle of Fluidity is that all such data should never be directly mutated outside of the storage/transport implementation - all changes *must* be the result of some controlled, observable transaction.

This is why the API that exposes this information is immutable: `StoredArticleView`.

`StoredArticleView` includes *both* a whole-number `count()` (for discrete articles) and a fractional `amount()` (for bulk articles).  This creates a tiny amount of extra work for implementations (which is largely handled automatically via the base packages) at the benefit of having fewer/simpler interfaces overall.  Consumers of the API can use whichever accounting method makes sense for their purpose.  

Note that the `count()` property will not reflect fractional amounts less than a unit and so is not a reliable test of emptiness for implementations that may contain bulk items.  To test for emptiness, use `isEmpty()`.

`StoredArticleView` also has a special instance meant to be used in place of `null` values: `StoredArticleView.EMPTY`.  Implementations should return this instance instead of `null` when the intent is to signal the absence of a result.

#### Implementation Support
Obviously, implementations *will* need to mutate their contents and most implementations will be firmly discrete or bulk - not both.  The [`grondag.fluidity.base.article`]() package provides specialized discrete/bulk interfaces and classes to support most types of implementations.  Use of these is entirely optional but mod authors are encouraged to examine them for illustration before creating their own.  

#### Stored Article Handles
`StoredArticleView` also exposes an integer `handle()` property, which is *similar* in purpose to vanilla inventory "slots" but also different in key ways:

* Handles are not guaranteed to correspond to a specific, "physical" location in storage. Some implementations (something like a Storage Drawers mod, for example) may have this contract, but it is never required.

* Handle can be used to retrieve a storage view (similar to `List.get()`) but the targets of storage transactions are *always* specified by article - never by handle.  This ensures that no transaction is ambiguously or erroneously specified. A vanilla `Inventory` will let you blindly replace or change the contents of a slot without knowing or validating what was in it. Fluidity *never* allows this in its public API. Implementations that extend this to allow transactions based on handle (again, something like Storage Drawers would require this) are advised to *also* include article as part of any transaction specification. (The `FixedDiscreteStorage` interface in `grondag.fluidity.base.storage.discrete` and its sub-types offer an example of this.)

* Storage implementations are required to maintain a consistent handle:article mapping for as long as the storage has any listeners. In practice, this means preserving empty handles and creating new ones when articles are completely removed and new articles are added.  This makes is much easier for listeners to maintain a synchronized view of content and/or respond appropriately to changes.  Except...

* Implementations that *do* have physical slots *may* change a handle:article mapping, but when doing so must send listeners two events: one to remove the article from its current handle association (the listener would associate the old handle with `StoredArticleView.EMPTY`) and a second event to re-add the article with its new handle.  Implementations that have other reasons to change handle:article mappings may also do so if they follow the same practice.

## Fractions

## Store and its Variants

## Devices

## Transactions

## Multiblocks

## Carriers

### Best Practices - enlist and support auto enlist

# Using Fluidity

## Dev Environment Setup

## Examples

