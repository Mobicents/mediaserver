/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.control.mgcp.pkg.au.pc;

import java.util.Map;

import org.apache.log4j.Logger;
import org.mobicents.media.control.mgcp.pkg.AbstractMgcpSignal;
import org.mobicents.media.control.mgcp.pkg.MgcpEventSubject;
import org.mobicents.media.control.mgcp.pkg.SignalType;
import org.mobicents.media.control.mgcp.pkg.au.AudioPackage;
import org.mobicents.media.control.mgcp.pkg.au.SignalParameters;
import org.mobicents.media.server.spi.dtmf.DtmfDetector;
import org.mobicents.media.server.spi.dtmf.DtmfDetectorListener;
import org.mobicents.media.server.spi.dtmf.DtmfEvent;
import org.mobicents.media.server.spi.player.Player;
import org.mobicents.media.server.spi.player.PlayerEvent;
import org.mobicents.media.server.spi.player.PlayerListener;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

/**
 * Plays a prompt and collects DTMF digits entered by a user.
 * 
 * <p>
 * If no digits are entered or an invalid digit pattern is entered, the user may be reprompted and given another chance to enter
 * a correct pattern of digits. The following digits are supported: 0-9, *, #, A, B, C, D.
 * </p>
 * 
 * <p>
 * By default PlayCollect does not play an initial prompt, makes only one attempt to collect digits, and therefore functions as
 * a simple Collect operation.<br>
 * Various special purpose keys, key sequences, and key sets can be defined for use during the PlayCollect operation.
 * </p>
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class PlayCollect extends AbstractMgcpSignal {
    
    private static final Logger log = Logger.getLogger(PlayCollect.class);

    static final String SYMBOL = "pc";

    // Finite State Machine
    private final PlayCollectFsm fsm;

    // Media Components
    private final DtmfDetector detector;
    final DtmfDetectorListener detectorListener;

    private final Player player;
    final PlayerListener playerListener;

    // Execution Context
    private final PlayCollectContext context;

    public PlayCollect(Player player, DtmfDetector detector, Map<String, String> parameters,
            ListeningScheduledExecutorService executor) {
        super(AudioPackage.PACKAGE_NAME, SYMBOL, SignalType.TIME_OUT, parameters);
        
        // Media Components
        this.detector = detector;
        this.detectorListener = new DetectorListener();

        this.player = player;
        this.playerListener = new AudioPlayerListener();

        // Execution Context
        this.context = new PlayCollectContext(detector, detectorListener, parameters);

        // Finite State Machine
        StateMachineBuilder<PlayCollectFsm, PlayCollectState, Object, PlayCollectContext> builder = StateMachineBuilderFactory
                .<PlayCollectFsm, PlayCollectState, Object, PlayCollectContext> create(PlayCollectFsmImpl.class,
                        PlayCollectState.class, Object.class, PlayCollectContext.class, DtmfDetector.class,
                        DtmfDetectorListener.class, Player.class, PlayerListener.class, MgcpEventSubject.class,
                        ListeningScheduledExecutorService.class, PlayCollectContext.class);

        builder.onEntry(PlayCollectState.PROMPTING).callMethod("enterPrompting");
        builder.onExit(PlayCollectState.PROMPTING).callMethod("exitPrompting");
        builder.onEntry(PlayCollectState.COLLECTING).callMethod("enterCollecting");
        builder.onExit(PlayCollectState.COLLECTING).callMethod("exitCollecting");
        builder.onEntry(PlayCollectState.SUCCEEDED).callMethod("enterSucceeded");
        builder.onEntry(PlayCollectState.FAILED).callMethod("enterFailed");

        builder.transition().from(PlayCollectState.READY).to(PlayCollectState.COLLECTING).on(PlayCollectEvent.COLLECT);
        builder.transition().from(PlayCollectState.READY).to(PlayCollectState.PROMPTING).on(PlayCollectEvent.PROMPT);
        builder.internalTransition().within(PlayCollectState.PROMPTING).on(PlayCollectEvent.PLAYER_STOP).callMethod("onPrompting");
        builder.transition().from(PlayCollectState.PROMPTING).to(PlayCollectState.COLLECTING).on(PlayCollectEvent.COLLECT);
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_0).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_1).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_2).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_3).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_4).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_5).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_6).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_7).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_8).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_9).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_A).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_B).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_C).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_D).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_HASH).callMethod("onCollecting");
        builder.internalTransition().within(PlayCollectState.COLLECTING).on(DtmfToneEvent.DTMF_STAR).callMethod("onCollecting");
        builder.transition().from(PlayCollectState.COLLECTING).to(PlayCollectState.SUCCEEDED).on(PlayCollectEvent.SUCCEED);
        builder.transition().from(PlayCollectState.COLLECTING).to(PlayCollectState.FAILED).on(PlayCollectEvent.FAIL);
        builder.transition().from(PlayCollectState.COLLECTING).to(PlayCollectState.TIMING_OUT).on(PlayCollectEvent.TIME_OUT).callMethod("onTimingOut");
        builder.transition().from(PlayCollectState.COLLECTING).to(PlayCollectState.READY).on(PlayCollectEvent.RESTART).callMethod("onReady");
        builder.transition().from(PlayCollectState.TIMING_OUT).to(PlayCollectState.SUCCEEDED).on(PlayCollectEvent.SUCCEED);
        builder.transition().from(PlayCollectState.TIMING_OUT).to(PlayCollectState.FAILED).on(PlayCollectEvent.FAIL);
        builder.transition().from(PlayCollectState.TIMING_OUT).to(PlayCollectState.READY).on(PlayCollectEvent.RESTART).callMethod("onReady");
        this.fsm = builder.newStateMachine(PlayCollectState.READY, this.detector, this.detectorListener, this.player, this.playerListener, this, executor, this.context);
    }

    @Override
    protected boolean isParameterSupported(String name) {
        // Check if parameter is valid
        SignalParameters parameter = SignalParameters.fromSymbol(name);
        if (parameter == null) {
            return false;
        }

        // Check if parameter is supported
        switch (parameter) {
            case INITIAL_PROMPT:
            case REPROMPT:
            case NO_DIGITS_REPROMPT:
            case FAILURE_ANNOUNCEMENT:
            case SUCCESS_ANNOUNCEMENT:
            case NON_INTERRUPTIBLE_PLAY:
            case SPEED:
            case VOLUME:
            case CLEAR_DIGIT_BUFFER:
            case MAXIMUM_NUM_DIGITS:
            case MINIMUM_NUM_DIGITS:
            case DIGIT_PATTERN:
            case FIRST_DIGIT_TIMER:
            case INTER_DIGIT_TIMER:
            case EXTRA_DIGIT_TIMER:
            case RESTART_KEY:
            case REINPUT_KEY:
            case RETURN_KEY:
            case POSITION_KEY:
            case STOP_KEY:
            case START_INPUT_KEY:
            case END_INPUT_KEY:
            case INCLUDE_END_INPUT_KEY:
            case NUMBER_OF_ATTEMPTS:
                return true;

            default:
                return false;
        }
    }

    @Override
    public void execute() {
        if (!this.fsm.isStarted()) {
            if(this.context.getInitialPrompt().isEmpty()) {
                this.fsm.fire(PlayCollectEvent.COLLECT, this.context);
            } else {
                this.fsm.fire(PlayCollectEvent.PROMPT, this.context);
            }
        }
    }

    @Override
    public void cancel() {
        if (this.fsm.isStarted()) {
            fsm.fire(PlayCollectEvent.CANCEL, this.context);
        }
    }

    /**
     * Listens to DTMF events raised by the DTMF Detector.
     * 
     * @author Henrique Rosa (henrique.rosa@telestax.com)
     *
     */
    private final class DetectorListener implements DtmfDetectorListener {

        @Override
        public void process(DtmfEvent event) {
            final char tone = event.getTone().charAt(0);
            final DtmfToneEvent dtmfToneEvent = DtmfToneEvent.fromTone(tone);
            fsm.fire(dtmfToneEvent, PlayCollect.this.context);
        }

    }

    /**
     * Listen to Play events raised by the Player.
     * 
     * @author Henrique Rosa (henrique.rosa@telestax.com)
     *
     */
    private final class AudioPlayerListener implements PlayerListener {

        @Override
        public void process(PlayerEvent event) {
            switch (event.getID()) {
                case PlayerEvent.STOP:
                    fsm.fire(PlayCollectEvent.PLAYER_STOP, context);
                    break;
                    
                case PlayerEvent.FAILED: 
                    // TODO handle player failure
                    break;

                default:
                    break;
            }
        }
    }

}
