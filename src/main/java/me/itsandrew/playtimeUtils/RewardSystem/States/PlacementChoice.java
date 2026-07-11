//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

public enum PlacementChoice {
    FIRST, SECOND, THIRD;

    public String toConfigFileForm(){
        return this.name().toLowerCase()+"-place";
    }

    public String toColoredStringForm(){
        switch(this){
            case FIRST -> {return "&e&l1st Place";}
            case SECOND -> {return "&f&l2nd Place";}
            case THIRD -> {return "&6&l3rd Place";}
            default -> {return " ";}
        }
    }
}
