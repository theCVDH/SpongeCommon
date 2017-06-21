/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;

final class PostState extends GeneralState {

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state.getPhase() == TrackingPhases.GENERATION || state == BlockPhase.State.RESTORING_BLOCKS;
    }

    @Override
    public boolean tracksBlockRestores() {
        return false; // TODO - check that this really is needed.
    }
    @Override
    void unwind(PhaseContext context) {
        final IPhaseState unwindingState = context.getRequiredExtra(InternalNamedCauses.Tracker.UNWINDING_STATE, IPhaseState.class);
        final PhaseContext unwindingContext = context.getRequiredExtra(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, PhaseContext.class);
        this.getPhase().postDispatch(unwindingState, unwindingContext, context);
    }

    public void appendContextPreExplosion(PhaseContext phaseContext, PhaseData currentPhaseData) {
        final IPhaseState phaseState = currentPhaseData.context.getRequiredExtra(InternalNamedCauses.Tracker.UNWINDING_STATE, IPhaseState.class);
        final PhaseContext unwinding = currentPhaseData.context.getRequiredExtra(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, PhaseContext.class);
        final PhaseData phaseData = new PhaseData(unwinding, phaseState);
        phaseState.getPhase().appendContextPreExplosion(phaseContext, phaseData);

    }

    @Override
    public boolean spawnEntityOrCapture(PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }
}
