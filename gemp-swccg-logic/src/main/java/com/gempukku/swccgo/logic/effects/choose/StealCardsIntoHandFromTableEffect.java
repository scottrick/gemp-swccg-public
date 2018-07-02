package com.gempukku.swccgo.logic.effects.choose;

import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.actions.SubAction;
import com.gempukku.swccgo.logic.timing.AbstractSubActionEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.PassthruEffect;

import java.util.Collection;
import java.util.Collections;

/**
 * An effect to steal the specified cards on table into hand.
 */
public class StealCardsIntoHandFromTableEffect extends AbstractSubActionEffect {
    private Collection<PhysicalCard> _remainingCards;

    /**
     * Creates an effect to steal the specified cards on table into hand.
     * @param action the action performing this effect
     * @param cardsToSteal the cards to steal
     */
    public StealCardsIntoHandFromTableEffect(Action action, Collection<PhysicalCard> cardsToSteal) {
        super(action);
        _remainingCards = Collections.unmodifiableCollection(cardsToSteal);
    }

    @Override
    public boolean isPlayableInFull(SwccgGame game) {
        return true;
    }

    @Override
    protected SubAction getSubAction(SwccgGame game) {
        SubAction subAction = new SubAction(_action);

        _remainingCards = Filters.filter(_remainingCards, game, Filters.onTable);

        if (!_remainingCards.isEmpty()) {
            subAction.appendEffect(
                    new ChooseNextCardToSteal(subAction, _remainingCards));
        }

        return subAction;
    }

    @Override
    protected boolean wasActionCarriedOut() {
        return true;
    }

    /**
     * A private effect for choosing the next card to be stolen.
     */
    private class ChooseNextCardToSteal extends ChooseCardOnTableEffect {
        private SubAction _subAction;

        /**
         * Creates an effect for choosing the next card to be stolen.
         * @param subAction the action
         * @param remainingCards the remaining cards to choose from to be stolen
         */
        public ChooseNextCardToSteal(SubAction subAction, Collection<PhysicalCard> remainingCards) {
            super(subAction, subAction.getPerformingPlayer(), "Choose card to steal", remainingCards);
            _subAction = subAction;
        }

        @Override
        protected void cardSelected(final PhysicalCard selectedCard) {
            // Perform the StealOneCardIntoHandEffect on the selected card
            _subAction.appendEffect(
                    new StealOneCardIntoHandEffect(_subAction, selectedCard));
            _subAction.appendEffect(
                    new PassthruEffect(_subAction) {
                        @Override
                        protected void doPlayEffect(SwccgGame game) {
                            _remainingCards.remove(selectedCard);
                            _remainingCards = Filters.filter(_remainingCards, game, Filters.onTable);
                            if (!_remainingCards.isEmpty()) {
                                _subAction.appendEffect(
                                        new ChooseNextCardToSteal(_subAction, _remainingCards));
                            }
                        }
                    }
            );
        }
    }
}
