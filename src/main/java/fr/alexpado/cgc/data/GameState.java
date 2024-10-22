package fr.alexpado.cgc.data;

import fr.alexpado.cgc.enums.PlayTurn;
import fr.alexpado.cgc.heplers.MixedResponse;

public interface GameState extends MixedResponse {

    PlayTurn getPlayTurn();

}
