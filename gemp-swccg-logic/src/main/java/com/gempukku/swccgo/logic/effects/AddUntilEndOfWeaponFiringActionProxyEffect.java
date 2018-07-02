package com.gempukku.swccgo.logic.effects;

import com.gempukku.swccgo.game.ActionProxy;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.timing.Action;
import com.gempukku.swccgo.logic.timing.PassthruEffect;

/**
 * An effect that adds an action proxy until the end of the weapon firing.
 */
public class AddUntilEndOfWeaponFiringActionProxyEffect extends PassthruEffect {
    private ActionProxy _actionProxy;

    /**
     * Creates an effect that adds an action proxy until the end of the weapon firing.
     * @param action the action performing this effect
     * @param actionProxy the action proxy
     */
    public AddUntilEndOfWeaponFiringActionProxyEffect(Action action, ActionProxy actionProxy) {
        super(action);
        _actionProxy = actionProxy;
    }

    @Override
    public void doPlayEffect(SwccgGame game) {
        game.getActionsEnvironment().addUntilEndOfWeaponFiringActionProxy(_actionProxy);
    }
}