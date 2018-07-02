package com.gempukku.swccgo.cards.set7.dark;

import com.gempukku.swccgo.cards.AbstractDevice;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.cards.effects.RevealCardFromOwnHandEffect;
import com.gempukku.swccgo.cards.effects.UseDeviceEffect;
import com.gempukku.swccgo.cards.effects.usage.OncePerTurnEffect;
import com.gempukku.swccgo.common.*;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.GameUtils;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.OptionalGameTextTriggerAction;
import com.gempukku.swccgo.logic.decisions.YesNoDecision;
import com.gempukku.swccgo.logic.effects.PlayoutDecisionEffect;
import com.gempukku.swccgo.logic.effects.RetrieveForceEffect;
import com.gempukku.swccgo.logic.effects.choose.DrawCardIntoHandFromForcePileEffect;
import com.gempukku.swccgo.logic.modifiers.MayNotDeployToTargetModifier;
import com.gempukku.swccgo.logic.modifiers.Modifier;
import com.gempukku.swccgo.logic.modifiers.ModifierFlag;
import com.gempukku.swccgo.logic.modifiers.SpecialFlagModifier;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.results.ActivatedForceResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Special Edition
 * Type: Device
 * Title: Floating Refinery
 */
public class Card7_216 extends AbstractDevice {
    public Card7_216() {
        super(Side.DARK, 4, PlayCardZoneOption.ATTACHED, Title.Floating_Refinery, Uniqueness.RESTRICTED_2);
        setLore("Refines Tibanna gas at mines like those at Cloud City. Also used for moisture collection on dry planets.");
        setGameText("Deploy on a cloud sector (limit one per sector). Force you activate may be drawn into hand (one per turn for each of your Floating Refineries on table). Each cloud sector or gas miner drawn in this way may be revealed to retrieve 1 Force.");
        addIcons(Icon.SPECIAL_EDITION);
    }

    @Override
    protected Filter getGameTextValidDeployTargetFilter(SwccgGame game, PhysicalCard self, PlayCardOptionId playCardOptionId, boolean asReact) {
        return Filters.cloud_sector;
    }

    @Override
    protected List<Modifier> getGameTextAlwaysOnModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new MayNotDeployToTargetModifier(self, Filters.sameLocationAs(self, Filters.and(Filters.other(self), Filters.Floating_Refinery))));
        return modifiers;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(SwccgGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new SpecialFlagModifier(self, ModifierFlag.DO_NOT_SKIP_LISTENER_UPDATES_DURING_FORCE_ACTIVATION, self.getOwner()));
        return modifiers;
    }

    @Override
    protected List<OptionalGameTextTriggerAction> getGameTextOptionalAfterTriggers(final String playerId, final SwccgGame game, final EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        // Check condition(s)
        if (TriggerConditions.forceActivated(game, effectResult, playerId)
                && GameConditions.isOncePerTurn(game, self, gameTextSourceCardId)
                && GameConditions.canUseDevice(game, self, self)) {
            PhysicalCard cardActivated = ((ActivatedForceResult) effectResult).getCard();
            if (cardActivated.getZone() == Zone.TOP_OF_FORCE_PILE) {

                final OptionalGameTextTriggerAction action = new OptionalGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Draw activated Force into hand");
                // Update usage limit(s)
                action.appendUsage(
                        new OncePerTurnEffect(action));
                action.appendUsage(
                        new UseDeviceEffect(action, self));
                // Perform result(s)
                action.appendEffect(
                        new DrawCardIntoHandFromForcePileEffect(action, playerId) {
                            @Override
                            protected void cardDrawnIntoHand(final PhysicalCard card) {
                                if (Filters.or(Filters.cloud_sector, Filters.gas_miner).accepts(game, card)) {
                                    action.appendEffect(
                                            new PlayoutDecisionEffect(action, playerId,
                                                    new YesNoDecision("Do you want to reveal " + GameUtils.getCardLink(card) + " to retrieve 1 Force?") {
                                                        @Override
                                                        protected void yes() {
                                                            game.getGameState().sendMessage(playerId + " chooses to reveal " + GameUtils.getCardLink(card) + " to retrieve 1 Force");
                                                            action.appendEffect(
                                                                    new RevealCardFromOwnHandEffect(action, playerId, card) {
                                                                        @Override
                                                                        protected void cardRevealed(PhysicalCard cardRevealed) {
                                                                            action.appendEffect(
                                                                                    new RetrieveForceEffect(action, playerId, 1));
                                                                        }
                                                                    }
                                                            );
                                                        }
                                                    }
                                            )
                                    );
                                }
                            }
                        }
                );
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}