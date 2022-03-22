# bukkit-view

A [pure](https://en.wikipedia.org/wiki/Purely_functional_programming) library to express minecraft chest view.

There is no side effect except `BukkitView.class`, all functions just pure, therefore this can be run in multithreads -- even Bukkit part can't -- and easy to write unit tests.

Also, this library is a good showcase how to do declarative programming in Java.

[Example is here!](https://github.com/typecraft-io/bukkit-view/blob/main/plugin/src/main/java/io/typecraft/bukkit/view/plugin/ViewPlugin.java)

## Import

### Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.typecraft:bukkit-view-core:3.2.0")
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.typecraft</groupId>
        <artifactId>bukkit-view-core</artifactId>
        <version>3.2.0</version>
    </dependency>
</dependencies>
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

Map<Integer, ViewItem> map = new HashMap<>();
String title = "title";
int row = 1;
map.put(3, ViewItem.of(
    new ItemStack(Material.DIAMOND),
    e -> {
        // this view item don't -- also shouldn't -- know how to open view,
        // just tell what view want to open.
        return new ViewAction.Open(subView);
    }
));
ChestView view = new ChestView(title, row, map);
BukkitView.openView(view, player, plugin);
```

To open asynchronously, `ViewAction.OpenAsync(Future<View>)`:
```java
ViewItem.of(
    bukkitItemStack,
    e -> {
        Future<ChestView> myChestViewFuture;
        return new ViewAction.OpenAsync(myChestViewFuture);
    }
)
```

On close the view:
```java
new ChestView(title, row, map, closeEvent -> {
    return ViewAction.NOTHING; // or ViewAction.REOPEN
})
```

## PageView

Default construction `ofDefault()` for `PageViewLayout`:

```java
// Lazy `Function<PageContext, ViewItem>` not just `ViewItem`
List<Function<PageContext, ViewItem>> items = ...;
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
List<Function<PageContext, ViewItem>> items = ...;
// Paging elements will be put in this slots.
List<Integer> slots = ...;
// Control means fixed view-item, won't affected by view paging.
Map<Integer, Function<PageContext, PageViewControl>> controls = ...;
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

## ViewItem

Constructions:

`ViewItem.of(ItemStack, Function<ClickEvent, ViewAction>)`

> (ItemStack, ClickEvent -> ViewAction) -> ViewItem

`ViewItem.just(ItemStack)`

> ItemStack -> ViewItem

`ViewItem.consumer(ItemStack, Consumer<ViewAction>)`

> (ItemStack, ClickEvent -> Unit) -> ViewItem
