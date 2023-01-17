package org.ccu.core.commands;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.labymod.api.client.chat.command.Command;
import org.ccu.core.CCU;
import org.ccu.core.Colours;
import org.ccu.core.config.CCUManager;
import org.ccu.core.config.imp.GameStatsTracker;
import org.ccu.core.config.subconfig.StatsTrackerSubConfig;

public class StatCommands extends Command {

  private final CCU addon;
  private final Pattern timeFormat = Pattern.compile("\\b(20[0-9]{2})-([1-9]|1[1-2])-(1[0-9]|2[0-9]|3[0-1]|[1-9])\\b");

  @Inject
  private StatCommands(CCU addon) {
    super("stats");

    this.addon = addon;
  }

  // TODO: Colours
  @Override
  public boolean execute(String prefix, String[] arguments) {
    CCUManager manager = this.addon.getManager();
    StatsTrackerSubConfig config = this.addon.configuration().getStatsTrackerSubConfig();
    HashMap<String, GameStatsTracker> allGameStatsTrackers = config.getGameStatsTrackers();

    if (arguments.length == 0) {
      return false;
    }

    String gameName = this.getGameString(arguments);

    GameStatsTracker gameStatsTracker;
    if (gameName == null ) {
       gameStatsTracker = allGameStatsTrackers.get(manager.getDivisionName());
    } else {
      gameStatsTracker = allGameStatsTrackers.get(gameName);
    }

    if (gameStatsTracker == null) {
      if (!arguments[0].equals("help")) {
        this.displayMessage(
            this.addon.prefix()
                .append(Component.text(" Could not find the specified game. Possible options are ", Colours.Error))
                .append(this.gamesList().color(Colours.Error))
        );
        return false;
      } else {
        this.helpCommand();
        return true;
      }
    }

    if (gameName != null) {
      arguments = this.removeGameNameFromArguments(arguments, gameName);
    }

    switch (arguments.length) {
      case 0: {
        this.displayMessage(gameStatsTracker.getDisplayComponent(this.addon));
        return true;
      }
      case 1: {
        if (this.timeFormat.matcher(arguments[0]).matches()) {
          GameStatsTracker snapShot = gameStatsTracker.getHistoricalData(arguments[0]);
          this.displayMessage(snapShot.getDisplayComponent(this.addon));
        } else if (arguments[0].equals("help")) {
          this.helpCommand();
        } else {
          this.displayMessage(gameStatsTracker.getUserStatsDisplayComponent(this.addon, arguments[0]));
        }
        return true;
      }
      case 2: {
        if (this.timeFormat.matcher(arguments[0]).matches()) {
          GameStatsTracker snapShot = gameStatsTracker.getHistoricalData(arguments[0]);
          this.displayMessage(snapShot.getUserStatsDisplayComponent(this.addon, arguments[1]));
          return true;
        } else if (this.timeFormat.matcher(arguments[1]).matches()) {
            GameStatsTracker snapShot = gameStatsTracker.getHistoricalData(arguments[1]);
            this.displayMessage(snapShot.getUserStatsDisplayComponent(this.addon, arguments[0]));
          return true;
        } else if (arguments[0].equals("help")) {
          this.helpCommand();
        }
      }
    }


    return false;
  }

  private void helpCommand() {
    Component helpComponent = this.addon.prefix()
        .append(Component.text("------- Enhanced Stats Commands -------", Colours.Title))
        .append(Component.text("\n/stats <game>", Colours.Primary)
            .clickEvent(ClickEvent.suggestCommand("/stats ")))
        .append(Component.text(" Displays your stats in a game.", Colours.Secondary))
        .append(Component.text("\n/stats <game> <username>", Colours.Primary)
            .clickEvent(ClickEvent.suggestCommand("/stats ")))
        .append(Component.text(" Displays your kills on and deaths by a player.", Colours.Secondary))
        .append(Component.text("\n/stats <game> <YYYY-MM-DD>", Colours.Primary)
            .clickEvent(ClickEvent.suggestCommand("/stats ")))
        .append(Component.text(" Displays your stats in a game on a certain day (Omit real 0's).", Colours.Secondary))
        .append(Component.text("\n/stats <game> <YYYY-MM-DD> <username>", Colours.Primary)
            .clickEvent(ClickEvent.suggestCommand("/stats "))
            .hoverEvent(HoverEvent.showText(Component.text("  This has to be enabled in settings.", Colours.Hover))))
        .append(Component.text(" Displays your stats in a game on a certain day with a player (Omit real 0's).", Colours.Secondary)
            .hoverEvent(HoverEvent.showText(Component.text("  This has to be enabled in settings.", Colours.Hover))))
        .append(Component.text("\n/stats help", Colours.Primary)
            .clickEvent(ClickEvent.suggestCommand("/stats ")))
        .append(Component.text(" Displays this message.", Colours.Secondary))
        .append(Component.text("\n\nGames tracked: ", Colours.Primary))
        .append(this.gamesList().color(Colours.Secondary));

    this.displayMessage(helpComponent);
  }

  private Component gamesList() {
    Component comp = Component.empty();
    Set<String> keySet = this.addon.configuration().getStatsTrackerSubConfig().getGameStatsTrackers().keySet();
    List<String> keys = new ArrayList<>(keySet);
    for (int i = 0; i < keys.size(); i++) {
      String game = keys.get(i);
      comp = comp
          .append(Component.text(game)
              .clickEvent(ClickEvent.suggestCommand("/stats " + game + " ")));
      if (i != keys.size() - 1) {
        comp = comp.append(Component.text(", "));
      }
    }
    return comp;
  }

  private String[] removeGameNameFromArguments(String[] arguments, String gameName) {
    String[] gameComponents = gameName.split(" ");

    for (String comp : gameComponents) {
      arguments = this.removeFromArguments(arguments, comp);
    }
    return arguments;
  }

  private String[] removeFromArguments(String[] arguments, String member) {
    String[] newArguments = new String[arguments.length];

    int j = 0;
    for (String argument : arguments) {
      if (!argument.equals(member)) {
        newArguments[j] = argument;
        j++;
      }
    }
    return newArguments;
  }

  private String getGameString(String[] arguments) {
    String game = "";

    for (String arg : arguments) {
      game = game + arg;

      GameStatsTracker tracker = this.addon.configuration().getStatsTrackerSubConfig().getGameStatsTrackers()
          .get(game);

      if (tracker != null) {
        return game;
      }
    }

    return null;
  }
}
