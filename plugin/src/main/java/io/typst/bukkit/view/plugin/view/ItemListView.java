package io.typst.bukkit.view.plugin.view;

import io.typst.bukkit.view.ChestView;
import io.typst.bukkit.view.ClickEvent;
import io.typst.bukkit.view.ViewAction;
import io.typst.bukkit.view.ViewControl;
import io.typst.bukkit.view.page.PageContext;
import io.typst.bukkit.view.page.PageViewLayout;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemListView {
    public static ChestView create(Stream<Material> materials) {
        return createLayout(materials).toView(1);
    }

    public static PageViewLayout createLayout(Stream<Material> materials) {
        List<Function<PageContext, ViewControl>> pagingContents =
            materials
                .map(material ->
                    // Why `Function<PageContext, ViewControl` not just `ViewControl`?
                    // Because of laziness to avoid lag from immediate calculation the all page items.
                    (Function<PageContext, ViewControl>) ctx ->
                        ViewControl.of(
                            e -> createDisplayItem(material),
                            e -> onClick(e, material)
                        )
                )
                .collect(Collectors.toList());
        return PageViewLayout.ofDefault(
            "materials",
            6,
            Material.STONE_BUTTON,
            pagingContents
        );
    }

    private static ItemStack createDisplayItem(Material material) {
        return new ItemStack(material);
    }

    private static ViewAction onClick(ClickEvent event, Material material) {
        Player clicker = event.getPlayer();
        if (clicker.isOp()) {
            clicker.getInventory().addItem(new ItemStack(material));
        }
        return ViewAction.NOTHING;
    }
}
