# bukkit-view

A [pure](https://en.wikipedia.org/wiki/Purely_functional_programming) library to express minecraft chest view.

There's no any side effect except `BukkitView.class`, all functions just pure, therefore this can be run in multithreads -- even Bukkit part can't -- and easy to write unit tests.

Also, this library is a good showcase how to do declarative programming in Java.

## Initialize

```java
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(BukkitView.viewListener(this), this);
    }
}
```

## View

```java
View subView = ...;

Map<Integer, ViewItem> map = new HashMap<>();
String title = "title";
int row = 1;
map.put(3, new ViewItem(
        new ItemStack(Material.DIAMOND),
        e -> {
            // this view item don't -- also shouldn't -- know how to open view,
            // just tell what view want to open.
            return new ViewAction.Open(subView);
        }
));
View view = new View(title, row, map);
BukkitView.openView(view, player);
```

## PageView

Default construction `ofDefault()` for `PageViewLayout`:

```java
// Lazy `Supplier<ViewItem>` not just `ViewItem`
List<Supplier<ViewItem>> items = ...;
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
List<Supplier<ViewItem>> items = ...;
// Paging elements will be put in this slots.
List<Integer> slots = ...;
// Control means fixed view-item, won't affected by view paging.
Map<Integer, Function<PageContext, PageViewControl>> controls = ...;
String title = "title";
int row = 6;
PageViewLayout layout = new PageViewLayout(title, row, items, slots, controls);
```

Evaluate a single page from the layout and open:

```java
int page = 1;
View view = layout.toView(page);
BukkitView.openView(view, player);
```

## ViewItem

Constructions:

`new ViewItem(ItemStack, Function<ClickEvent, ViewAction>)`

> (ItemStack, ClickEvent -> ViewAction) -> ViewItem

`ViewItem.just(ItemStack)`

> ItemStack -> ViewItem

`ViewItem.consumer(ItemStack, Consumer<ViewAction>)`

> (ItemStack, ClickEvent -> Unit) -> ViewItem
