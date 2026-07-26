package com.protyvkultury.revivalages.api.weight;

import java.util.Objects;
import net.neoforged.bus.api.Event;

/**
 * Posted once on the NeoForge event bus during common setup. Registrations made
 * after the event returns are rejected.
 */
public final class RegisterCarriedWeightProvidersEvent extends Event {

    private final CarriedWeightRegistrar registrar;

    public RegisterCarriedWeightProvidersEvent(CarriedWeightRegistrar registrar) {
        this.registrar = Objects.requireNonNull(registrar);
    }

    public CarriedWeightRegistrar registrar() {
        return registrar;
    }
}
