package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.game.state.GameState;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.results.PutCardInCardPileFromOffTableResult;

import java.util.Collections;

/**
 * An effect to put the specified stacked card in the specified card pile.
 */
abstract class PutOneStackedCardInCardPileEffect extends PutOneCardInCardPileEffect {

    /**
     * Creates an effect that causes the specified stacked card to be put in the specified card pile.
     * @param action the action performing this effect
     * @param card the card
     * @param cardPile the card pile
     * @param bottom true if cards are to be put on the bottom of the card pile, otherwise false
     * @param msgText the message to send
     */
    protected PutOneStackedCardInCardPileEffect(Action action, PhysicalCard card, Zone cardPile, boolean bottom, String msgText) {
        super(action, card, cardPile, card.getOwner(), bottom, msgText);
    }

    @Override
    protected void doPlayEffect(SwccgGame game) {
        if (_card.getZone() == Zone.STACKED || _card.getZone() == Zone.STACKED_FACE_DOWN) {
            GameState gameState = game.getGameState();
            gameState.removeCardsFromZone(Collections.singleton(_card));
            _card.setOwner(_cardPileOwner);
            if (_bottom)
                gameState.addCardToZone(_card, _zone, _cardPileOwner);
            else
                gameState.addCardToTopOfZone(_card, _zone, _cardPileOwner);
            gameState.sendMessage(_msgText);

            // Emit effect result
            game.getActionsEnvironment().emitEffectResult(
                    new PutCardInCardPileFromOffTableResult(_action, _card, _cardPileOwner, _zone, false));

            // A callback that can be used after card is placed in card pile
            afterCardPutInCardPile();
        }

        // A callback that can be used to schedule the next card to be put in card pile
        scheduleNextStep();
    }

    @Override
    protected final void afterCardPutInCardPile() {
    }
}
