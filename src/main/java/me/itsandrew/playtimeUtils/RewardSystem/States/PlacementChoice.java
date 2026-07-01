//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

public enum PlacementChoice {
    FIRST, SECOND, THIRD;

    public String toConfigFileForm(){
        return this.name().toLowerCase()+"-place";
    }

    public String toColoredStringForm(){
        switch(this){
            case FIRST -> {return "<gradient:#ffee55:#ffaa00><b>1st Place";}
            case SECOND -> {return "<gradient:#ffffff:#bbbacc><b>2nd Place";}
            case THIRD -> {return "<gradient:#ccc923:#e6765a><b>3rd Place";}
            default -> {return " ";}
        }
    }
}
