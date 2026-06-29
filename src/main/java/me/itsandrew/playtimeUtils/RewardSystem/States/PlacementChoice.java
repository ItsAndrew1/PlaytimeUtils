//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

public enum PlacementChoice {
    FIRST, SECOND, THIRD;

    public String toStringForm(){
        return this.name().toLowerCase()+"-place";
    }
}
