package org.cubecraftutilities.core.utils.imp.base;

import net.labymod.api.client.component.Component;
import org.cubecraftutilities.core.utils.ColourConverters;

public interface EggWarsMap {

  Component getGenLayoutComponent();
  Component getMapLayoutComponent();

  Component getBuildLimitMessage();

  String getPartyMessage();

  String getName();

  void setCurrentTeamColour(String teamColour);

  default Component getTeamFiller(String colour) {
    return Component.text("||||||", ColourConverters.colourToNamedTextColor(colour));
  }

}
