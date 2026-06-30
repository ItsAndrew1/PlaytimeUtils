//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.States;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum PlacementChoice {
    FIRST, SECOND, THIRD;

    public String toStringForm(){
        return this.name().toLowerCase()+"-place";
    }

    public Component toDeserializedComponent(){
        switch(this){
            case FIRST -> {return MiniMessage.miniMessage().deserialize("<gradient:#ffee55:#ffaa00><b>1st Place");}
            case SECOND -> {return MiniMessage.miniMessage().deserialize("<gradient:#ffffff:#bbbacc><b>2nd Place");}
            case THIRD -> {return MiniMessage.miniMessage().deserialize("<gradient:#ccc923:#e6765a><b>3rd Place");}
        }
        return null;
    }
}
