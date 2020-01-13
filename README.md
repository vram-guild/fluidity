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
Public interfaces for use and implementation by mod.  These are the only types an implementation *must* use or be aware of.

* [**`grondag.fludity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base)
Base implementations and helpers.  These are extensive and probably of interest to most implementations but their use is entirely optional.


## Articles

### Best Practices
Avoid NBT for anything other than items

## Storage and its Variants

## Devices

## Transactions

### Best Practices - enlist and support auto enlist

# Using Fluidity

## Dev Environment Setup

## Examples

