//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

import org.bukkit.inventory.ItemStack;

public class StaffState {
    public AddRemoveChoice choice;
    public RewardType rewardType;
    public PlacementChoice placement;
    public ItemStack itemToRemove;

    public StaffState(AddRemoveChoice choice, RewardType rewardType, PlacementChoice placement, ItemStack itemToRemove){
        this.choice = choice;
        this.rewardType = rewardType;
        this.placement = placement;
        this.itemToRemove = itemToRemove;
    }
}