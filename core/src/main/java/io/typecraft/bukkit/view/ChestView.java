package io.typecraft.bukkit.view;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Value(staticConstructor = "of")
@With
@Builder
public class ChestView {
    @Builder.Default
    String title = "";
    @Builder.Default
    int row = 6;
    @Builder.Default
    ViewContents contents = ViewContents.ofControls(Collections.emptyMap());
    @Builder.Default
    Function<CloseEvent, ViewAction> onClose = e -> ViewAction.NOTHING;
    @Builder.Default
    Consumer<UpdateEvent> onContentsUpdate = e -> {};
    @Builder.Default
    List<Integer> overwriteMoveToOtherInventorySlots = Collections.emptyList();

    int findFirstSpace(List<Integer> slots, @Nullable ItemStack item) {
        int ret = -1;
        for (Integer slot : slots) {
            if (getContents().getControls().containsKey(slot)) {
                continue;
            }
            ItemStack slotItem = getContents().getItems().get(slot);
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                return slot;
            }
            if (item != null && item.isSimilar(slotItem)) {
                int newAmount = slotItem.getAmount() + item.getAmount();
                if (newAmount <= slotItem.getType().getMaxStackSize()) {
                    return slot;
                }
            }
        }
        return ret;
    }
}
