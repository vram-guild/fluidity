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
Fluidity represents Fractions as three `long` values: whole units, numerator and denominator.  While two longs (numerator and denominator) would arguably be sufficient for most use cases, that arrangement would mean that scale and sub-unit resolution would would vary inversely.  In an unpredictable multi-mod environment, it is better for mod authors (and mod testers) if the maximum resolution and scale of fractions are both invariant.

Fluidity includes two concrete implementations as part of the public API: `Fraction` and `MutableFraction`. These are and do exactly what you would expect based on the their names. Implementations that frequently update fractional values will generally want to use `MutableFraction` instead of allocating new `Fraction` instances with every operation.

For this reason, consumers of `Fraction` should *never* retain a reference but instead copy the value to a `MutableFraction` instance, or use `Fraction.toImmutable()` to ensure you have a Fraction instance that is safe to hold.

These classes were designed to be final in order to ensure static invocation in what are likely to be frequent calls and mod authors should not try to sub-class or modify them via reflection, Mixins or other esoteric methods. If you really need them to do something else, please submit a PR.

### Fractions without 'Fraction'
In many places, fractional quantities are exposed and manipulated as primitive values, without use of `Fraction` itself.  These interfaces (see below and in the code) instead rely on `long` values that correspond to `Fraction.whole()` or `Fraction.numerator()` and `Fraction.denominator()`.  When available and appropriate for your use case (i.e. when you have fixed minimum and maximum transfer amounts) these will generally be more performant and easier to work with. 

## Store and its Variants
A `Store` in Fluidity is an instance that holds and, optionally, supplies or accepts Articles. Stores may also publish an event stream that is useful for synchronizing store contents to client-side GUIs or implementing aggregate views of multiple stores.

A store may contain any `ArticleType` or combination of article types, using either discrete or bulk accounting and the interfaces are designed so that all implementations *must* support queries and operations using either discrete or bulk (fractional) quantities. This allows consumers of any storage implementation to have rigid and simple code paths, and helps to limit redundancy and the size of the API more generally.

In practice, most implementations are likely to store one article type (or a small, focused set of article types) and will use the form of accounting (discrete or fractional) most appropriate for that set.  Fluidity includes base classes and helpers to make this flexibility relatively easy to attain - mod authors should only need to handle the use cases that matter for their implementation.

### Querying Store Content
Unlike `Inventory` a `Store` is never asked to expose its internal representation of content, or even all of its content. You can ask a store a question, and it will give you the answer it wants you to have. A store should not lie, but it doesn't have to tell the whole truth.  Consumers should expect store query results to be consistent, but shouldn't try to infer information the store hasn't explicitly provided.

#### Best Practice: Don't Query Store Contents
The rest of this section will describe the various ways you can get information about a store, most of the time you should not. If a store reports it has an article, that does *not* imply the store will supply a certain amount of the article if asked.  If a store is empty, that does not mean it can accept a particular article in whatever amount.

The *only* reliable way to know if a store can accept or supply some quantity of an article is to try it - simulating if the intent is informational.  This is a very deliberate limitation in the contract of the `Store` interface, and is meant to ensure that `Store` implementations have flexibility in how they operate.  Any attempt to codify the rules what a chest or tank might be allowed to do would have to anticipate every possible use case (and thus be very extensive). And it would almost certainly fail in this attempt, eventually becoming an obstacle to somebody's cool idea.

Another expected use of store queries might be to synchronize information to clients for display, or to aggregate contents from multiple stores into a single view.  In both cases, subscribing to the stores' event streams (described below) will be more reliable and performant.

The best (and intended) use of the query mechanism explained below is to emulate `Inventory` behaviors, or gather summary information for *ad-hoc* display or debugging, especially when working with stores from different mods. They also serve as a back-stop to handle use cases that were not otherwise anticipated. 

#### Querying by Handles
As described earlier, a `StoredArticleView` exposes a `handle()` property, and iterating a store's contents via handle or retrieving the article view for a specific known handle will usually be the most straightforward way to navigate store content in those rare cases when it is necessary. The relevant members are `handleCount()` and `view(handle)`.

Stores are generally not meant to be thread-safe and consumers should check `handleCount()` or `isHandleValid()` before calling `view(handle)` but `Store` implementations should return `StoredArticleView.EMPTY` when an invalid handle is encountered.  This is a functionally correct result, and simplifies store implementation and usage overall.

#### Query via `forEach`
`Store` also exposes a pair of specialized `forEach()` methods.  These methods have naive default implementation that simply iterate using handles, but should be overridden by `Store` implementations where iteration by handle may be expensive. Unlike a standard `forEach` method, these accept a `Predicate` as the action, and iteration will stop if the predicate returns `false.` This behavior is especially useful when you only need to act on the first article. An alternate version of `forEach()` accepts a second predicate the `Store` will use to filter articles before applying the action.  This simplifies the action predicate, and may be faster for some implementations.

#### Querying Quantities
Sometimes, you just need to know if a store has a particular article in stock.  The `countOf()` and `amountOf()` methods offer this ability. But note again: the only reliable way to know if a store can actually supply an article is to request it and observe what happens.  This is exactly how these methods work in their default implementation - they simulate maximum extraction of the requested article and return the result.

### Store Operations
While a store may receive requests for any article type with either discrete or fractional quantities, a store can reject or partially fulfill any request that isn't supported,  simply by returning zero or a lesser amount than what was requested. 

For example, a fluid tank could be implemented using discrete accounting to store only whole buckets.  Such a tank would return empty results for any request to fill or drain any sub-unit amount, and would only partially fulfill requests for one or more units that also include a fractional amount.  A tank could also be designed to only accept a single type of fluid, and thus reject any request to drain or fill other fluids (or other types of articles).  More generally, implementations can adopt any constraint that doesn't violate the contract of exposed interfaces, and those interfaces were designed to allow flexibility.

#### ArticleFunction
Storage input and output operations use the same interface: `ArticleFunction`.  

`ArticleFunction` is overloaded, but every variant accepts an `Article` (or something that can be converted to an `Article`, like an `Item`) and a `simulate` parameter.  When the `simulate` parameter is true, the result of the operation will be a forecast only and the state of the Store will not change.

The remaining variation is in how quantities are specified, as follows:

* A single `long` value - for discrete articles or when only whole (bucket) units are needed.  The result will also be a whole unit.

* Two `long` values - a numerator and denominator - for fractional accounting using primitives.  The result will a  multiple of the denominator.

* A Fraction instance - for full-scale, full-precision fractional accounting. The result will be a Fraction.

All `ArticleFunction` implementations *must* accept all of these variants, but as with `Store` most implementations will adopt a specific form of accounting internally and rely on the base classes provided by Fluidity to convert mismatched requests to an appropriate form.  

Note there are some subtle differences in how the quantity input affect the outputs: variations that accept `long` values must also return results that can be expressed as `long` values.  This means, for example, that a request to supply 2/3 from a tank that is 1/2 full can only return 1/3 if the result must be expressed as two long values. But if the same request is made with 2/3 (as a `Fraction`) then the result will be 1/2 (also as a `Fraction`).

The lesson here is this: if you can accept Fraction values then you should also make requests using Fraction values.  If you can't accept Fraction values, then primitive values are exactly what you need, because that will ensure you never get a result you can't accept.  In either case you should not assume an empty result means a store is empty or full - it only means the store can't supply or accept the amount requested in the form you requested it.   (Use `Store.isEmpty()` or `Store.isFull()` to answer those questions.)

#### Supplier and Consumer Article Functions
The quantity parameters to `ArticleFunction`, in any form, are *always* zero or positive.  (Fraction numerators must be >= 1) The direction of articles in or out of storage is instead implied by which article function is called: 

* **`Store.getSupplier()`** Use to remove articles from a store. (Or to forecast the result of a removal request,)

* **`Store.getConsumer()`** Use to add articles to a store. (Or to forecast the result of an add request.)

Both methods by default return `ArticleFunction.ALWAYS_RETURN_ZERO` - a special implementation of `ArticleFunction` that, unsurprisingly, always returns zero in response to any request. A store can be made insert-only or extract-only by overriding only one of these methods.

Implementations should not override these methods to return `null`. The default return value can be used as-is with the (correct) result that no operation will change state.  Classes that do frequent operations on many stores may see some performance benefit from excluding inoperable stores by testing for the presence of a real implementation using the convenience methods `hasConsumer()` and `hasSupplier()`. 
 
### Store Event Streams and Storage Listeners
A store can optionally expose a `StorageEventStream` (via `storageEventStream()`) to broadcast changes to storage state to interested listeners.  Listeners must implement `StorageListener` and pass themselves to `StorageEventStream.startListening()` to begin receiving events.

The `StorageListener` interface, like others in Fluidity, must handle both discrete and fractional accounting.  To this end, there are two versions of events, one with a single long quantity and one with a fraction.  Here as elsewhere, Fluidity includes base classes to reduce the burden on implementations.

As with `ArticleFunction`, quantity parameters are always positive.  Direction is shown by which method is called: `onAccept()` for articles coming into the store, and `onSupply()` for articles going out.  These methods include both the amount changed and the new amount that results.  This bit of redundancy is easy for the store to provide (it must have this information anyway) and makes many listener implementations simpler if they have no need to track prior state. It also makes it easier to identify state synchronization errors.   

Stores should not notify listeners of simulated requests. It would serve no useful purpose and there is no simulation flag in any of the notification methods.

Stores that are enrolled in a transaction (explained in a later section) should generally send notifications immediately and not wait for the transaction to be committed. Some listeners may depend on the store's state and could also be enrolled in the same transaction.  If such listeners are not notified of events as they happen, they could give invalid results.  This means that if a transaction is rolled back, the store must send events that reverse earlier notifications.  Again, the quantities are always positive - an `onAccpt()` notification is reversed by a matching `onSupply()` notification, etc.      

A store should respond to a new listener by immediately sending acceptance notifications for current content that should be visible to listeners.  This avoids the need for any listener to know how to query the store to build an initial state vector that will be exactly synchronized with the event stream. This behavior can be disabled when unneeded via the `sendNotifications` parameter to `startListening()`.  

The same pattern applies to the `stopListening()` method - the store should respond by sending supply notifications for all current content unless asked not to.  This behavior is particularly useful for aggregate views of multiple stores because store addition and removal can be handled by the same routines that handle accept and supply events.

Stores that do not implement `Store.eventStream()` should rely on the default implementation to return `StorageEventStream.UNSUPPORTED`. Consumers of event streams should check for the presence of a valid instance using `hasEventStream()` before subscribing. Subscribing to `StorageEventStream.UNSUPPORTED` logs a one-time warning as an aid to troubleshooting but no exception is thrown and no event notifications will be received. 

### API Variants
The API currently includes two variations on 'Store`:
* **`FixedStore`** For stores with fixed handles, adds handles to operations via `FixedArticleFunction` extension of `ArticleFunction` - for bins, drawers, or vanilla-like storage with fixed "slots"
* **`InventoryStore`**  Combo of `Store`, `RecipeInputProvider` and `Inventory` with a few default handlers - useful mainly as consistent shorthand for this combo

### Implementation Variants
[**`grondag.fluidity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base/storage) and its sub-packages include the following interfaces and classes to facilitate various types of `Store` implementations:

* **`DiscreteStore` and `FixedDiscreteStore`**  Extensions of `Store` and `FixedStore` with default implementations for fractional accounting - implement these when tracking whole units
* **`BulkStore` and `FixedBulkStore`**  Extensions of `Store` and `FixedStore` with default implementations for discrete accounting - implement these when tracking fractional units
* **`AbstractStore`**  Has core components of listener notifications
    * **`AbstractAggregateStore`**  Handles tracking of multiple stores
        * **`AggregateBulkStore`**  Tracks combined view of content from multiple stores using fractional accounting
        * **`AggregateDiscreteStore`**  Tracks combined view of content from multiple stores using discrete accounting
    * **`AbstractLazyRollbackStore`**  Adds single-store hooks for transaction participation and rollback
        * **`AbstractDiscreteStore`**  Base class for simple stores using discrete accounting 
            * **`CreativeBinStorage`**  What it sounds like - fixed handles and creative behaviors
            * **`DividedDiscreteStore`**  Non-`Inventory` store with fixed handles and per-handle capacity limits, implements `FixedStore`
            * **`FlexibleDiscreteStore`**  Non-`Inventory` store with dynamic handles and store-level capacity limit
            * **`SlottedInventoryStore`**  Fixed-handle, fix-slot store, implements `InventoryStore`
        * **`SimpleTank`** Single-article bulk store with volume limit
        * **`SingleStackInventory`** `InventoryStore` implementation wrapping a single `ItemStack`, prototype for storage items
* **`ForwardingStore`**  Wraps a `Store` instance - override methods as needed to modify behavior of an existing store 

## Transactions
Transactions allow a `Store` or any instance that implements `TransactionParticipant` to be notified when an operation is part of a transaction and may need to be undone.  Participants are then given an opportunity to save any necessary rollback state before the operation happens, and then notified when the transaction is committed or rolled back.

Participants can be explicitly enlisted in a transaction when their involvement is known, but implementations can also self-enlist.  This is particularly useful for transportation networks with cost accounting - the initiator of an operation may not know all of the attendant costs and side effects of the transport network, or even that they exist.  If the transport network can self-enlist, all of that can be handled without complicating the initiating code. 

Transactions are useful in at least two ways:
1. Operations across multiple participants do not have to forecast results with perfect accuracy, nor handle clean up of participant state when things do not go according to plan.  When working with complicated implementations (something like a Project E table, for example) both the forecasting and the cleanup could be nigh-impossible to get right and will inevitably result in undesirable coupling of implementations.
2. Code that initiates an operation does not have to know of and handle all of the possible side effects that could result because transaction participants that aren't directly known or meaningful to the initiator can self-enlist.

### Using Transactions
The transaction-related interfaces are located in `grondag.fluidity.api.transact`.
* **`Transaction`**  A single transaction - may be nested within another transaction. The initiator obtains this instance and uses it to commit or roll back the transaction, and to enlist participants.  Should be enclosed in a try-with-resources block - default close behavior is to roll back unless `commit()` was called successfully before `close()`.
* **`TransactionParticipant`**  Provides a `TransactionDelegate` and indicates if the participant is self-enlisting. Implement this on stores, transport carriers, machines or other game objects that can benefit from transaction handling. All Fluidity base implementations (except aggregate views) include transaction support.
	* **`TransactionDelegate`** Does the actual rollback preparation and handles closure notifications. Allows participants to share the same rollback state.  The `ArticleFunction` interface itself extends `TransactionParticipant` so it is common to have multiple `ArticleFunctions` instances that internally update the same state.
* **`TransactionContext`**  Exposed to participants at time of enlistment, and again at close.  Used to save and retrieve rollback state, and to query commit/rollback status at close.

Here's an example of simple transaction reliably transferring one unit of something between two stores:

```java
try(Transaction tx = Transaction.open()) {
	tx.enlist(firstStore);
	tx.enlist(secondStore);

	if (firstStore.getSupplier().apply(myArticle), 1) == 1 && secondStore.getConsumer().apply(myArticle, 1) == 1) {
		tx.commit();
	} else {
		tx.rollback();
	}
}
```

As we'll see in the Transport section we may only have an `ArticleFunction` to work with instead of a full-fledged `Store` instance.  That's why `ArticleFunction` also extends `TransctionParticipant` - you don't have to know where a supplier or consumer function came from to enlist it in a transaction.

### Implementing Transaction Support
The Fluidity base implementations include several different variations of transaction support that take advantage of specific implementation characteristics.  The reader can look to those as examples. In particular, see `AbstractLazyRollbackStore` and it's sub-types. The main principle to follow is to defer creating rollback state until something actually changes, unless creating rollback state is very inexpensive.  Often, the easiest way to accomplish this is to make `TransctionParticipant.isSelfEnlisting()` return true, and then call `Transaction.current.enlistSelf()` right before something changes.

Transactions track which delegates have already been enlisted (self-enlisted or otherwise), and guarantees that `TransactionDelegate.prepareRollback()` will be called exactly once, immediately when the delegate first becomes enlisted.

The rollback state provided by the delegate via `TransactionContext.setState()` can be any Object, or null.  It will never be inspected or altered by the transaction manager, and will be provided back to the delegate via `TransactionContext.getState()` when the transaction is closed.

The `Consumer` function returned by `prepareRollback()` will *always* be called, both when a transaction is committed and when it is rolled back deliberately or due to an exception.  Implementations *must* therefore check the value of `TransactionContext.isCommited()` to know what action is appropriate.

Oddball implementations that don't need to do anything on commit or rollback can return `TransactionDelegate.IGNORE` as their delegate, which does exactly what you'd expect it to do.  Examples of this are creative-type storage or void blocks where state is essentially immutable, and aggregate storage implementations that don't have any internal, independent state that would need to be restored but instead rely on their component instances to handle transaction state and change notifications as needed. 

However, a `Store` or other class that extends `TrasactionDelegate` *is* expected to provide transaction support to the extent that it means anything for that implementation.  Transaction delegates should not be null and they should be `TransactionDelegate.IGNORE` unless that gives "correct" results. 

See also the related notes regarding storage event streams and listeners, above.

### Transaction Mechanics
Fluidity Transaction State is global state.  There is only ever one current transaction, across all threads.

As we all know, global state is always a bad thing.  Sometimes, it is also the *least* bad thing. Nobody wants to wear the cleanest dirty shirt, and being forced to do so may motivate us to improve the regularity of our laundering habits, but sometimes we have no choice but to choose from a menu of unsavory options. The author believes such is the case here.

Earlier designs considered the possibility of partitioning transaction state into isolated scopes, and to allow concurrent transactions from multiple threads, much as a modern RDBMS would do.  However this immediately introduces many complicating problems, including the need to track which objects are visible in which scope(s) and the need for synchronization when such objects are referenced in more than one scope. It also creates the need to detect and handle deadlocks, or otherwise shape the API in a (probably onerous and restrictive) way so that deadlocks cannot occur. It's simply not worth it in the context of modded Minecraft, assuming it could be made to work at all before we have all moved on to other pursuits. 

When there can only be a single current transaction, transaction state is *de-facto* global state.  *Exposing* it as explicit global state is not essential, but it ends up being *very* nice for allowing transaction participants to self-enlist.  This single change greatly simplified implementations that benefit from lazy rollback preparation or want to automatically include side-effects (like transport costs) in the transaction that caused the side effects to occur. Doing this without global visibility requires cluttering the API to pass the current transaction up and down the call stack, or exposing it on some accessible instance such that it becomes effectively global anyway.

Fluidity also supports nested transactions, and making the overall transaction state a singleton allows the nesting implementation to be a simple stack of zero or more transactions associated with a single thread, which brings us to the question of support for multiple threads. 

Fluidity *does* support transactions initiated from any server-side thread, and automatically synchronizes the current state via a specialized locking mechanism. This mechanism makes the following guarantees:
* Only one thread can own the current transaction state.
* To open a new transaction, there must be no current transaction, or the calling thread must already own the current state.
* A thread that tries to open a transaction without holding the lock will block until the transaction is complete. This will not cause deadlocks unless an action somehow waits on a blocked thread through some dependency other than the transaction.  That would be strange and bad. Don't do that.
* Most important: the Minecraft server thread is *always* given priority above all other threads. If a transaction from another thread is open when the server thread tries to open a transaction, the server thread will block until that thread completes, and will then be scheduled before any other waiting threads.  Non-server threads should be scheduled in an approximately fair order. 

The rational for this last guarantee is performance: the server thread should not be held up by locks from other threads.  That said, the server thread will not be using the transaction state most of the time - server ticks only happen 20 times per second and, ideally, are short.  A future update may further restrict locks from other threads, only allowing them to proceed outside of the server tick event.

That all said, the best practice for opening transactions from other threads is: don't.  It makes everything much more complicated and prone to breakage.

If, like the author, you have some mods that *really* need to move some work off the server thread to avoid killing it, the answer is *still* don't.  If you are querying or changing any state you don't completely control, it probably doesn't expect to be queried or changed from anywhere other than the server thread, preferably during server tick. 

A better approach is to completely isolate the state that will be processed off-thread, buffering world state if needed, and then synchronizing with world state during each server tick.  During the server tick you can initiate transactions on the server thread, and those transactions can consume or produce state that is the result of or input to off-thread processing.  Server ticks may take a little longer to run, but you won't block them or break the game (probably), and usually there is time to spare or to be found with server-side optimization mods.  

Fermion Simulator and Working Scheduler both provide some mechanisms that could be useful for this sort of setup, and if you are committed to doing concurrent processing server-side it should be something you are comfortable doing on your own if needed.

Consistent with this recommendation, there is a non-zero chance support for transactions from non-server threads will be altogether *removed* in some future release and this is a topic on which the author would value feedback.  For now the feature remains to account for scenarios that may not have been anticipated, and because someone will probably try to do it anyway.

Lastly, note that transactions are a fully server-side construct, and no client-side code should ever reference them.  This is difficult to enforce directly without expensive checks, and so for now mod authors are on the honor system to get this right.  If it ends being a problem, some checks may be added, perhaps with configuration to turn them on or off.

## Devices

## Multiblocks

## Transport

# Using Fluidity

## Dev Environment Setup

## Examples

