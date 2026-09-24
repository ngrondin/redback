# Redback Screen Configuration Reference

This document is a guide for humans and coding agents authoring **Redback screen configurations** — the JSON documents the Redback server sends to this Angular SPA to define screens. The SPA reads these configurations and dynamically instantiates UI components to build each screen.

All key names documented here are the **JSON keys** used in configuration files.

> Source of truth: the component registry in `src/app/loader.ts` (`componentRegistry`), the input-binding logic in `src/app/services/build.service.ts`, and the `@Input()` declarations of each component class. A machine-checkable verification script lives alongside this document (`verify-screen-config-doc.mjs`).

---

## Table of contents

1. [How screens work](#1-how-screens-work)
2. [Common inputs (by base class)](#2-common-inputs-by-base-class)
3. [Component reference](#3-component-reference)
   - [3.1 Data sources](#31-data-sources)
   - [3.2 Layout & structure](#32-layout--structure)
   - [3.3 Forms & field inputs](#33-forms--field-inputs)
   - [3.4 Lists, tables & trees](#34-lists-tables--trees)
   - [3.5 Buttons & action controls](#35-buttons--action-controls)
   - [3.6 Graphs & visualizations](#36-graphs--visualizations)
   - [3.7 Misc display & dynamic forms](#37-misc-display--dynamic-forms)
4. [Payload & expression formats](#4-payload--expression-formats)
   - [Filters](#41-filters)
   - [Expressions](#42-expressions)
   - [Link / navigation config](#43-link--navigation-config-linkconfig)
   - [Colors](#44-colors-colorconfig)
   - [Value formats](#45-value-formats)
   - [Actions](#46-actions)
   - [Remote control: initialcontrols & comptargets](#47-remote-control-initialcontrols--comptargets)
   - [Modals](#48-modals)
   - [Search modes](#49-search-modes)
   - [Table columns](#410-table-columns)
   - [Gantt config](#411-gantt-config)
5. [Worked examples](#5-worked-examples)
6. [Appendix: type → class mapping](#6-appendix-type--class-mapping)

---

## 1. How screens work

### 1.1 View configuration

A **view configuration** is a JSON object fetched by name from the server (`GET` on the view resource). Its shape:

```json
{
  "name": "myview",          // view identifier (set by the server)
  "label": "My View",        // display title of the view
  "onload": "console.log('view loaded')",   // optional JS body, executed on first load of the view (in the app's global context)
  "content": [ /* array of component configuration objects, see 1.2 */ ]
}
```

Only `content` (plus `label`/`onload`) matter for rendering. `content` is an **ordered array** of component configuration objects; they are instantiated in order, top to bottom.

### 1.2 Component configuration objects

Every entry in `content` (and in nested `content` arrays) is a **component configuration object**:

```json
{
  "type": "<component-type>",     // REQUIRED — selects which component to instantiate
  "<input-alias>": <value>,       // any number of inputs
  "content": [ /* child component objects — only for container components */ ]
}
```

Rules the build process applies (see `src/app/services/build.service.ts`):

- `type` must be one of the keys in the component registry (§6). An unknown `type` is silently skipped.
- Every other key in the object that matches an **`@Input` alias** of the target component is set on that instance. Keys that don't match any input are ignored.
- Input aliases are case-sensitive as documented. Aliases — not TypeScript property names — are what go in JSON. Example: `@Input('object') objectname` means the JSON key is `"object"`.
- The `content` key is consumed by the builder, never set as an input. It is only honored when the component is a **container** (see 1.4).

### 1.3 Context inputs (do NOT put these in JSON)

Seven inputs are **context-injected** by the builder: their value comes from the nearest enclosing ancestor, not from the JSON object. Putting them in JSON is pointless — the context value takes precedence:

| Input | Meaning | Provided by |
|---|---|---|
| `dataset` | The `dataset` component enclosing this component in the config tree | nearest ancestor `dataset` |
| `datasetgroup` | The `datasetgroup` enclosing this component | nearest ancestor `datasetgroup` |
| `aggregateset` | The `aggregateset` enclosing this component | nearest ancestor `aggregateset` |
| `fileset` | The `fileset` enclosing this component | nearest ancestor `fileset` |
| `activator` | The activation source for visibility/lifecycle of children | the view root, and any enclosing `tab`, `modal`, `vcollapse`, `hcollapse`, `repeater` |
| `tabsection` | The `tabsection` enclosing a `tab` | nearest ancestor `tabsection` |
| `virtualselector` | A per-iteration object selector | `repeater` (one per repeated object); any ancestor `repeater` context |

Practical effect: a `dataset` placed inside a `dataset` automatically observes its parent — this is the standard **master–detail** pattern. Components that need the parent's *selected* object to load their own data get it for free.

### 1.4 Containers and nesting

Components whose class extends `RbContainerComponent` have a `content` slot and can hold children. Containers are:

`layout`, `hsection`, `vsection`, `tabsection`, `tab`, `vcollapse`, `hcollapse`, `modal`, `repeater`, `tile`, `scroll`, `group`, `dataset`, `datasetgroup`, `aggregateset`, `fileset`, `form`.

All other components are leaf components; a `"content"` key on them is an error (logged, not rendered).

Top-level items are mounted directly into the view loader's container; wrap them in `hsection` / `vsection` / `layout` to control their arrangement (multi-panel screens typically use top-level `hsection`s with `grow` weights, see §5).

### 1.5 Sizing & alignment model

Containers are laid out with CSS flexbox. These container inputs control the layout:

| Input | Type | Meaning |
|---|---|---|
| `grow` | number | flex-grow (default 1 when no explicit width/height is set; 0 otherwise) |
| `shrink` | number | flex-shrink (default 1 when no explicit width/height) |
| `basis` | string | flex-basis |
| `width` | number | explicit width in **size-units** (see below) |
| `height` | number | explicit height in **size-units** |
| `mainalign` | `"start"` \| `"center"` \| `"end"` | justify-content — alignment of children on the main axis |
| `crossalign` | `"start"` \| `"center"` \| `"end"` | align-items — alignment of children on the cross axis |
| `color` | string | background color of the container |

**Size-units:** a value `N` means `min(0.88·N·vw, 17·N·px)` — the component is `N × 17px` wide at a 1920px window. `1` ≈ 2.4vw (≈17px at 1920). Typical input field `size` is 15 (≈ 255px at 1920). This same unit is used by `width`/`height`/`size` across components and by `spacer`.

Note: if both `width` and `height` are set, `grow`/`shrink` are ignored.

### 1.6 Activation

Components participate in the activation system: an `activator` (view root, `tab`, `modal`, `vcollapse`, `hcollapse`, `repeater`) controls whether its children are **active**. Inactive components are still built, but they do not fetch data and are not shown in tab/modals until activated. The dataset refreshes automatically when it becomes active.

---

## 2. Common inputs (by base class)

Every component listed below inherits the inputs of its base classes. The sections are ordered from most-base to most-specific; a component's full input surface = its own inputs (§3) + every base-class section it inherits.

**Inheritance map** (only bases relevant to config authors):

```
RbComponent
├── RbDataObserverComponent
│   ├── RbContainerComponent
│   │   ├── RbSetComponent                 (dataset, aggregateset, fileset)
│   │   └── RbActivatorComponent           (tab, modal, vcollapse, hcollapse, repeater)
│   │                                       (also: layout, hsection, vsection, tabsection, tile, scroll, group, form)
│   ├── RbInputComponent
│   │   ├── RbFieldInputComponent
│   │   │   └── RbPopupInputComponent
│   │   └── (some inputs extend RbInput directly)
│   └── RbDataCalcComponent                (gantt, calendar, timeline, funnel, map)
└── RbAggregateDisplayComponent            (graph, stackedgraph, vbargraph, hbargraph, numbertiles, pivot)
```

### 2.1 `RbComponent` — present on (almost) every component

| Input | Type | Default | Description |
|---|---|---|---|
| `id` | string | `null` | Stable identifier. **Give datasets and controlled components an `id`**: it is used to target datasets from navigation (`datatargets[].datasetid`), to target components (`comptargets[].compid`), and to key user preferences. |
| `activator` | — | context | Context-injected (§1.3); not set from JSON. |
| `initialcontrols` | object | `null` | Data pushed to the component's `control()` handler once it initializes (§4.7). |

> Exception: `image` and `icon` are plain components that inherit **nothing** from `RbComponent`.

### 2.2 `RbDataObserverComponent` — data-linked components

Adds the data-linking inputs. Most non-trivial components are data observers.

| Input | Type | Default | Description |
|---|---|---|---|
| `dataset` | — | context | Observe this `dataset` (enclosing one, §1.3). |
| `datasetgroup` | — | context | Observe this `datasetgroup`. |
| `aggregateset` | — | context | Observe this `aggregateset` (aggregate displays). |
| `virtualselector` | — | context | Observe this `repeater`'s per-iteration selector. |
| `datasetevents` | string[] | all events | Restrict which dataset events trigger re-render: e.g. `["select","load"]`. Events: `init`, `load`, `select`, `update`, `clear`, `removed`, `globalvariable`. |
| `object` | object reference | `null` | Explicit object (advanced; normally resolved from the dataset's selection). |
| `show` | string (expression) | shown | JS expression controlling visibility, re-evaluated on data events. May reference `object.`, `relatedObject.`, `dataset.` (§4.2). |
| `targetdatasetid` | string | — | When observing a `datasetgroup`, the `id` of the member dataset to target (used also by `search` modes). |

### 2.3 `RbContainerComponent` — containers

Adds layout inputs — see the sizing model in §1.5 (`grow`, `shrink`, `basis`, `color`, `width`, `height`, `mainalign`, `crossalign`).

### 2.4 `RbSetComponent` — datasets, aggregatesets, filesets

| Input | Type | Default | Description |
|---|---|---|---|
| `object` | string | **required** | The Redback **object type name** (domain object) this set loads. |
| `basefilter` | filter object | — | Base filter applied to all loads (§4.1). Values may be expressions (§4.2) resolved against the related object. |
| `master` | object | — | Master-slave link to the enclosing dataset's selection: `{"relationship": { <attr>: "'obj.uid'" }}` — the `relationship` filter is merged in (expressions resolve against the parent's selected object). When set, the set only loads data once the parent has a selected object. |
| `requiresuserfilter` | boolean | `false` | If `true`, the set loads nothing until the user has applied a filter (via a `search` component etc.). |
| `ignoretarget` | boolean | `false` | If `true`, navigation `datatargets` are not applied to this set. |

### 2.5 `RbInputComponent` — all field inputs

| Input | Type | Default | Description |
|---|---|---|---|
| `attribute` | string | — | Attribute of the **selected object** this input edits/displays. May be a path (`"related.attr"`) to read through related objects. When set, the input's value, editability (`object.validation[attr].editable`) and mandatoriness come from the object. |
| `value` | any | `null` | Static value, used when `attribute` and `variable` are absent. |
| `variable` | string | — | Name of a **global variable** (on the client runtime). The input binds to that variable; commit calls `setGlobalVariable`. Useful for cross-component state. |
| `label` | string | — | Label shown next to/above the field. |
| `tip` | string | — | Tooltip text (deprecated in favor of the tip directive, but still supported). |
| `icon` | string | per-type default | Material icon name shown in the field (e.g. `"person"`, `"calendar_today"`). |
| `showicon` | boolean | `true` | Whether to show the icon. |
| `size` | number | type default (usually 15) | Width in size-units (§1.5). |
| `grow` | number | — | flex-grow when no `size` set. |
| `editable` | boolean | `true` | Force-readonly when `false`. |
| `mandatory` | boolean | `false` | Force-mandatory when `true` (or when object validation says so). |
| `alert` | boolean | `false` | Highlight the field in an alert state. |
| `color` | string / color obj | — | Field color; accepts a plain color or a color-config object (§4.4). |
| `updatescript` | string (JS body) | — | Script run when the committed value changes; args `(previousvalue, value)`. |

**Value resolution order:** `attribute` (on the linked object) → `variable` (global) → `value` (static).

> Note: `listbutton`, `text`, `progress`, and `selector` re-declare some of these aliases with component-specific behavior; see their entries in §3.

### 2.6 `RbFieldInputComponent` — inline-editable fields

Adds one input:

| Input | Type | Default | Description |
|---|---|---|---|
| `margin` | boolean | `true` | Vertical spacing around the field. |

Used by: `input`, `numberinput`, `textarea`, `richtext`, `urlinput`, `currencyinput`, `durationinput`, `percentinput`, `tagsinput`, `inlineinput`, `search`.

### 2.7 `RbPopupInputComponent` — popup-picker fields

Adds one input on top of §2.5/§2.6:

| Input | Type | Default | Description |
|---|---|---|---|
| `filter` | filter object | — | Filter for the list shown in the picker popup (§4.1). |

Used by: `relatedinput`, `multirelatedinput`, `hierarchyinput`, `choiceinput`, `addressinput`, `datepicker`.

> Note: `search` also declares a `filter` input, but there it is the **default filter config of the search mode**, not a popup filter.

### 2.8 `RbDataButtonComponent` — buttons

| Input | Type | Default | Description |
|---|---|---|---|
| `label` | string | — | Button text. |
| `icon` | string | — | Material icon shown on the button. |
| `mode` | string | `"roundedbox"` | Visual mode of the button. |
| `enabled` | boolean | `true` | Whether the button can be clicked. |
| `focus` | boolean | `false` | Give initial keyboard focus. |
| `margin` | boolean | `true` | Spacing around the button. |
| `hover` | string | — | Color for hover state. |

Used by: `button`, `listbutton`, `actiongroup`, `processactionsbutton`.

### 2.9 `RbDataCalcComponent` — computed datasets (gantt, calendar, timeline, funnel, map)

| Input | Type | Default | Description |
|---|---|---|---|
| `series` | array of series configs | — | One entry per data series (§4 for per-component fields). Each item has at least `dataset` — the **`id`** of a member dataset when linked to a `datasetgroup`, otherwise the dataset's `object` name. |
| `dofilter` | boolean | `true` | When `true`, the component applies each series' filter/sort to its target dataset before drawing. |

### 2.10 `RbAggregateDisplayComponent` — aggregate graphs

Base of `graph`, `stackedgraph`, `vbargraph`, `hbargraph`, `numbertiles`, `pivot`. Declares its own data-linking inputs (`dataset`, `aggregateset`, `virtualselector`, `datasetevents` — context-injected like §2.2) plus:

| Input | Type | Default | Description |
|---|---|---|---|
| `series` | object | — | X-series spec: `{ label, dimension, labelattribute, labelformat, sortby, sortdir, top }`. `dimension` is the aggregate dimension used as series keys; `labelattribute` the dimension value used for display; `sortby` `"name"` sorts by label, otherwise by value; `top` keeps only the top N. |
| `categories` | object | — | Same shape as `series`. When present, the display becomes 2-D (categories × series). |
| `value` | object | — | Y-value spec: `{ name, label, format, convert }`. `name` is the metric to read; `format` a display format string (§4.5); `convert` set to `mstohour` converts milliseconds to hours for display. |
| `target` | object | — | Target metric: `{ name }` (used by value/target graph types). |
| `min` / `max` | number | — | Value bounds. |
| `grow` / `width` | number | flex | Sizing (size-units). |
| `height` | number | — | Height in size-units. |
| `showrefresh` | boolean | `false` | Show a refresh button on hover. |
| `colormap` | object | — | Map of value → color, applied to dimension values. |
| `linkview` | string | — | View opened when a data point is clicked. |
| `linkfilter` | string (filter) | — | Filter (with `cat`/`code` context, §4.3) applied to navigation on click; defaults to a filter on the dimension value. |
| `title` | string | — | Optional title displayed in the graph area. |
| `script` | string (JS body) | — | Click-handling script; runs instead of navigation. |
| `scriptparam` | any | — | Argument passed to `script` (resolved against the click context). |

---

## 3. Component reference

Grouped by purpose. "Own inputs" = inputs declared by that component (JSON aliases). Inherited inputs are not repeated — see the §2 section referenced.

### 3.1 Data sources

Data sources are containers: they load data from the server, maintain the selection, and provide the `dataset`/`aggregateset`/`fileset` context to everything nested inside them.

#### `dataset` — `RbDatasetComponent`

Loads a page of objects of one type, driven by a filter, keeping a selection. The workhorse of master–detail screens.

- **Inherits**: §2.1 + §2.2 + §2.3 + §2.4
- **Own inputs**:

| Input | Type | Default | Description |
|---|---|---|---|
| `basesort` | object | — | Sort applied when no user sort is set (same shape as sort in filters, §4.1). |
| `autoselect` | string | `"whensingle"` | `"whensingle"`: auto-select when the list has exactly one item; `"first"`: always select the first item. |
| `fetchall` | boolean | `false` | Fetch the entire list on init (page size 250) instead of paginated 50 (also disables adding related objects to fetched items). |
| `alwaysreceiveupdates` | boolean | `false` | Keep accepting live-updated objects even after the list reaches a full page. |
| `addrelated` | boolean | `true` | Request related objects with the fetched list (skipped when `fetchall`). |
| `addtoend` | boolean | `false` | New objects are appended to the end of the list instead of the front. |
| `muteevents` | string[] | `[]` | Dataset events that should not be emitted (see §2.2 for event names). |
| `eventscript` | string (JS body) | — | Script run for every dataset event; arg `event` = `{ event, dataset, object }`. |
| `name` | string | — | **Deprecated** identifier (pre-`id`); equivalent to `id` when `id` is absent. |
| `id` | string | — | *(re-emphasized)* give datasets an `id`; it is the key used by `datasetgroup` members, navigation `datatargets`, and `linktable`/search targeting. |

**Events**: `init`, `load`, `select`, `update`, `clear`, `removed`.

#### `datasetgroup` — `RbDatasetGroupComponent`

A container for multiple sibling `dataset` components that behave as one coordinated set (shared selection across members with the same object type, group-wide refresh and search). Members register under their `id` (or `name`/`object` as fallback).

- **Inherits**: §2.1 + §2.2 + §2.3 + §2.4 — *note: `object` applies to the group as a base object; in practice configure the child datasets.*
- **Own inputs**: none — configure via the child `dataset` components in its `content`.
- Children become available as `datasetgroup.datasets[<id>]`; data-calc components reference them by `series[0].dataset = <id>`.

#### `aggregateset` — `RbAggregatesetComponent`

Container that fetches **pre-aggregated** data (dimensions × metrics) instead of raw objects. Powers the aggregate graphs (§3.6).

- **Inherits**: §2.1 + §2.2 + §2.3 + §2.4
- **Own inputs**:

| Input | Type | Description |
|---|---|---|
| `tuple` | object/array | The dimension "tuple" to aggregate by (server-side aggregate definition). |
| `metrics` | object | The metrics to aggregate. |
| `base` | object | Base object config for the aggregation. |

Like `dataset`, honors `master` (via `master.relationship`) to scope aggregates to the parent's selection.

#### `fileset` — `RbFilesetComponent`

Container that manages the file collection of an object (uploads, deletion, selection).

- **Inherits**: §2.1 + §2.2 + §2.3 + §2.4
- **Own inputs**:

| Input | Type | Description |
|---|---|---|
| `editable` | string (expression) | Expression (`"true"`, `"false"`, or JS with `object`/`relatedObject`) controlling whether the user may modify the file set. |
| `overrideobject` | — | Override the object the fileset attaches to (setter-only input; advanced). |

### 3.2 Layout & structure

Pure structural components. All inherit §2.1 (+ §2.2/§2.3 as noted).

#### `layout` — `RbLayoutComponent`

A plain transparent container; arranges its `content` per the flex inputs (§2.3). Typically used for free-form positioning. No own inputs.

#### `hsection` — `RbHsectionComponent`

Horizontal section: children laid out in a row, with a standard section chrome. No own inputs (uses §2.3 layout inputs).

#### `vsection` — `RbVsectionComponent`

Vertical section: children laid out in a column. No own inputs.

#### `scroll` — `RbScrollComponent`

A scrollable container: children render in normal flow and the overflow scrolls. No own inputs.

#### `group` — `RbGroupComponent`

A labeled group container (fixed-size unless `grow`/`shrink` are set — defaults to `0`/`0`).

| Input | Type | Description |
|---|---|---|
| `label` | string | Caption of the group. |

#### `spacer` — `RbSpacerComponent`

Invisible filler. Inherits §2.1 only.

| Input | Type | Description |
|---|---|---|
| `width` | number | Width in size-units (0.88·N vw, c.17px each at 1920). |
| `height` | number | Height in size-units. If neither set, the spacer `grow`s to fill. |

#### `hseparator` / `vseparator` — `RbHseparatorComponent` / `RbVseparatorComponent`

Horizontal / vertical line separators. No own inputs.

#### `tile` — `RbTileComponent`

A titled card that wraps content and can show a reload affordance.

| Input | Type | Default | Description |
|---|---|---|---|
| `title` | string | `null` | Title bar text. |
| `showreload` | boolean | `false` | Show a reload button that refreshes the view's top datasets. |

#### `tabsection` — `RbTabSectionComponent`

Container that hosts `tab` children (the enclosing `tabsection` is injected into each tab).

| Input | Type | Default | Description |
|---|---|---|---|
| `keeplasttab` | boolean | `false` | Keep the last tab open even when closing. |
| `secondary` | boolean | `false` | Render as a secondary (sub-tab) section. |

#### `tab` — `RbTabComponent`

One tab inside a `tabsection`. Its `content` is only shown when the tab is active; the tab is the `activator` for its children.

| Input | Type | Description |
|---|---|---|
| `label` | string | Tab caption. |
| `isdefault` | boolean | If `true`, this tab is open by default. (default `false`) |
| `tabsection` | — | context-injected (§1.3). |

#### `vcollapse` / `hcollapse` — `RbVcollapseComponent` / `RbHcollapseComponent`

Vertical / horizontal collapsible sections; act as activators for their children (collapsed = children inactive).

| Input | Type | Default | Description |
|---|---|---|---|
| `label` | string | `null` | Caption shown in the collapse header. |
| `reverseicon` | boolean | `false` | *(vcollapse)* flip the expand/collapse icon. |
| `control` | string | `"separator"` | *(hcollapse)* variant of the expand control (`"separator"` or similar chrome variant). |
| `defaultopen` | boolean | `false` | *(hcollapse)* start expanded. |

#### `modal` — `RbModalComponent`

A named dialog rendered on top of the view. Registered on view build; opened by name via `modalService` / `action: "modal"` / `linktable` & `list4` `modal` inputs. Its `content` is active only while the modal is open.

| Input | Type | Description |
|---|---|---|
| `name` | string | Modal identifier (namespaced per view). Referenced from `link`, `list4`, `linktable`, actions, and navigation events (`modal`). |
| `title` | string | Dialog title. |
| `icon` | string | Material icon for the title bar. |
| `eventscript` | string (JS body) | Runs on open/close; arg `event` = `{ event: 'open' \| 'close' }`. |

#### `repeater` — `RbRepeaterComponent`

Builds its children **once per object** in a linked dataset. For each object in `dataset.list`, every item in `repeat` is instantiated with that object exposed as `virtualselector` (so `attribute` inputs inside resolve against the repeated object). Inherits §2.1 + §2.2 + §2.3.

| Input | Type | Description |
|---|---|---|
| `repeat` | array of component config objects | The sub-tree to build per object (same syntax as `content`). |
| `direction` | string | `"row"` (default) or `"col"` — flex direction of the produced items. |

### 3.3 Forms & field inputs

`form` is the container; the rest are field inputs. All field inputs inherit §2.5 (and §2.6/§2.7 where noted).

#### `form` — `RbFormComponent`

Container that lays out inputs as a form (label/value alignment). No own inputs — place inputs in its `content`.

#### `input` — `RbStringInputComponent`

Plain text input. No own inputs (inherits §2.5 + §2.6).

#### `numberinput` — `RbNumberInputComponent`

Numeric input. No own inputs.

#### `urlinput` — `RbUrlInputComponent`

Text input with URL handling/validation. No own inputs.

#### `textarea` — `RbTextareaInputComponent`

Multi-line text input.

| Input | Type | Description |
|---|---|---|
| `rows` | number | Number of visible text rows. |

#### `richtext` — `RbRichtextInputComponent`

Rich-text (HTML) editor input.

| Input | Type | Default | Description |
|---|---|---|---|
| `allowswitch` | boolean | `false` | Allow switching between rich-text and raw HTML editing. |
| `wraphtml` | boolean | `false` | Wrap the stored value in `<html>` when displaying. |

#### `switch` — `RbSwitchInputComponent`

Boolean toggle / checkbox.

| Input | Type | Default | Description |
|---|---|---|---|
| `margin` | boolean | `true` | Spacing. |
| `fieldheight` | boolean | `true` | Match the height of adjacent text fields (false renders a compact toggle). |
| `labelafter` | boolean | `false` | Render the label after the control instead of before. |
| `mode` | string | `"checkbox"` | Control variant (`"checkbox"` or toggle-style mode). |

#### `stars` — `RbStarsInputComponent`

Star rating input.

| Input | Type | Default | Description |
|---|---|---|---|
| `margin` | boolean | `true` | Spacing. |

#### `tagsinput` — `RbTagsInputComponent`

Multi-value tag editor. No own inputs beyond §2.5 + §2.6.

#### `inlineinput` — `RbInlineInputComponent`

Single-line text input that stays visually inline (no icon box). No own inputs.

#### `currencyinput` — `RbCurrencyInputComponent`

Currency input with locale formatting. Note the **camelCase** aliases.

| Input | Type | Default | Description |
|---|---|---|---|
| `decimalCount` | number | `2` | Decimal places. |
| `thousandsSeparator` | string | `","` | Thousands separator. |
| `decimalSeparator` | string | `"."` | Decimal separator. |

#### `percentinput` — `RbPercentInputComponent`

Percentage input.

| Input | Type | Default | Description |
|---|---|---|---|
| `capped` | boolean | `true` | Cap the value at 100 when editing. |

#### `durationinput` — `RbDurationInputComponent`

Duration input (stored as milliseconds; displayed compactly). No own inputs.

#### `choiceinput` — `RbChoiceInputComponent`

Pick from a list of options shown in a popup.

| Input | Type | Description |
|---|---|---|
| `choicelist` | object (map of key → label) | The option list to present. |
| `choicelistvariable` | string | Global variable holding the option list (alternative to `choicelist`). |

#### `codeinput` — `RbCodeInputComponent`

Code editor input (multi-line, syntax mode).

| Input | Type | Description |
|---|---|---|
| `rows` | number | Editor height in rows. |
| `mode` | string | Syntax highlighting mode. |

#### `datepicker` / `dateinput` — `RbDatetimeInputComponent`

Date / date-time picker (both types map to the same component).

| Input | Type | Default | Description |
|---|---|---|---|
| `datepart` | boolean | `true` | Include the date portion. |
| `timepart` | boolean | `true` | Include the time portion. |
| `format` | string | `"YYYY-MM-DD HH:mm"` | **Deprecated** — kept for compatibility. |

#### `colorinput` — `RbColorInputComponent`

Color picker.

| Input | Type | Default | Description |
|---|---|---|---|
| `margin` | boolean | `true` | Spacing. |

#### `relatedinput` — `RbRelatedInputComponent`

Picker for a **related object** (single). Inherits §2.5 + §2.6 + §2.7 (`filter`).

| Input | Type | Description |
|---|---|---|
| `displayattribute` | string | Attribute of the target objects to display as the option label. |
| `sortattribute` | string | Attribute to sort options by. |
| `parentattribute` | string | When filtering options against a parent object: the attribute on this object pointing to the parent. |
| `childattribute` | string | The attribute on the parent pointing to this object's type. |

#### `multirelatedinput` — `RbMultiRelatedInputComponent`

Picker for multiple related objects.

| Input | Type | Description |
|---|---|---|
| `displayattribute` | string | Display attribute for options. |
| `sortattribute` | string | Sort attribute for options. |
| `parentattribute` | string | Parent-link attribute on this object. |
| `childattribute` | string | Child-link attribute on the parent. |
| `tagmode` | boolean | Render selected values as editable tags (default `false`). |

#### `hierarchyinput` — `RbHierarchyInputComponent`

Tree picker for objects arranged in a hierarchy.

| Input | Type | Description |
|---|---|---|
| `displayattribute` | string | Attribute shown per tree node. |
| `parentattribute` | string | Attribute pointing to the node's parent. |
| `childattribute` | string | Attribute pointing to the node's children. |

#### `addressinput` — `RbAddressInputComponent`

Address picker with map/address popup.

| Input | Type | Description |
|---|---|---|
| `centerattribute` | string | Attribute on the selected object used to center the map view. |

#### `fileinput` — `RbFileInputComponent`

Attachment picker bound to the object's fileset. Inherits §2.5 (extends `RbInputComponent` directly — no `margin`).

| Input | Type | Description |
|---|---|---|
| `fileuidattribute` | string | Attribute storing the chosen file's `fileuid`. |
| `mimeattribute` | string | Attribute storing the file's MIME type. |
| `thumbnailattribute` | string | Attribute storing the file's thumbnail. |
| `width` | number | Display width in size-units. |
| `height` | number | Display height in size-units. |
| `shrinkborder` | boolean | Render a thinner border (`true` default). |
| `validextensions` | string[] | Allowed file extensions. |

#### `filedrop` — `RbFiledropComponent`

Drag-and-drop target for uploading files into a `fileset`. Inherits §2.1 + §2.2 + §2.3 (it is a container).

| Input | Type | Description |
|---|---|---|
| `fileset` | — | context-injected from an enclosing `fileset` (§1.3). |

Files dropped are uploaded to the fileset's object (honoring the fileset's `editable`).

#### `filelist` — `RbFilelistComponent`

Lists the files of a `fileset` with previews, details and a viewer. Inherits §2.1 **only** (no data-observer inputs).

| Input | Type | Default | Description |
|---|---|---|---|
| `fileset` | — | — | context-injected from an enclosing `fileset` (§1.3). |
| `downloadOnSelect` | boolean | `false` | Download the file on click instead of previewing. (Note the **camelCase** alias.) |
| `details` | boolean | `true` | Show file metadata/details. |

**Typical fileset block:**

```json
{ "type": "fileset", "object": "customer",
  "master": { "relationship": { "uid": "'obj.uid'" } },
  "content": [
    { "type": "filelist", "details": true },
    { "type": "filedrop" }
  ] }
```

### 3.4 Lists, tables & trees

All inherit §2.1 + §2.2 (data observers).

#### `list3` — `RbListComponent`

Compact object list (3-tier row: header + subhead + supporting text). Clicking a row selects the object in its dataset.

| Input | Type | Description |
|---|---|---|
| `headerattribute` | string | Attribute used for the main (bold) row text. |
| `subheadattribute` | string | Second-line text. |
| `supptextattribute` | string | Small supporting text. |
| `sidetextattribute` | string | Text rendered on the side of each row. |
| `iconattribute` | string | Icon name read from each object. |
| `colorattribute` | string | Color value read from each object. |
| `iconmap` | object | Map of `<iconattribute> value → icon name`. |
| `colormap` | object | Map of `<colorattribute> value → color`. |

#### `list4` — `RbList4Component`

The rich list: configurable main/sub/meta lines, colors, images, refresh, drag & click hooks.

| Input | Type | Default | Description |
|---|---|---|---|
| `mainattribute` | string | — | Attribute for the main line (or use `mainexpression`). |
| `mainexpression` | string | — | JS expression for the main line (§4.2). |
| `mainformat` | string | — | Display format for the main value (§4.5). |
| `maincolor` | string | — | Color/expression for the main line. |
| `subattribute` / `subexpression` / `subformat` / `subcolor` | — | — | Second line, same pattern. |
| `meta1attribute` / `meta1expression` / `meta1format` / `meta1color` | — | — | Small meta line 1. |
| `meta2attribute` / `meta2expression` / `meta2format` / `meta2color` | — | — | Small meta line 2. |
| `imageattribute` | string | — | File-attachment attribute shown as row thumbnail. |
| `color` | string | — | Row color (plain color or expression). |
| `colormap` | object | — | Map applied to `colorattribute` values. |
| `colorattribute` | string | — | Attribute supplying per-row color. |
| `modal` | string | — | Modal name: item click opens this modal instead of just selecting. |
| `navigate` | string | — | When present, item click navigates to the object's view (by object type, with a filter on the object's uid) instead of only selecting. |
| `clickscript` | string (JS body) | — | Run on click; arg `event` = `{ alreadyselected, object, dataset }`. |
| `allowdrag` | boolean | `false` | Rows can be dragged (to gantt lanes / linktable). |
| `showrefresh` | boolean | `true` | Show a refresh icon. |
| `emptytext` | string | `"No records"` | Text shown when the list is empty. |

#### `tree` — `RbTreeComponent`

Hierarchical object list.

| Input | Type | Description |
|---|---|---|
| `displayattribute` | string | Attribute shown per node. |
| `mainattribute` | string | Primary attribute per node (label). |
| `subattribute` | string | Secondary attribute per node. |
| `meta1attribute` | string | Meta line 1. |
| `meta2attribute` | string | Meta line 2. |
| `parentattribute` | string | Attribute pointing to the node's parent. |
| `childattribute` | string | Attribute pointing to the node's children. |

#### `table` — `RbTableComponent`

Editable inline table of the selected object's collection (each row = a related object of the dataset's selected object). Columns are editable inline by default.

| Input | Type | Default | Description |
|---|---|---|---|
| `columns` | array of column configs | **required** | See §4.10 for the column object shape. |
| `headersonemtpy` | boolean | `true` | Show column headers even when the table is empty (alias is a typo in the source — keep it as-is). |
| `candeleterows` | boolean | `true` | Show the row-delete control. |
| `emptymessage` | string | `null` | Message shown when there are no rows. |
| `orderattribute` | string | — | Attribute that orders rows and enables drag re-ordering. |

#### `linktable` — `RbLinktableComponent`

Read-only multi-row display of objects (often a dataset of related objects); rows can be selected (single- or multi-select depending on the dataset).

| Input | Type | Default | Description |
|---|---|---|---|
| `columns` | array of column configs | **required** | Same shape as `table` columns (§4.10) minus editing. |
| `group` | object | — | Grouping config for the rows. |
| `view` | string | — | View to open when a row is clicked. |
| `grid` | boolean | `false` | Render in table-grid style. |
| `flexfill` | boolean | `true` | Fill available flex space. |

#### `log` — `RbLogComponent`

Activity log / comment list: timestamped entries with author, optional categories and grouping, optionally editable.

| Input | Type | Description |
|---|---|---|
| `size` | number | Entry height in size-units. |
| `userattribute` | string | Attribute holding the author (username). |
| `dateattribute` | string | Attribute holding the entry timestamp. |
| `entryattribute` | string | Attribute holding the entry text. |
| `categoryattribute` | string | Attribute holding the entry category. |
| `groupattribute` | string | Attribute used to group entries. |
| `editable` | any | Whether new entries can be added/edited. |
| `linkobjectattribute` | string | Attribute on the entry referencing a linked object (opens on click). |
| `linkuidattribute` | string | Attribute on the entry holding the linked object's uid. |

### 3.5 Buttons & action controls

All inherit §2.1 + §2.2 + §2.8.

#### `button` — `RbActionButtonComponent`

A button that executes a single **action** (§4.6) against the linked dataset.

| Input | Type | Description |
|---|---|---|
| `action` | string | Built-in action name or an object function name (§4.6). |
| `target` | string | Action target (e.g. report name, modal name, view for `navigate`, object function name for `execute`). |
| `param` | object | Parameters passed to the action (a filter-ish object; expressions are resolved against the selected object). |
| `confirm` | string | When set, show a confirmation dialog with this text before executing. |
| `timeout` | number | Timeout (ms) for `execute*` actions. |

#### `listbutton` — `RbActionlistComponent`

A button that lists and executes **several** actions defined on a related list object. Inherits §2.1 + §2.2 + §2.8 + the 5 `button` inputs (`action`, `target`, `param`, `confirm`, `timeout` — note: those are inherited/available but the list's own actions come from its data):

| Input | Type | Description |
|---|---|---|
| `object` | string | The object type whose action definitions are listed. *(Shadows the data-observer `object` input — here it is a string type name.)* |
| `displayattribute` | string | Attribute used as each listed item's label. |
| `basefilter` | filter object | Filter applied to the list of available actions/items. |

#### `actiongroup` — `RbActiongroupComponent`

A cluster of buttons (or a menu) built from a JSON list of actions; each item is gated by the `show` expression.

| Input | Type | Default | Description |
|---|---|---|---|
| `actions` | array of action objects | **required** | Each item: `{ action, target, param, timeout, label, icon, confirm, show }` — `show` is a JS expression (or `null`/`true`) evaluated against the selected/related object (§4.2). See §4.6. |
| `menucategory` | string | — | Category key that renders the items as a dropdown menu. |
| `domaincategory` | string | — | **Deprecated.** |
| `showprocessinteraction` | boolean | `false` | Show interaction states (e.g. process step indicators). |
| `script` | string (JS body) | — | Alternate click handling script. |
| `round` | boolean | `false` | Rounded visual variant. |
| `hideonempty` | boolean | `false` | Hide the whole group when no action is shown. |

#### `processactionsbutton` — `RbProcessactionsComponent`

Button that surfaces the **process actions** currently available on the selected object (from the process engine), executed the same way as `button` actions.

| Input | Type | Default | Description |
|---|---|---|---|
| `round` | boolean | `false` | Rounded visual variant. |
| `hideonempty` | boolean | `false` | Hide when the object has no process actions. |

#### `search` — `RbSearchComponent`

Search bar that filters/sorts the linked dataset or datasetgroup (or a named search mode). Inherits §2.5 + §2.6 (its `filter`/`sort` have search-specific meaning — see §4.9 for mode objects).

| Input | Type | Default | Description |
|---|---|---|---|
| `filter` | filter object | — | Initial/default filter of the (single implicit) search mode. |
| `sort` | object | — | Initial/default sort of the implicit mode. |
| `showsearchfield` | boolean | `true` | Show the free-text search box. |
| `searchtarget` | — | — | **Not settable from JSON** — runtime-only target (use `targetdatasetid`/modes instead). |
| `modes` | array of mode objects | — | Named search modes, e.g. filter-builder combinations (§4.9). When absent, one implicit mode from `filter`/`sort`/`targetdatasetid` is created. |

### 3.6 Graphs & visualizations

Data-calc components inherit §2.1 + §2.2 + §2.9 (`series`, `dofilter`); aggregate displays inherit §2.10.

#### `graph` — `RbDynamicGraphComponent`

Dynamic aggregate graph.

| Input | Type | Description |
|---|---|---|
| `graphtype` | string | Chart variant of the generic dynamic graph. |

Uses `series`/`categories`/`value`/`target` from §2.10.

#### `stackedgraph` — `RbStackedGraphComponent`

Stacked aggregate graph.

| Input | Type | Default | Description |
|---|---|---|---|
| `legendposition` | string | `"right"` | Legend placement. |
| `verticalxlabels` | boolean | `false` | Rotate x-axis labels vertically. |
| `valuetargetlegend` | object | — | Legend config for value vs target series. |
| `codeorder` | string | — | Ordering key for series codes. |

#### `vbargraph` — `RbVbarGraphComponent`

Vertical bar aggregate graph.

| Input | Type | Default | Description |
|---|---|---|---|
| `legendposition` | string | `"right"` | Legend placement. |
| `verticalxlabels` | boolean | `false` | Rotate x-axis labels vertically. |
| `valuetargetlegend` | object | — | Value/target legend config. |
| `singlecolor` | string | — | Use one color for all bars (`null` = default palette). |

#### `hbargraph` — `RbHbarGraphComponent`

Horizontal bar aggregate graph.

| Input | Type | Default | Description |
|---|---|---|---|
| `legendposition` | string | `"right"` | Legend placement. |
| `valueonbar` | boolean | `false` | Print values on the bars. |
| `valuetargetlegend` | object | — | Value/target legend config. |
| `singlecolor` | string | — | Single color for all bars (`null` = default palette). |

#### `numbertiles` — `RbNumberTilesComponent`

Grid of number tiles (KPI tiles) from aggregates.

| Input | Type | Default | Description |
|---|---|---|---|
| `rows` | number | `1` | Number of tile rows. |
| `cols` | number | `1` | Number of tile columns. |
| `format` | string | — | Display format (§4.5). |
| `valuecolorrange` | object | — | Color range applied to values. |
| `fullcolor` | boolean | `false` | Fill tiles fully with the value color. |

#### `pivot` — `RbPivotTableComponent`

Pivot table over aggregates.

| Input | Type | Default | Description |
|---|---|---|---|
| `format` | string | — | Display format for cell values. |
| `showsum` | boolean | `true` | Show sum row/column. |
| `fontsize` | number | `1` | Font size in size-units. |

#### `calendar` — `RbCalendarComponent`

Calendar with event layers. Series items (`CalendarSeriesConfig`): `{ dataset, dateattribute, durationattribute, labelattribute, colorattribute, colormap, color, icon, linkattribute, linkview, modal }`.

| Input | Type | Description |
|---|---|---|
| `layers` | array of `{ label, datasets }` | Layer switcher: each layer is a named set of dataset ids to show. |
| `filter` | filter object | Base filter config for the calendar's datasets. |

#### `gantt` — `RbGanttComponent`

Gantt chart: lanes from one dataset, spreads (task bars) from series, drop-to-move editing enabled. Full config in §4.11.

| Input | Type | Default | Description |
|---|---|---|---|
| `lanes` | object | — | Lane config (§4.11). |
| `overlays` | array of objects | `[]` | Overlay lane configs (§4.11). |
| `layers` | array | `[]` | Layer configs for lane groupings. |
| `toolbar` | array of component configs | — | Components built in the gantt toolbar (regular config objects, see example §5.2). |
| `showcontrols` | boolean | `true` | Show the zoom/pan/now controls. |
| `locktonow` | boolean | `false` | Lock the time window around "now". |
| `allowpastdrop` | boolean | `true` | Allow dropping spreads in the past. |
| `allowoverlapgroup` | boolean | `false` | Allow overlapping spreads in a group lane. |
| `allowmultipleoverlays` | boolean | `false` | Show more than one overlay over the lanes at the same time: the first overlay picked is drawn as a filled band, every further one in the `overlaystyle`. Off, picking an overlay replaces the one shown. |
| `overlaystyle` | string | `hatch` | How the overlays after the first are drawn when `allowmultipleoverlays` is on: `hatch` (thin lines in the overlay's colour, at a different angle per position) or `outline` (only the left and right edge of each block, in its colour). |
| `snapinterval` | number | `null` | Snap dragged spreads to this interval (ms). |
| `headerwidth` | number | `17` | Size of the lane header area in size-units. |
| `startvariable` | string | — | Global variable driving the time window start. |
| `spanvariable` | string | — | Global variable driving the time span. |
| `zoomvariable` | string | — | Global variable driving the zoom. |
| `emptymessage` | string | — | Message when nothing to show. |

**`series` items** (`GanttSeriesConfig`): `{ dataset, id, laneattribute(s)/laneforeignattribute(s), startattribute|start, durationattribute|duration, endattribute|end, labelattribute, labelexpression, labelalts, centerlabel, labelcolor, color|colorattribute|colormap|colorexpression, selectedbordercolor, indicatorattribute, indicatorexpression, dependencyattribute, groupof, groupattribute, isbackground, canedit, ghost, modal, link, clickscript, applylanefilter, applydatefilter, show }` — time parts accept either an attribute name (`startattribute`) or a value/attribute/expression object (§4.4 VAE shape). `modal` opens a modal on click; `link` is a LinkConfig (§4.3); `show` is an expression over `(dataset, relatedObject)`.

**Control data**: `componentControl` accepts `{ start, span, zoom, hideemptylanes, hideemptybackgrounds }` (via `initialcontrols`/comptargets).

#### `timeline` — `RbTimelineComponent`

Timeline of dated events across datasets.

| Input | Type | Default | Description |
|---|---|---|---|
| `reverse` | boolean | `false` | Reverse chronological direction. |
| `grow` | number | — | Flex grow. |
| `datefocus` | boolean | `false` | Focus the timeline on the selected date. |
| `wide` | boolean | `false` | Wide layout variant. |
| `showmorelevel` | number | `1` | "show more" grouping level. |

**`series` items** (`TimelineSeriesConfig`): `{ dataset, mainattribute, mainexpression, subattribute, subexpression, dateattribute, icon, level, link, modal }` — `link` is a LinkConfig (§4.3), `modal` a modal name.

#### `funnel` — `RbFunnelComponent`

Funnel visualization grouped into phases.

| Input | Type | Default | Description |
|---|---|---|---|
| `groups` | array of `{ key, label, order, open }` | — | Funnel groups. |
| `phases` | array of `{ dataset, keyattribute, labelattribute, orderattribute, groupattribute }` | — | Phase definitions. |
| `phasegroups` | array of `{ dataset, keyattribute, labelattribute, orderattribute }` | — | Grouping of phases. |
| `emptymessage` | string | `"Nothing to show"` | Message when empty. |

**`series` items** (`FunnelSeriesConfig`): `{ dataset, labelattribute, sublabelattribute, colorattribute, colormap, groupattribute | group {value|attribute|expression}, phaseattribute, linkview, modal }`.

#### `map` — `RbMapComponent`

Geographic map of objects.

| Input | Type | Description |
|---|---|---|
| `geoattribute` | string | Attribute with the object's geometry (used when no `series` is given). |
| `labelattribute` | string | Label attribute (no-series mode). |
| `descriptionattribute` | string | Description/popup attribute. |
| `dateattribute` | string | Date attribute — enables trail/animated mode. |

**`series` items** (`MapSeriesConfig`): `{ dataset, geometryattribute, labelattribute, initialsattribute, iconattribute, icon, iconmap, colorattribute, colormap, animateattribute, animatemap, visibleattribute, linkattribute, dateattribute }`.

### 3.7 Misc display & dynamic forms

#### `text` — `RbTextComponent`

Displays a value as text. Re-declares the binding inputs (§2.5-style) plus `expression`:

| Input | Type | Description |
|---|---|---|
| `attribute` | string | Attribute of the selected object. |
| `object` | — | Explicit object reference (shadows data-observer `object`). |
| `value` | any | Static value (default `null`). |
| `variable` | string | Global variable name. |
| `expression` | string | JS expression evaluated against the object (§4.2). |
| `size` | number | Text size in size-units. |
| `margin` | boolean | Spacing (`true` default). |
| `alert` | boolean | Alert highlight (default `false`). |
| `icon` | string | Leading icon. |
| `color` | string | Text color. |

#### `progress` — `RbProgressComponent`

Progress indicator with label.

| Input | Type | Default | Description |
|---|---|---|---|
| `attribute` | string | — | Attribute holding the progress value (0..1 or 0..100). |
| `object` | — | — | Explicit object. |
| `value` | any | `null` | Static value. |
| `variable` | string | — | Global variable. |
| `label` | string | `"Progress"` | Label next to the bar. |
| `icon` | string | `"progress_activity"` | Icon. |
| `size` | number | — | Size in size-units. |
| `grow` | number | — | Flex grow. |
| `margin` | boolean | `true` | Spacing. |

#### `image` — `RbImageComponent`

Static image. **Inherits no base inputs** (no `id`, no data-linking).

| Input | Type | Description |
|---|---|---|
| `name` | string | Image file name, resolved to `/rbui/img/<name>`. |
| `size` | number | Width in size-units (width = 0.88·N vw). |

#### `icon` — `RbIconComponent`

A Material icon. **Inherits no base inputs.**

| Input | Type | Description |
|---|---|---|
| `icon` | string | Material icon name (e.g. `"home"`). |
| `color` | string | Icon color. |

#### `link` — `RbLinkComponent`

Clickable link (text/icon) that navigates (§4.3 LinkConfig semantics; here simplified):

| Input | Type | Default | Description |
|---|---|---|---|
| `attribute` | string | — | Related-object attribute: navigation targets the related object instead of the current one. |
| `datatargets` | array | — | Explicit LinkConfig-style datatargets array (see §4.3). |
| `view` | string | — | View to open. |
| `modal` | string | — | Modal to open instead of/view with. |
| `margin` | boolean | `true` | Spacing. |
| `filtersingleobject` | boolean | `true` | `true`: filter the target dataset to the single related object; `false`: only select it. |

#### `selector` — `RbSelectorComponent`

A dropdown that selects an object in the linked dataset (picker bound to the data, distinct from `choiceinput`).

| Input | Type | Default | Description |
|---|---|---|---|
| `label` | string | — | Label. |
| `displayattribute` | string | — | Attribute to show per option. |
| `displayexpression` | string | — | Expression (alternative to `displayattribute`) per option. |
| `tip` | string | — | Tooltip. |
| `icon` | string | — | Icon. |
| `showicon` | boolean | `true` | Show icon. |
| `size` | number | — | Width in size-units. |
| `grow` | number | — | Flex grow. |
| `margin` | boolean | `true` | Spacing. |

#### `dynamicform` — `RbDynamicformComponent`

A form whose **fields are data**: each object in a linked dataset (of a form-field object type) becomes one field. The mapping attributes below point at the fields' attributes:

| Input | Type | Description |
|---|---|---|
| `valueattribute` | string | Attribute holding the field value (or `null`/`uid` conventions). |
| `typeattribute` | string | Attribute holding the field type. Valid types: `string`, `textarea`, `choice`, `files`, `checkbox`, `signature`, `number`, `date`, `time`, `phone`, `address`, `email`, `currency`, `url`, `photos`, `videos`, `infoonly` (anything else renders as `unknown` → `string`). |
| `optionsattribute` | string | Attribute holding the options for `choice` fields. |
| `titleattribute` | string | Field title attribute. |
| `detailattribute` | string | Field detail/subtitle attribute. |
| `labelattribute` | string | Label attribute. |
| `orderattribute` | string | Sort field by this attribute. |
| `categoryattribute` | string | Categories group fields; shown when the category changes. |
| `categoryorderattribute` | string | Sort order of categories. |
| `dependencyattribute` | string | Hide the field unless the related object at this attribute satisfies the dependency. |
| `dependencyoperatorattribute` | string | Dependency operator attribute (default `eq`). |
| `dependencyvalueattribute` | string | Dependency expected value attribute. |
| `editable` | string | `"true"`, `"false"`, or a JS expression (arg context `relatedObject` etc.) making the whole form read-only conditionally. |

#### `dynamicformeditor` — `RbDynamicformeditorComponent`

Editor screen for the data behind a `dynamicform` (edit the field definitions themselves). Inherits §2.1 + §2.2 + §2.9.

| Input | Type | Description |
|---|---|---|
| `items` | array | The field-definition objects to edit (a linked datasetgroup's datasets provide them). |
| `categories` | array | Category definitions. |

---

## 4. Payload & expression formats

### 4.1 Filters

Filters are JSON objects mapping attribute names to values, with optional operators:

```json
{ "status": "open",
  "priority": { "$in": [1, 2] },
  "amount":   { "$gt": 100, "$lt": 5000 },
  "name":     { "$regex": "/^ACME/i" },
  "$or": [ { "type": "a" }, { "type": "b" } ] }
```

Supported operators (per `filter.service.ts`):

| Operator | Meaning |
|---|---|
| (plain value) | equality (`$eq`) |
| `$eq` | equal |
| `$ne` | not equal |
| `$in` | in list |
| `$nin` | not in list |
| `$gt` | greater than (numeric / ISO-date aware) |
| `$lt` | less than |
| `$regex` | regex as a string (evaluated as a JS regex literal) |
| `$or` | array of sub-filters, any must match |
| `$and` | array of sub-filters, all must match |

**Expression values**: any *string* value inside a filter (or sort/select objects) is treated as a **JS expression** and resolved at load time against these variables: `obj` / `object` / `selectedObject` (the related object), `dataset`, `relatedObject`, `relatedDataset`, `datasetgroup`, `userpref`, `uid` (the related object's uid), plus **each attribute of the related object as its own variable** (e.g. `"customer"` resolves if the related object has a `customer` attribute). Quoted literal strings are kept as-is. Example:

```json
"basefilter": { "owner": "obj.uid", "department": "'sales'" }
```

Sort objects use the same syntax, e.g. `"{ 'order': 1 }"` or an attribute reference.

### 4.2 Expressions

String expressions appear in: `show`, `mainexpression` / `subexpression` / `meta*expression`, `expression` (text), `updatescript`, `eventscript`, `clickscript`, list/color expressions, `master.relationship` filter values, `dynamicform.editable`, action `param` values, and graph/click `script`.

- **Visibility/field expressions** (`show`, `expression`, list line expressions): evaluated as JS with `object`, `relatedObject`, `dataset` in scope; the `show` string is URI-decoded first. Must yield a truthy/falsy value.
- **Callback bodies** (`updatescript`, `eventscript`, `clickscript`, `script`): JS function **bodies** executed with documented args (e.g. `updatescript(prev, val)`; `eventscript(event)` where `event = {event, dataset, object}`; `clickscript(event)` where `event = {alreadyselected, object, dataset}` or the click data for graphs).
- Expressions must reference only the documented context variables and global helpers available on `window.redback` / `window` (e.g. `Formatter.format(...)`, `ColorTool.*`).

### 4.3 Link / navigation config (LinkConfig)

Used by `link`-style clicks, `navigate` actions, `gantt` series `link`, `timeline` series `link`, and graph click navigation. JSON shape:

```json
{
  "target": "default",            // target view-loader name (optional)
  "view": "myview",               // view to open
  "objectname": "task",           // used when view is omitted (lookup view by object type)
  "tab": "overview",              // tab to open in the target view
  "modal": "details",             // modal to open
  "reset": false,                 // reset the back-stack
  "datatargets": [
    { "datasetid": "tasks",         // or "objectname": "task"
      "attribute": "owner",          // uid taken from the clicked object's related object at this attribute (default: the object itself)
      "filter": { "$or": [...] },    // optional filter (expression strings, §4.1)
      "filtersingleobject": true,    // true → filter {uid}; false → select {uid} (default true when neither filter nor select given)
      "sort": { ... }, "select": { ... } }
  ],
  "comptargets": [ { "compid": "mygantt", "data": { "start": "object.startDate" } } ]
}
```

Deprecated notation: top-level `attribute` / `objectname` / `filter` / `filtersingleobject` / `select` next to `view` behave as a single datatarget.

### 4.4 Colors (ColorConfig)

Color inputs that accept an **object** use the VAE + map shape:

```json
{ "value": "#ff0000",            // literal color
  "attribute": "color",          // or attribute of the object
  "expression": "object.color ? object.color : '#888'",  // or expression
  "map": { "red": "#c00", "blue": "#00c" }   // optional value→color map applied last
}
```

Resolution precedence: `value` → `attribute` → `expression`, then `map[value]` if present. Plain strings are treated as literal colors.

### 4.5 Value formats

The `format` inputs (`mainformat`, `format`, `value.format`, column `format`) accept:

| Format | Result |
|---|---|
| `"date"` | `2026-09-17` (or user date-format preference) |
| `"time"` | `14:05` |
| `"datetime"` | date + time |
| `"duration"` | `2h 5m` from milliseconds |
| `"currency"` | `$1,234.56` (USD, en-US) |
| `"integer"` | rounded integer |
| `"decimal(3)"` | fixed decimal places (N = number of decimals) |

Anything else is displayed as-is.

### 4.6 Actions

The `action` value on `button` / `actiongroup` items selects the behavior (case-insensitive), with `target` and `param` (a filter-ish object whose string entries are expressions, §4.1):

| Action | Target | Param | Behavior |
|---|---|---|---|
| `create` | — | creation attributes | Create a new object in the dataset (pre-filled from param + current filter), select it. |
| `createinmemory` | — | creation attributes | Create the object in memory only (no server push yet). |
| `delete` | — | — | Delete the selected object (confirm dialog built in). |
| `exportall` | — | — | Export the current dataset list. |
| `report` | report name | report params | Launch a report for the selected object (or given param). |
| `reportall` | report name | — | Launch a report over the whole filtered dataset. |
| `reportlist` | category | param | Open the report picker for a category. |
| `execute` | object function name | args | Call the object function **on the selected object**. |
| `executeall` | function | args | Call the function on every object in the dataset. |
| `executemaster` | function | args | Call the function on the dataset's related (master) object. |
| `executeglobal` | function | args | Call a global server function. |
| `clientscript` | — | script body | Run JS client-side with args (`dataset`, `obj`, `object`, `selectedObject`, `relatedObject`). |
| `modal` | modal name | — | Open a modal. |
| `navigate` | — (not used) | LinkConfig (§4.3) | Navigate to another view/tab/modal, scoping its datasets via the LinkConfig. |
| `externallink` | expression | — | Open an external URL (expression on the selected object). |
| `refresh` | — | — | Refresh the dataset/datasetgroup. |
| *(anything else)* | — | — | Treated as an object function name on the selected object (`execute`). |

`confirm` (string) wraps any action in a confirmation dialog; `timeout` (ms) applies to `execute*`.

### 4.7 Remote control: initialcontrols & comptargets

Components with an `id` can be pushed data to after load:

- **`initialcontrols`** (on the component config): delivered to the component's `componentControl(data)` once it initializes.
- **`comptargets`** (on a navigation event / link): `[{ "compid": "<id>", "data": {...} }]` — data pushed to the matching component on navigation.

Examples: gantt accepts `{ start, span, zoom, hideemptylanes, hideemptybackgrounds }`.

### 4.8 Modals

`modal` components (in any view's `content`) register under their `name` (namespaced by the view they're declared in). Opened via: `action: "modal"` + `target`, a LinkConfig `modal` key, `list4.modal`, or navigation events. A modal's `content` is only active while the modal is open.

### 4.9 Search modes

`search.modes` — array of mode objects:

```json
"modes": [
  { "label": "By customer", "filter": { "customer": "'obj.uid'" }, "sort": { "name": 1 }, "targetdatasetid": "orders" },
  { "label": "Builder",     "filter": null }
]
```

`targetdatasetid` selects which dataset of the enclosing `datasetgroup` the mode acts on (default: the enclosing `dataset`). When `modes` is absent, a single implicit mode is built from `filter`, `sort`, and `targetdatasetid`.

### 4.10 Table columns

Items of `table.columns` / `linktable.columns`:

| Key | Type | Description |
|---|---|---|
| `label` | string | Column header. |
| `attribute` | string | Attribute of each row's object (or of the selected object + `displayattribute` for related columns). |
| `type` | string | Cell editor/input type (matches input types, e.g. `"input"`, `"numberinput"`, `"switch"`); omit for read-only display in `linktable`. |
| `displayattribute` | string | For related-object columns: attribute of the related object to display. |
| `parentattribute` | string | Related column: attribute on the row object pointing to the related object. |
| `childattribute` | string | Related column: reverse attribute on the related object. |
| `format` | string | Display format (§4.5). |
| `icon` | string | Icon shown in the cell. |
| `size` | number | Column width in size-units (default 17). |
| `editable` | boolean | Allow inline editing (default `true`). |
| `linkview` | string | View opened on cell click. |
| `modal` | string | Modal opened on cell click. |
| `show` | expression | Show/hide this column (default `"true"`). |
| `filter` | filter | Filter applied to related-object columns. |
| `alt` | object of column configs | Alternate column definitions keyed by name (user-switchable variants). |

### 4.11 Gantt config

**`lanes`** (`GanttLaneConfig`) — where task bars attach:

| Key | Description |
|---|---|
| `dataset` | dataset id to build lanes from (in the enclosing datasetgroup). |
| `linkattributes` | attributes joining series objects to lanes (default `["uid"]`). |
| `labelattribute` / `labelexpression` | lane label (expression: §4.2). |
| `subattribute` | sub-label. |
| `imageattribute` / `iconattribute` / `iconmap` | lane thumbnail / icon. |
| `orderattribute` | lane ordering. |
| `modal` | modal opened on lane click. |
| `link` | LinkConfig for lane click (§4.3). |
| `dragfilter` | filter restricting which dropped objects may attach to this lane. |
| `editable` | allow drops onto the lane (default `false`). |

**`overlays`** items (`GanttOverlayConfig`): `{ dataset, label, labelattribute, color|colorattribute|colormap|colorexpression, startattribute|start, durationattribute|duration, endattribute|end, applydatefilter, defaulton }` — read-only marker lanes. `defaulton: true` shows the overlay over the lanes from the start, as if its label had been clicked; without `allowmultipleoverlays` only the first overlay with it counts.

**`series`** items — see the `GanttSeriesConfig` list in §3.6 (`gantt` entry): time parts (`startattribute`/`durationattribute`/`endattribute` or VAE objects), lane linkage (`laneattribute(s)`/`laneforeignattribute(s)`), labels, colors (`color`/`colorattribute`/`colormap`/`colorexpression`), indicators, dependencies (`dependencyattribute` → SS/FS/SF/DU arrows), grouping (`groupof`/`groupattribute`), background layers (`isbackground`), editing (`canedit`), `modal`, `link`, `clickscript`, visibility `show` expression.

Drag & drop: spreads of series with `canedit` can be dragged along time; `allowpastdrop`, `snapinterval`, `allowoverlapgroup` (gantt) and `dragfilter`/`editable` (lanes) shape the behavior.

---

## 5. Worked examples

### 5.1 Simple two-panel screen (list + detail)

```json
{
  "label": "Customers",
  "content": [
    {  // ── left panel: dataset + list (list lives INSIDE the dataset,
          // so it receives the `dataset` context) ──────────────────────
      "type": "hsection",
      "grow": 1, "shrink": 1,
      "content": [
        { "type": "dataset",
          "object": "customer", "id": "customers",
          "basesort": { "name": 1 },
          "autoselect": "whensingle",
          "content": [
            { "type": "list4",
              "mainattribute": "name",
              "subattribute": "email",
              "iconattribute": "customerclass",
              "iconmap": { "vip": "star", "standard": "account" },
              "colorattribute": "status",
              "colormap": { "active": "#4caf50", "inactive": "#9e9e9e" },
              "showrefresh": true },
            { "type": "button",
              "label": "New customer", "icon": "add",
              "action": "create",
              "param": { "status": "'active'" },
              "margin": true }
          ]
        }
      ]
    },
    {  // ── right panel: detail form bound to the selected customer ──
      "type": "hsection",
      "grow": 2, "shrink": 1,
      "content": [
        { "type": "dataset",
          "object": "customer", "id": "detail",
          "master": { "relationship": { "uid": "'obj.uid'" } },
          "fetchall": false,
          "content": [
            { "type": "form", "margin": true, "content": [
                { "type": "input",      "attribute": "name",     "label": "Name",      "mandatory": true, "size": 30 },
                { "type": "input",      "attribute": "email",    "label": "Email",     "icon": "email" },
                { "type": "switch",     "attribute": "active",   "label": "Active" },
                { "type": "currencyinput", "attribute": "limit", "label": "Credit limit", "decimalCount": 2 },
                { "type": "datepicker", "attribute": "since",    "label": "Customer since", "timepart": false },
                { "type": "textarea",   "attribute": "notes",    "label": "Notes", "rows": 4 }
            ] },
            { "type": "aggregateset",
              "object": "order",
              "master": { "relationship": { "customer": "'obj.uid'" } },
              "tuple":  { "status": true },
              "metrics": { "count": "COUNT" },
              "content": [
                { "type": "numbertiles", "rows": 1, "cols": 3, "format": "integer" }
              ] }
          ]
        }
      ]
    }
  ]
}
```

Notes:
- The detail `dataset` never lists data freely: it is slaved to the left panel's selection through `master.relationship` (`'obj.uid'` — a filter expression resolved against the parent dataset's selected object).
- Inputs with `attribute` automatically read/write the **selected object's** attribute and respect `validation` for editability/mandatoriness.
- The `aggregateset` inside the detail dataset is itself slaved the same way, so the KPI tiles follow the selected customer.

### 5.2 Master–detail with search bar, tabs, gantt, and a modal

Key structural rules this example obeys:
- `search`, the list and the create `button` must sit **inside** `dataset#projects` to receive its `dataset` context.
- The gantt needs a `datasetgroup` in its ancestor chain; therefore the group is placed inside `dataset#detail`. Group members (like `#tasks`) inherit the `dataset` context of the enclosing `dataset#detail` through the group, so their `master` relationships resolve against the selected project.
- The modal's form binds to the *clicked task* (not the project) via `targetdatasetid`.

```json
{
  "label": "Projects",
  "content": [
    { "type": "hsection", "grow": 1, "shrink": 0, "width": 40, "content": [
        { "type": "dataset", "object": "project", "id": "projects",
          "basesort": { "start": 1 },
          "content": [
            { "type": "search", "showsearchfield": true,
              "modes": [
                { "label": "All",  "filter": null },
                { "label": "Late", "filter": { "end": { "$lt": "new Date()" } } }
              ] },
            { "type": "list4", "mainattribute": "title", "subattribute": "customer.name",
              "colorattribute": "risk", "colormap": { "high": "#d32f2f", "medium": "#f9a825", "low": "#388e3c" },
              "allowdrag": true },
            { "type": "button", "label": "New project", "icon": "add", "action": "create",
              "param": { "status": "'open'" },
              "confirm": "Create a new project?" }
          ]
        }
      ] },

    { "type": "hsection", "grow": 2, "shrink": 1, "content": [
        { "type": "dataset", "object": "project", "id": "detail",
          "master": { "relationship": { "uid": "'obj.uid'" } },
          "content": [
            { "type": "datasetgroup", "content": [
                { "type": "dataset", "object": "worker", "id": "workers",
                  "basesort": { "name": 1 } },
                { "type": "dataset", "object": "task", "id": "tasks",
                  "master": { "relationship": { "project": "'obj.uid'" } },
                  "basesort": { "title": 1 } },
                { "type": "dataset", "object": "milestone", "id": "milestones",
                  "master": { "relationship": { "project": "'obj.uid'" } } },

                { "type": "tabsection", "content": [
                    { "type": "tab", "label": "Overview", "isdefault": true, "content": [
                        { "type": "form", "content": [
                            { "type": "input",  "attribute": "title", "label": "Title", "mandatory": true },
                            { "type": "switch", "attribute": "active", "label": "Active" }
                        ] }
                    ] },
                    { "type": "tab", "label": "Schedule", "content": [
                        { "type": "gantt", "id": "gantt",
                          "showcontrols": true, "headerwidth": 25, "allowpastdrop": true,
                          "lanes": { "dataset": "workers", "labelattribute": "name", "editable": true },
                          "series": [
                            { "dataset": "tasks",
                              "startattribute": "start", "durationattribute": "duration",
                              "laneattribute": "worker",
                              "labelattribute": "title",
                              "colorattribute": "risk", "colormap": { "high": "#d32f2f" },
                              "modal": "task-edit" }
                          ],
                          "overlays": [
                            { "dataset": "milestones", "label": "Milestones",
                              "startattribute": "date", "labelattribute": "name", "color": "#1c4e80" }
                          ],
                          "toolbar": [
                            { "type": "button", "label": "Refresh", "icon": "refresh", "action": "refresh" }
                          ] }
                    ] }
                ] },

                { "type": "modal", "name": "task-edit", "title": "Edit task", "icon": "edit",
                  "width": 80,
                  "content": [
                    { "type": "form", "content": [
                        { "type": "input",       "attribute": "title",    "label": "Task",
                          "targetdatasetid": "tasks" },
                        { "type": "datepicker",  "attribute": "start",    "label": "Start",
                          "targetdatasetid": "tasks" },
                        { "type": "numberinput", "attribute": "duration", "label": "Duration (ms)",
                          "targetdatasetid": "tasks" }
                    ] },
                    { "type": "button", "label": "Done", "action": "refresh" }
                  ] }
            ] }
          ]
        }
      ] }
  ]
}
```

Notes:
- `search` (inside `dataset#projects`) filters that dataset; its modes switch between "no filter" and "late projects" (`$lt "new Date()"` — a filter expression, §4.1).
- `dataset#detail` holds the single project selected in the left panel (`master`). Its sole data child is a `datasetgroup` — needed because the gantt references multiple datasets by `id`.
- Gantt task click: the task is selected in `#tasks`, then modal `task-edit` opens. Inputs in the modal use `targetdatasetid: "tasks"` so they bind to the selected **task**, not the project.
- Dragging a task spread to another worker lane sets `task.worker` (series `laneattribute`); `allowpastdrop`/`lanes.editable` shape the drag behavior.

---

## 6. Appendix: type → class mapping

All 74 registry keys with their component classes and source files (paths relative to `src/app/`):

| Type | Class | File |
|---|---|---|
| `dataset` | RbDatasetComponent | `rb-dataset/rb-dataset.component.ts` |
| `datasetgroup` | RbDatasetGroupComponent | `rb-datasetgroup/rb-datasetgroup.component.ts` |
| `layout` | RbLayoutComponent | `rb-layout/rb-layout.component.ts` |
| `form` | RbFormComponent | `rb-form/rb-form.component.ts` |
| `hsection` | RbHsectionComponent | `rb-hsection/rb-hsection.component.ts` |
| `vsection` | RbVsectionComponent | `rb-vsection/rb-vsection.component.ts` |
| `tabsection` | RbTabSectionComponent | `rb-tab-section/rb-tab-section.component.ts` |
| `tab` | RbTabComponent | `rb-tab/rb-tab.component.ts` |
| `input` | RbStringInputComponent | `inputs/rb-string-input/rb-string-input.component.ts` |
| `numberinput` | RbNumberInputComponent | `inputs/rb-number-input/rb-number-input.component.ts` |
| `relatedinput` | RbRelatedInputComponent | `inputs/rb-related-input/rb-related-input.component.ts` |
| `multirelatedinput` | RbMultiRelatedInputComponent | `inputs/rb-multi-related-input/rb-multi-related-input.component.ts` |
| `hierarchyinput` | RbHierarchyInputComponent | `inputs/rb-hierarchy-input/rb-hierarchy-input.component.ts` |
| `inlineinput` | RbInlineInputComponent | `inputs/rb-inline-input/rb-inline-input.component.ts` |
| `list3` | RbListComponent | `rb-list/rb-list.component.ts` |
| `list4` | RbList4Component | `rb-list4/rb-list4.component.ts` |
| `tree` | RbTreeComponent | `rb-tree/rb-tree.component.ts` |
| `button` | RbActionButtonComponent | `clickable/rb-actionbutton/rb-actionbutton.component.ts` |
| `search` | RbSearchComponent | `rb-search/rb-search.component.ts` |
| `actiongroup` | RbActiongroupComponent | `clickable/rb-actiongroup/rb-actiongroup.component.ts` |
| `addressinput` | RbAddressInputComponent | `inputs/rb-address-input/rb-address-input.component.ts` |
| `aggregateset` | RbAggregatesetComponent | `rb-aggregateset/rb-aggregateset.component.ts` |
| `choiceinput` | RbChoiceInputComponent | `inputs/rb-choice-input/rb-choice-input.component.ts` |
| `codeinput` | RbCodeInputComponent | `inputs/rb-code-input/rb-code-input.component.ts` |
| `currencyinput` | RbCurrencyInputComponent | `inputs/rb-currency-input/rb-currency-input.component.ts` |
| `durationinput` | RbDurationInputComponent | `inputs/rb-duration-input/rb-duration-input.component.ts` |
| `tagsinput` | RbTagsInputComponent | `inputs/rb-tags-input/rb-tags.component.ts` |
| `dynamicform` | RbDynamicformComponent | `rb-dynamicform/rb-dynamicform.component.ts` |
| `dynamicformeditor` | RbDynamicformeditorComponent | `rb-dynamicformeditor/rb-dynamicformeditor.component.ts` |
| `fileinput` | RbFileInputComponent | `inputs/rb-file-input/rb-file-input.component.ts` |
| `filedrop` | RbFiledropComponent | `rb-filedrop/rb-filedrop.component.ts` |
| `filelist` | RbFilelistComponent | `rb-filelist/rb-filelist.component.ts` |
| `fileset` | RbFilesetComponent | `rb-fileset/rb-fileset.component.ts` |
| `textarea` | RbTextareaInputComponent | `inputs/rb-textarea-input/rb-textarea-input.component.ts` |
| `richtext` | RbRichtextInputComponent | `inputs/rb-richtext-input/rb-richtext-input.component.ts` |
| `datepicker` | RbDatetimeInputComponent | `inputs/rb-datetime-input/rb-datetime-input.component.ts` |
| `dateinput` | RbDatetimeInputComponent | `inputs/rb-datetime-input/rb-datetime-input.component.ts` |
| `urlinput` | RbUrlInputComponent | `inputs/rb-url-input/rb-url-input.component.ts` |
| `colorinput` | RbColorInputComponent | `inputs/rb-color-input/rb-color-input.component.ts` |
| `gantt` | RbGanttComponent | `rb-gantt/rb-gantt.component.ts` |
| `link` | RbLinkComponent | `rb-link/rb-link.component.ts` |
| `log` | RbLogComponent | `rb-log/rb-log.component.ts` |
| `map` | RbMapComponent | `rb-map/rb-map.component.ts` |
| `modal` | RbModalComponent | `rb-modal/rb-modal.component.ts` |
| `processactionsbutton` | RbProcessactionsComponent | `clickable/rb-processactions/rb-processactions.component.ts` |
| `switch` | RbSwitchInputComponent | `inputs/rb-switch-input/rb-switch-input.component.ts` |
| `table` | RbTableComponent | `rb-table/rb-table.component.ts` |
| `linktable` | RbLinktableComponent | `rb-linktable/rb-linktable.component.ts` |
| `vcollapse` | RbVcollapseComponent | `rb-vcollapse/rb-vcollapse.component.ts` |
| `hcollapse` | RbHcollapseComponent | `rb-hcollapse/rb-hcollapse.component.ts` |
| `hseparator` | RbHseparatorComponent | `rb-hseparator/rb-hseparator.component.ts` |
| `vseparator` | RbVseparatorComponent | `rb-vseparator/rb-vseparator.component.ts` |
| `spacer` | RbSpacerComponent | `rb-spacer/rb-spacer.component.ts` |
| `graph` | RbDynamicGraphComponent | `graphs/rb-dynamicgraph/rb-dynamicgraph.component.ts` |
| `calendar` | RbCalendarComponent | `rb-calendar/rb-calendar.component.ts` |
| `tile` | RbTileComponent | `rb-tile/rb-tile.component.ts` |
| `numbertiles` | RbNumberTilesComponent | `graphs/rb-number-tiles/rb-number-tiles.component.ts` |
| `stackedgraph` | RbStackedGraphComponent | `graphs/rb-stacked-graph/rb-stacked-graph.component.ts` |
| `vbargraph` | RbVbarGraphComponent | `graphs/rb-vbar-graph/rb-vbar-graph.component.ts` |
| `hbargraph` | RbHbarGraphComponent | `graphs/rb-hbar-graph/rb-hbar-graph.component.ts` |
| `timeline` | RbTimelineComponent | `rb-timeline/rb-timeline.component.ts` |
| `stars` | RbStarsInputComponent | `inputs/rb-stars-input/rb-stars-input.component.ts` |
| `percentinput` | RbPercentInputComponent | `inputs/rb-percent-input/rb-percent-input.component.ts` |
| `funnel` | RbFunnelComponent | `rb-funnel/rb-funnel.component.ts` |
| `listbutton` | RbActionlistComponent | `clickable/rb-actionlist/rb-actionlist.component.ts` |
| `scroll` | RbScrollComponent | `rb-scroll/rb-scroll.component.ts` |
| `group` | RbGroupComponent | `rb-group/rb-group.component.ts` |
| `text` | RbTextComponent | `rb-text/rb-text.component.ts` |
| `progress` | RbProgressComponent | `rb-progress/rb-progress.component.ts` |
| `image` | RbImageComponent | `rb-image/rb-image.component.ts` |
| `icon` | RbIconComponent | `rb-icon/rb-icon.component.ts` |
| `selector` | RbSelectorComponent | `rb-selector/rb-selector.component.ts` |
| `repeater` | RbRepeaterComponent | `rb-repeater/rb-repeater.component.ts` |
| `pivot` | RbPivotTableComponent | `graphs/rb-pivot-table/rb-pivot-table.component.ts` |
