package com.gempukku.swccgo.cards.actions;

import com.gempukku.swccgo.cards.effects.PayLandingCostEffect;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.actions.AbstractTopLevelRuleAction;
import com.gempukku.swccgo.logic.effects.LandEffect;
import com.gempukku.swccgo.logic.effects.MovingAsReactEffect;
import com.gempukku.swccgo.logic.effects.choose.ChooseCardOnTableEffect;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.Effect;

/**
 * An action to land a starship or vehicle.
 */
public class LandAction extends AbstractTopLevelRuleAction {
    private PhysicalCard _cardToMove;
    private boolean _forFree;
    private boolean _asReact;
    private PhysicalCard _locationToMoveTo;
    private boolean _useForceCostApplied;
    private MovingAsReactEffect _moveCardEffect;
    private boolean _cardMoved;
    private Action _that;

    /**
     * Creates an action to land starship or vehicle.
     * @param playerId the player
     * @param card the card to move
     * @param forFree true if moving for free, otherwise false
     * @param asReact true if moving as a 'react', otherwise false
     * @param moveTargetFilter the filter for where the card can be move
     */
    public LandAction(final String playerId, final PhysicalCard card, boolean forFree, boolean asReact, final Filter moveTargetFilter) {
        super(card, playerId);
        _cardToMove = card;
        _forFree = forFree;
        _asReact = asReact;
        _that = this;

        appendTargeting(
                new ChooseCardOnTableEffect(_that, playerId, "Choose where to land " + GameUtils.getCardLink(card), moveTargetFilter) {
                    @Override
                    protected void cardSelected(PhysicalCard selectedCard) {
                        _locationToMoveTo = selectedCard;

                        _moveCardEffect = new LandEffect(_that, card, _locationToMoveTo, _asReact);
                    }
                }
        );
    }

    @Override
    public String getText() {
        return "Land";
    }

    @Override
    public Effect nextEffect(SwccgGame game) {
        // Verify no costs have failed
        if (!isAnyCostFailed()) {

            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_useForceCostApplied) {
                _useForceCostApplied = true;
                if (!_forFree && !_asReact) {
                    appendCost(new PayLandingCostEffect(_that, getPerformingPlayer(), _cardToMove, _locationToMoveTo, false, 0));
                    return getNextCost();
                }
            }

            if (!_cardMoved) {
                _cardMoved = true;

                // Set the move as 'react' effect
                if (_asReact) {
                    game.getGameState().getMoveAsReactState().setMovingAsReactEffect(_moveCardEffect);
                }

                return _moveCardEffect;
            }

            Effect effect = getNextEffect();
            if (effect != null)
                return effect;
        }

        return null;
    }

    @Override
    public boolean wasActionCarriedOut() {
        return _cardMoved && _moveCardEffect.wasCarriedOut();
    }
}
