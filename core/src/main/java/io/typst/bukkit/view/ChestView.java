package io.typst.bukkit.view;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
    Function<BottomClickEvent, ViewAction> onBottomClick = e -> ViewAction.NOTHING;
    @Builder.Default
    Consumer<UpdateEvent> onContentsUpdate = e -> {
    };
    @Builder.Default
    List<Integer> overwriteMoveToOtherInventorySlots = Collections.emptyList();

    List<Integer> findSpaces(List<Integer> slots, ItemStack item) {
        List<Integer> ret = new ArrayList<>();
        int amount = item.getAmount();
        for (Integer slot : slots) {
            if (getContents().getControls().containsKey(slot)) {
                continue;
            }
            if (amount <= 0) {
                break;
            }
            ItemStack slotItem = getContents().getItems().get(slot);
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                ret.add(slot);
                amount -= item.getType().getMaxStackSize();
            } else if (slotItem.isSimilar(item)) {
                int newAmount = Math.min(slotItem.getAmount() + item.getAmount(), slotItem.getType().getMaxStackSize());
                if (newAmount > slotItem.getAmount()) {
                    ret.add(slot);
                    amount -= newAmount - slotItem.getAmount();
                }
            }
        }
        return ret;
    }
}
