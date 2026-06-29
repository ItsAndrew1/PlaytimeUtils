//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

public class StaffState {
    public AddRemoveChoice choice;
    public RewardType rewardType;
    public PlacementChoice placement;

    public StaffState(AddRemoveChoice choice, RewardType rewardType, PlacementChoice placement){
        this.choice = choice;
        this.rewardType = rewardType;
        this.placement = placement;
    }
}