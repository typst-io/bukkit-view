# bukkit-view

![Maven Central Version](https://img.shields.io/maven-central/v/io.typst/bukkit-view-core)

A [pure](https://en.wikipedia.org/wiki/Purely_functional_programming) library to express minecraft chest view.

There is no side effect except `BukkitView.class`, all functions just pure, therefore this can be run in multithreads -- even Bukkit part can't -- and easy to write unit tests.

Also, this library is a good showcase how to do declarative programming in Java.

[Example is here!](https://github.com/typst-io/bukkit-view/blob/main/plugin/src/main/java/io/typst/bukkit/view/plugin/ViewPlugin.java)

## Import

### Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.typst:bukkit-view-core:7.2.0")
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.typst</groupId>
        <artifactId>bukkit-view-core</artifactId>
        <version>7.2.0</version>
    </dependency>
</dependencies>
```

### Quickstart

https://github.com/typst-io/bukkit-view-template

```shell
git clone https://github.com/typst-io/bukkit-view-template
```

## Initialize

```java
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        BukkitView.register(this);
    }
}
```

## ChestView

```java
ChestView subView = ...;

Map<Integer, ViewControl> map = new HashMap<>();
String title = "title";
int row = 1;
map.put(3, ViewControl.of(
    new ItemStack(Material.DIAMOND),
    e -> {
        // this view item don't -- also shouldn't -- know how to open view,
        // just tell what view want to open.
        return new ViewAction.Open(subView);
    }
));
ViewContents contents = ViewContents.ofControls(map);
ChestView view = ChestView.just(title, row, contents);
BukkitView.openView(view, player, plugin);
```

To open asynchronously, `ViewAction.OpenAsync(Future<View>)`:

```java
ViewControl.of(
    bukkitItemStack,
    e -> {
        Future<ChestView> myChestViewFuture;
        return new ViewAction.OpenAsync(myChestViewFuture);
    }
)
```

To update just contents, `ViewAction.Update` also `ViewAction.UpdateAsync(Future<ViewContents>)`

```java
ViewControl.of(
    bukkitItemStack,
    e -> new ViewAction.Update(newContents)
    // UpdateAsync if needed
)
```

On close the view:

```java
ChestView.of(title, row, map, closeEvent -> {
    return ViewAction.NOTHING; // or ViewAction.REOPEN
})
```

## PageView

Default construction `ofDefault()` for `PageViewLayout`:

```java
// Lazy `Function<PageContext, ViewControl>` not just `ViewControl`
List<Function<PageContext, ViewControl>> items = ...;
PageViewLayout layout = PageViewLayout.ofDefault(
    "title", 
    6, 
    Material.STONE_BUTTON, 
    items
);
```

Full construction for `PageViewLayout`:

```java
// Paging elements
List<Function<PageContext, ViewControl>> items = ...;
// Paging elements will be put in this slots.
List<Integer> slots = ...;
// Control means fixed view-item, won't affected by view paging.
Map<Integer, Function<PageContext, ViewControl>> controls = ...;
String title = "title";
int row = 6;
PageViewLayout layout = PageViewLayout.of(title, row, items, slots, controls);
```

Evaluate a single page from the layout and open:

```java
int page = 1;
ChestView view = layout.toView(page);
BukkitView.openView(view, player, plugin);
```

## ViewControl

Constructions:

`ViewControl.of(ItemStack, Function<ClickEvent, ViewAction>)`

> (ItemStack, ClickEvent -> ViewAction) -> ViewControl

`ViewControl.just(ItemStack)`

> ItemStack -> ViewControl

`ViewControl.consumer(ItemStack, Consumer<ViewAction>)`

> (ItemStack, ClickEvent -> Unit) -> ViewControl
