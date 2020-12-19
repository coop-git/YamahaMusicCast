/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yamahamusiccast.internal;

import static org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastBindingConstants.*;
import org.openhab.binding.yamahamusiccast.internal.model.Status;
import org.openhab.binding.yamahamusiccast.internal.model.ThingsRest;
import org.openhab.binding.yamahamusiccast.internal.model.DeviceInfo;
import org.openhab.binding.yamahamusiccast.internal.model.DistributionInfo;
import org.openhab.binding.yamahamusiccast.internal.model.Features;
import org.openhab.binding.yamahamusiccast.internal.model.PlayInfo;
import org.openhab.binding.yamahamusiccast.internal.model.UdpMessage;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastStateDescriptionProvider;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastConfiguration;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastBridgeHandler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.io.net.http.HttpRequestBuilder;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.config.core.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Properties;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

/**
 * The {@link yamahamusiccastHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class YamahaMusiccastHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(YamahaMusiccastHandler.class);
    //private @Nullable ScheduledFuture<?> refreshTask;
    private @Nullable ScheduledFuture<?> keepUdpEventsAliveTask;
    
    private @NonNullByDefault({}) YamahaMusiccastConfiguration config;
    private @NonNullByDefault({}) String httpResponse;
    
    JsonParser parser = new JsonParser();
    String tmpString = "";
    Integer tmpInteger = 0;
    Integer connectionTimeout = 5000;
    Integer longConnectionTimeout = 60000;
    @Nullable String responseCode = "";
    @Nullable String powerState = "";
    @Nullable String muteState = "";
    @Nullable Integer volumeState = 0;
    @Nullable Integer maxVolumeState = 0;
    @Nullable String inputState = "";
    @Nullable Integer presetNumber = 0;
    @Nullable String soundProgramState = "";
    @Nullable Integer sleepState = 0;
    String playbackState = "";
    String artistState = "";
    String trackState = "";
    String albumState = "";
    String albumArtUrlState = "";
    String repeatState = "";
    String shuffleState = "";
    String topicAVR = "";
    @NonNullByDefault({}) String zone = "main";
    String channelWithoutGroup = "";
    @NonNullByDefault({}) String thingLabel = "";
    @NonNullByDefault({}) String mclinkSetupServer = "";
    String url = "";
    String json = "";
    String action= "";
    Integer zoneNum = 0;
    String groupId = "";
    String role = "";

    public String deviceId = "";
    

    private YamahaMusiccastStateDescriptionProvider stateDescriptionProvider;
    
    public YamahaMusiccastHandler(Thing thing, YamahaMusiccastStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;       
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) { 
        if (command instanceof RefreshType) {
            //nothing here
        } else  {
            logger.info("Handling command {} for channel {}", command, channelUID);
            channelWithoutGroup = channelUID.getIdWithoutGroup();
            zone = channelUID.getGroupId();
            switch (channelWithoutGroup) {
                case CHANNEL_POWER:
                    if (command.equals(OnOffType.ON)) {
                        httpResponse = setPower("on", zone);
                        tmpString = getResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF); 
                        }
                    } else if (command.equals(OnOffType.OFF)) {
                        httpResponse = setPower("standby", zone);
                        tmpString = getResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.ON); 
                        }
                    }
                    break;  
                case CHANNEL_MUTE:
                    if (command.equals(OnOffType.ON)) {
                        httpResponse = setMute("true", zone);
                        tmpString = getResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF); 
                        }
                    } else if (command.equals(OnOffType.OFF)) {
                        httpResponse = setMute("false", zone);
                        tmpString = getResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.ON); 
                        }
                    }
                    break;                  
                case CHANNEL_VOLUME:
                    tmpString = command.toString();
                    tmpString = tmpString.replace(".0","");
                    try {
                        tmpInteger = Integer.parseInt(tmpString);
                        tmpInteger = (maxVolumeState * tmpInteger)/100;
                        logger.debug("Pushed Volume:{} - Calculated Volume:{}", tmpString, tmpInteger);
                        setVolume(tmpInteger, zone);
                        if (config.configSyncVolume == true ) {
                            tmpString = getDistributionInfo(config.configHost);
                            DistributionInfo targetObject = new DistributionInfo();
                            targetObject = new Gson().fromJson(tmpString, DistributionInfo.class);
                            role = targetObject.getRole();
                            if (role.equals("server")) {
                                logger.info("Volume mc_link: {}", tmpString);
                            }
                        } // END configSyncVolume
                        

                    } catch (Exception e) {
                        //Wait for refresh
                    }                    
                    break;
                case CHANNEL_VOLUMEABS:
                    tmpString = command.toString();
                    tmpString = tmpString.replace(".0","");
                    try {
                        tmpInteger = Integer.parseInt(tmpString);
                        setVolume(tmpInteger, zone);
                    } catch (Exception e) {
                        //Wait for refresh
                    }                    
                    break;
                case CHANNEL_INPUT:
                    setInput(command.toString(), zone);
                    break;
                case CHANNEL_SOUNDPROGRAM:
                    setSoundProgram(command.toString(), zone);
                    break;
                case CHANNEL_SELECTPRESET:
                    setPreset(command.toString(), zone);
                    break;
                case CHANNEL_PLAYER:
                    if (command.equals(PlayPauseType.PLAY)) {
                        setPlayback("play");
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        setPlayback("pause");
                    } else if (command.equals(NextPreviousType.NEXT)) {
                        setPlayback("next");
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        setPlayback("previous");
                    } else if (command.equals(RewindFastforwardType.REWIND)) {
                        setPlayback("fast_reverse_start");
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        setPlayback("fast_forward_end");
                    }
                    break;
                case CHANNEL_SLEEP:
                    setSleep(command.toString(), zone);
                    break;
                case CHANNEL_MCSERVER:
                    action = "";
                    json = "";
                    InputStream is2 = new ByteArrayInputStream(json.getBytes());

                    if (command.toString().equals("")) {
                        action="unlink";
                        role="";
                        groupId="";
                    } else {
                        action="link";
                        String[] parts2 = command.toString().split("#");
                        mclinkSetupServer = parts2[0];
                        tmpString = getDistributionInfo(mclinkSetupServer);
                        DistributionInfo targetObject = new DistributionInfo();
                        targetObject = new Gson().fromJson(tmpString, DistributionInfo.class);
                        responseCode = targetObject.getResponseCode();
                        
                        role = targetObject.getRole();
                        if (role.equals("server")) {
                            groupId = targetObject.getGroupId();
                        } else if (role.equals("client")) {
                            // error geven
                            groupId = "";
                        } else if (role.equals("none")) {
                            groupId = generateGroupId();
                        }                            
                    }
                    
                    if (action.equals("unlink")) {
                        json = "{\"group_id\":\"\"}";
                        httpResponse = setClientInfo(config.configHost,json);
                    } else if (action.equals("link")) { 
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":\"" + zone + "\", \"type\":\"add\", \"client_list\":[\"" + config.configHost + "\"]}";
                        httpResponse = setServerInfo(mclinkSetupServer, json);
                        // All zones of Model are required for MC Link
                        tmpString = "";
                        for (int i = 1; i <= zoneNum; i++) {
                            switch (i) {
                                case 1:
                                    tmpString = "\"main\"";
                                    break;
                                case 2:
                                    tmpString = tmpString + ", \"zone2\"";
                                    break;
                                case 3:
                                    tmpString = tmpString + ", \"zone3\"";
                                    break;
                                case 4:
                                    tmpString = tmpString + ", \"zone4\"";
                                    break;
                            }
                        }
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":[" + tmpString + "]}";
                        logger.info("setClientInfo json: {}", json);
                        httpResponse = setClientInfo(config.configHost, json);
                        httpResponse = startDistribution(mclinkSetupServer);
                    }
                    break;
                case CHANNEL_UNLINKMCSERVER:
                        if (command.equals(OnOffType.ON)) {
                            json = "{\"group_id\":\"\"}";
                            httpResponse = setServerInfo(config.configHost, json);
                            updateState(channelUID, OnOffType.OFF); 
                        }
                    break;
                case CHANNEL_RECALLSCENE:
                    recallScene(command.toString(), zone);
                    break;
                case CHANNEL_REPEAT:
                    setRepeat(command.toString());
                    break;
                case CHANNEL_SHUFFLE:
                    setShuffle(command.toString());
                    break;
            }  // END Switch Channel          
        }
    }

    @Override
    public void initialize() {
        //Needed as extra parameters
        // * Max Volume
        // * Number of Zones
        // * Presets

        thingLabel = thing.getLabel();
        logger.info("YXC - Start initializing! - {}", thingLabel);
        this.config = getConfigAs(YamahaMusiccastConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        if (config.configHost.equals("")) {
            logger.info("YXC - No host found");
        } else {
            zoneNum = getNumberOfZones(config.configHost);
            logger.info("YXC - Zones found: {} - {}", zoneNum,thingLabel);
            
            if (zoneNum > 0) {
                refreshOnStartup();
                keepUdpEventsAliveTask = scheduler.scheduleWithFixedDelay(this::keepUdpEventsAlive, 5, 300,TimeUnit.SECONDS);
                logger.info("YXC - Start Keep Alive UDP events (5 minutes - {}) ", thingLabel);
                                
                updateStatus(ThingStatus.ONLINE);
                logger.info("YXC - Finished initializing! - {}", thingLabel);    
            } else {
                updateStatus(ThingStatus.OFFLINE);
                logger.info("YXC - Not initialized! - {}", thingLabel);        
            }
        }
    }

    private void refreshOnStartup() {
        for (int i = 1; i <= zoneNum; i++) {
            switch (i) {
                case 1:
                    updateStatusZone("main");
                    break;
                case 2:
                    updateStatusZone("zone2");
                    break;
                case 3:
                    updateStatusZone("zone3");
                    break;
                case 4:
                    updateStatusZone("zone4");
                    break;
            }
        }
        updatePresets(0);
        fetchOtherDevices();
        updateNetUSBPlayer();
        fetchOhterDevices2();
    }

    @Override
    public void dispose() { 
        if (keepUdpEventsAliveTask != null) {
            keepUdpEventsAliveTask.cancel(true);
        }
        
    }
    // Various functions 
    public void processUDPEvent (String json) {
        logger.debug("UDP package: {}", json);
        UdpMessage targetObject = new UdpMessage();
        ChannelUID channel;
        String zoneToUpdate;
        String jsonMain;
        String jsonZone2;
        String jsonZone3;
        String jsonZone4;
        String netUsb;

        targetObject = new Gson().fromJson(json, UdpMessage.class);
        try {
            jsonMain = targetObject.getMain().toString();
            if (!jsonMain.equals("")) {
                updateStateFromUDPEvent("main", targetObject);
            }
        } catch (Exception e) {
            //logger.warn("Could not update state via UDP event");
        }

        try {
            jsonZone2 = targetObject.getZone2().toString();
            if (!jsonZone2.equals("")) {
                updateStateFromUDPEvent("zone2", targetObject);
            }
        } catch (Exception e) {
            //logger.warn("Could not update state via UDP event");
        }

        try {
            jsonZone3 = targetObject.getZone3().toString();
            if (!jsonZone3.equals("")) {
                updateStateFromUDPEvent("zone3", targetObject);
            }
        } catch (Exception e) {
            //logger.warn("Could not update state via UDP event");
        }

        try {
            jsonZone4 = targetObject.getZone4().toString();
            if (!jsonZone4.equals("")) {
                updateStateFromUDPEvent("zone4", targetObject);
            }
        } catch (Exception e) {
            //logger.warn("Could not update state via UDP event");
        }

        try {
            netUsb = targetObject.getNetUSB().toString();
            if (!netUsb.equals("")) {
                updateStateFromUDPEvent("netusb", targetObject);
            }
        } catch (Exception e) {
            //logger.warn("Could not update state via UDP event");
        }

    }

    private void updateStateFromUDPEvent(String zoneToUpdate, UdpMessage targetObject) {
        ChannelUID channel;
        String playInfoUpdated = "";
        String statusUpdated = "";
        String powerState = "";
        String muteState="";
        String inputState = "";
        Integer volumeState = 0;
        Integer presetNumber = 0;
        logger.debug("YXC - Handling UDP for {}", zoneToUpdate);       
        switch (zoneToUpdate) {
            case "main":
                try {
                    powerState = targetObject.getMain().getPower();
                } catch (Exception e) {
                    powerState = "";
                }
                try {
                    muteState = targetObject.getMain().getMute();
                } catch (Exception e) {
                    muteState = "";
                }
                try {
                    inputState = targetObject.getMain().getInput();
                } catch (Exception e) {
                    inputState = "";
                }
                try {
                    volumeState = targetObject.getMain().getVolume();
                } catch (Exception e) {
                    volumeState = 0;
                }
                try {
                    statusUpdated = targetObject.getMain().getstatusUpdated();
                } catch (Exception e) {
                    statusUpdated = "";
                }
                break;
            case "zone2":
                try {
                    powerState = targetObject.getZone2().getPower();
                } catch (Exception e) {
                    powerState = "";
                }
                try {
                    muteState = targetObject.getZone2().getMute();
                } catch (Exception e) {
                    muteState = "";
                }
                try {
                    inputState = targetObject.getZone2().getInput();
                } catch (Exception e) {
                    inputState = "";
                }
                try {
                    volumeState = targetObject.getZone2().getVolume();
                } catch (Exception e) {
                    volumeState = 0;
                }    
                try {
                    statusUpdated = targetObject.getZone2().getstatusUpdated();
                } catch (Exception e) {
                    statusUpdated = "";
                }            
                break;
            case "zone3":
                try {
                    powerState = targetObject.getZone3().getPower();
                } catch (Exception e) {
                    powerState = "";
                }
                try {
                    muteState = targetObject.getZone3().getMute();
                } catch (Exception e) {
                    muteState = "";
                }
                try {
                    inputState = targetObject.getZone3().getInput();
                } catch (Exception e) {
                    inputState = "";
                }
                try {
                    volumeState = targetObject.getZone3().getVolume();
                } catch (Exception e) {
                    volumeState = 0;
                }
                try {
                    statusUpdated = targetObject.getZone3().getstatusUpdated();
                } catch (Exception e) {
                    statusUpdated = "";
                }                         
                break;
            case "zone4":
                try {
                    powerState = targetObject.getZone4().getPower();
                } catch (Exception e) {
                    powerState = "";
                }
                try {
                    muteState = targetObject.getZone4().getMute();
                } catch (Exception e) {
                    muteState = "";
                }
                try {
                    inputState = targetObject.getZone4().getInput();
                } catch (Exception e) {
                    inputState = "";
                }
                try {
                    volumeState = targetObject.getZone4().getVolume();
                } catch (Exception e) {
                    volumeState = 0;
                }
                try {
                    statusUpdated = targetObject.getZone4().getstatusUpdated();
                } catch (Exception e) {
                    statusUpdated = "";
                }                         
                break;
            case "netusb":
                try {
                    presetNumber = targetObject.getNetUSB().getPresetControl().getNum();
                } catch (Exception e) {
                    presetNumber = 0;
                }
                try {
                    playInfoUpdated = targetObject.getNetUSB().getPlayInfoUpdated();
                    //logger.info("netusb case: {}", playInfoUpdated);
                } catch (Exception e) {
                    playInfoUpdated = "";
                }
                break;
        }

        if (!powerState.equals("")) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, "channelPower");
            if (isLinked(channel)) {
                if (powerState.equals("on")) {                  
                    updateState(channel, OnOffType.ON); 
                } else if (powerState.equals("standby")) {
                    updateState(channel, OnOffType.OFF);
                }
            }
        }

        if (!muteState.equals("")) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, "channelMute");
            if (isLinked(channel)) {
                if (muteState.equals("true")) {                  
                    updateState(channel, OnOffType.ON); 
                } else if (muteState.equals("false")) {
                    updateState(channel, OnOffType.OFF);
                }
            }
        }

        if (!inputState.equals("")) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, "channelInput");
            if (isLinked(channel)) {
                updateState(channel, StringType.valueOf(inputState)); 
            }
        }

        if (!volumeState.equals(0)) {
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, "channelVolume");
            if (isLinked(channel)) {
                updateState(channel, new PercentType((volumeState * 100) / maxVolumeState));
            }
            channel = new ChannelUID(getThing().getUID(), zoneToUpdate, "channelVolumeAbs");
            if (isLinked(channel)) {
                updateState(channel, new DecimalType(volumeState));
            }
        }

        if (!presetNumber.equals(0)) {
            logger.debug("Preset detected: {}", presetNumber);
            updatePresets(presetNumber);
        }

        if (playInfoUpdated.equals("true")) {
            updateNetUSBPlayer();
        }

        if (!statusUpdated.equals("")) {
            updateStatusZone(zoneToUpdate);
        }
    } 

    private void updateStatusZone(String zoneToUpdate) {
        tmpString = getStatus(zoneToUpdate);
        try {
            Status targetObject = new Status();
            targetObject = new Gson().fromJson(tmpString, Status.class);
            responseCode = targetObject.getResponseCode();
            powerState = targetObject.getPower();
            muteState = targetObject.getMute();
            volumeState = targetObject.getVolume();
            maxVolumeState = targetObject.getMaxVolume();
            inputState = targetObject.getInput();
            soundProgramState = targetObject.getSoundProgram();
            sleepState = targetObject.getSleep();
 
        } catch (Exception e) {
            responseCode = "999";
        }
        
        switch (responseCode) {
            case "0":
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();
                    channelWithoutGroup = channelUID.getIdWithoutGroup();
                    zone = channelUID.getGroupId();
                    if (isLinked(channelUID)) {
                        switch (channelWithoutGroup) { //channelUID.getId()
                            case CHANNEL_POWER:
                                if (powerState.equals("on")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.ON); 
                                    }
                                } else if (powerState.equals("standby")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.OFF);
                                    }
                                }
                                break; 
                            case CHANNEL_MUTE:
                                if (muteState.equals("true")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.ON); 
                                    }
                                } else if (muteState.equals("false")) {
                                    if (zone.equals(zoneToUpdate)) {
                                        updateState(channelUID, OnOffType.OFF);
                                    }
                                }
                                break;
                            case CHANNEL_VOLUME:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new PercentType((volumeState * 100) / maxVolumeState));
                                }   
                                break;
                            case CHANNEL_VOLUMEABS:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new DecimalType(volumeState));
                                }   
                                break;                                
                            case CHANNEL_INPUT:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, StringType.valueOf(inputState));
                                }
                                break;
                            case CHANNEL_SOUNDPROGRAM:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, StringType.valueOf(soundProgramState));
                                }   
                                break;
                            case CHANNEL_SLEEP:
                                if (zone.equals(zoneToUpdate)) {
                                    updateState(channelUID, new DecimalType(sleepState));
                                }   
                                break;
                        } //END switch (channelWithoutGroup)
                    } //END IsLinked
                }    
                break;
            case "999":
                    logger.info("YXC - Nothing to do! - {} ({})", thingLabel, zoneToUpdate);
                break;
        }
    }

    private void updatePresets(Integer value) {
        String inputText = "";
        tmpString = getPresetInfo(); // Without zone
        Integer presetCounter = 0;
        Integer currentPreset = 0;
        try {
            JsonElement jsonTree = parser.parse(tmpString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonArray presetsArray = jsonObject.getAsJsonArray("preset_info");
            List<StateOption> optionsPresets = new ArrayList<>();
            inputText = getLastInput(); // Without zone
            for (JsonElement pr : presetsArray) {
                presetCounter = presetCounter + 1;
                JsonObject presetObject = pr.getAsJsonObject();
                String text = presetObject.get("text").getAsString();
                if (!text.equals("")) {
                    optionsPresets.add(new StateOption(presetCounter.toString(), "#" + presetCounter.toString() + " " + text));                
                    if (inputText.equals(text)) {
                        currentPreset = presetCounter;
                    }
                }
            }
            if (!value.equals(0)) {currentPreset = value;}
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                channelWithoutGroup = channelUID.getIdWithoutGroup();
                if (isLinked(channelUID)) {
                    switch (channelWithoutGroup) {
                        case CHANNEL_SELECTPRESET:
                            stateDescriptionProvider.setStateOptions(channelUID, optionsPresets);
                            updateState(channelUID,StringType.valueOf(currentPreset.toString()));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Something went wrong with fetching Presets");
        } 
    }

    private void updateNetUSBPlayer() {
        tmpString = getPlayInfo();
        try {
            PlayInfo targetObject = new PlayInfo();
            targetObject = new Gson().fromJson(tmpString, PlayInfo.class);
            responseCode = targetObject.getResponseCode();
            playbackState = targetObject.getPlayback();
            artistState = targetObject.getArtist();
            trackState = targetObject.getTrack();
            albumState = targetObject.getAlbum();
            albumArtUrlState = targetObject.getAlbumArtUrl();
            repeatState = targetObject.getRepeat();
            shuffleState = targetObject.getShuffle();
        } catch (Exception e) {
            responseCode = "999";
        }        

        if (responseCode.equals("0")) {
            ChannelUID testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelPlayer");
            switch (playbackState) {
                case "play":
                    updateState(testchannel,PlayPauseType.PLAY);
                    break;
                case "stop":
                    updateState(testchannel,PlayPauseType.PAUSE);
                    break;
                case "pause":
                    updateState(testchannel,PlayPauseType.PAUSE);
                    break;
                case "fast_reverse":
                    updateState(testchannel,RewindFastforwardType.REWIND);
                    break;
                case "fast_forward":
                    updateState(testchannel,RewindFastforwardType.FASTFORWARD);
                    break;
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelArtist");
            if (isLinked(testchannel)){
                updateState(testchannel, StringType.valueOf(artistState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelTrack");
            if (isLinked(testchannel)){
                updateState(testchannel, StringType.valueOf(trackState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelAlbum");
            if (isLinked(testchannel)){
                updateState(testchannel, StringType.valueOf(albumState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelAlbumArt");
            if (isLinked(testchannel)){
                albumArtUrlState = "http://" + config.configHost + albumArtUrlState;
                updateState(testchannel, StringType.valueOf(albumArtUrlState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelRepeat");
            if (isLinked(testchannel)){
                updateState(testchannel, StringType.valueOf(repeatState));
            }
            testchannel = new ChannelUID(getThing().getUID(), "playerControls", "channelShuffle");
            if (isLinked(testchannel)){
                updateState(testchannel, StringType.valueOf(shuffleState));
            }
        }

    }

    private String getResponseCode(String json) {
        JsonElement jsonTree = parser.parse(json);
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        return jsonObject.get("response_code").getAsString();
    }

    private String getLastInput() {
        String text = "";
        tmpString = getRecentInfo();
        responseCode = getResponseCode(tmpString);
        if (responseCode.equals("0")) {
            JsonElement jsonTree = parser.parse(tmpString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonArray recentsArray = jsonObject.getAsJsonArray("recent_info");
            for (JsonElement re : recentsArray) {
                JsonObject recentObject = re.getAsJsonObject();                
                text = recentObject.get("text").getAsString();
                break;
            }
        }
        return text;
    }

    private void fetchOhterDevices2 () {
        // Bridge bridge = thing.getBridge();
        // for (Thing thing : bridge.getThings()) {
        //     YamahaMusiccastHandler handler = (YamahaMusiccastHandler) thing.getHandler();
        //     logger.info("Bridge: {} - {})", handler.getDeviceId(), thing.getLabel());
        // }

    }

    private void fetchOtherDevices() {
        Integer zonesPerHost = 1;
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://127.0.0.1:8080/rest/things", longConnectionTimeout);               
            List<StateOption> options = new ArrayList<>();
            Gson gson = new Gson(); 
            ThingsRest[] resultArray = gson.fromJson(httpResponse, ThingsRest[].class);
            
            for(ThingsRest result : resultArray) {
                if (result.getThingTypeUID().equals("yamahamusiccast:Device")) {
                    String label = result.getLabel();
                    JsonObject jsonObject = result.getConfiguration();
                    String host = jsonObject.get("configHost").getAsString();
                    zonesPerHost = getNumberOfZones(host);
                    for (int i = 1; i <= zonesPerHost; i++) {
                        switch (i) {
                            case 1:
                                options.add(new StateOption(host + "#" + "main", label + "#main"));                    
                                break;
                            case 2:
                                options.add(new StateOption(host + "#" + "zone2", label + "#zone2"));
                                break;
                            case 3:
                                options.add(new StateOption(host + "#" + "zone3", label + "#zone3"));
                                break;
                            case 4:
                                options.add(new StateOption(host + "#" + "zone4", label + "#zone4"));
                                break;
                        }
                    }
                }
            }
            options.add(new StateOption("", ""));

            //for each zone of the device set all the possible combinations
            for (int i = 1; i <= zoneNum; i++) {
                switch (i) {
                    case 1:
                        ChannelUID testchannel = new ChannelUID(getThing().getUID(), "main", "channelMCServer");
                        if (isLinked(testchannel)) {
                            stateDescriptionProvider.setStateOptions(testchannel, options);
                        }
                        break;
                    case 2:
                        testchannel = new ChannelUID(getThing().getUID(), "zone2", "channelMCServer");
                        if (isLinked(testchannel)) {
                            stateDescriptionProvider.setStateOptions(testchannel, options);
                        }
                        break;
                    case 3:
                        testchannel = new ChannelUID(getThing().getUID(), "zone3", "channelMCServer");
                        if (isLinked(testchannel)) {
                            stateDescriptionProvider.setStateOptions(testchannel, options);
                        }
                        break;
                    case 4:
                        testchannel = new ChannelUID(getThing().getUID(), "zone4", "channelMCServer");
                        if (isLinked(testchannel)) {
                            stateDescriptionProvider.setStateOptions(testchannel, options);
                        }
                        break;
                }
            }

 
        } catch (IOException e) {
        }
    }

    private String generateGroupId () {
        return UUID.randomUUID().toString().replace("-","").substring(0,32);
    }

    private Integer getNumberOfZones(String host) {
        try {
            tmpString = getFeatures(host);
            Features targetObject = new Features();
            targetObject = new Gson().fromJson(tmpString, Features.class);
            return Integer.valueOf(targetObject.getSystem().getZoneNum());
        } catch (Exception e) {
            logger.warn("Error fetching zones");
            return 0;
        }
    }

    public String getDeviceId() {
        try {
            tmpString = getDeviceInfo();
            DeviceInfo targetObject = new DeviceInfo();
            targetObject = new Gson().fromJson(tmpString, DeviceInfo.class);
            return targetObject.getDeviceId();
        } catch (Exception e) {
            logger.warn("Error fetching Device Id");
            return "";
        }

    }
    // End Various functions

    // API calls to AVR

    // Start Zone Related


    private String getStatus(String zone) {
        topicAVR = "Status";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/getStatus", connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPower(String value, String zone) {
        topicAVR = "Power";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setPower?power=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setMute(String value, String zone) {
        topicAVR = "Mute";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setMute?enable=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setVolume(Integer value, String zone) {
        topicAVR = "Volume";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setVolume?volume=" + value, connectionTimeout);
            logger.info("{}", httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setInput(String value, String zone) {
        topicAVR = "setInput";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setInput?input=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSoundProgram(String value, String zone) {
        topicAVR = "setSoundProgram";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setSoundProgram?program=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPreset(String value, String zone) {
        topicAVR = "setPreset";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/recallPreset?zone=" + zone + "&num=" + value, longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSleep(String value, String zone) {
        topicAVR = "setSleep";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/setSleep?sleep=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String recallScene(String value, String zone) {
        topicAVR = "recallScene";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/" + zone + "/recallScene?num=" + value, connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }
    // End Zone Related

    // Start Net Radio/USB Related

    private String getPresetInfo() {
        topicAVR = "PresetInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v2/netusb/getPresetInfo", longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse; 
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getRecentInfo() {
        topicAVR = "RecentInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/getRecentInfo", longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getPlayInfo() {
        topicAVR = "PlayInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/getPlayInfo", longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPlayback(String value) {
        topicAVR = "Playback";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/setPlayback?playback=" + value, longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setRepeat(String value) {
        topicAVR = "Repeat";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/setRepeat?mode=" + value, longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setShuffle(String value) {
        topicAVR = "Shuffle";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/netusb/setShuffle?mode=" + value, longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    // End Net Radio/USB Related

    // Start Music Cast API calls
    private String getDistributionInfo(String host) {
        topicAVR = "DistributionInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + host + "/YamahaExtendedControl/v1/dist/getDistributionInfo", connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setServerInfo(String host, String json) {
        InputStream is = new ByteArrayInputStream(json.getBytes());
        topicAVR = "SetServerInfo";
        try {
            url = "http://" + host + "/YamahaExtendedControl/v1/dist/setServerInfo";
            httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout); 
            logger.debug("MC Link/Unlink Server {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }
    
    private String setClientInfo(String host, String json) {
        InputStream is = new ByteArrayInputStream(json.getBytes());
        topicAVR = "SetClientInfo";
        try {
            url = "http://" + host + "/YamahaExtendedControl/v1/dist/setClientInfo";
            httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout); 
            logger.debug("MC Link/Unlink Client {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private String startDistribution(String host) {
        topicAVR = "StartDistribution";
        try {
            url = "http://" + host + "/YamahaExtendedControl/v1/dist/startDistribution?num=1";
            httpResponse = HttpUtil.executeUrl("GET", url, longConnectionTimeout);
            logger.debug("MC Start Distribution {}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    // End Music Cast API calls

    // Start General/System API calls
    private String getFeatures(String host) {
        topicAVR = "Features";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + host + "/YamahaExtendedControl/v1/system/getFeatures", longConnectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }
    
    private String getDeviceInfo() {
        topicAVR = "DeviceInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/system/getDeviceInfo", connectionTimeout);
            logger.debug("{}", httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}", topicAVR, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }

    private void keepUdpEventsAlive() {
        Properties appProps = new Properties();
        appProps.setProperty("X-AppName", "MusicCast/1");
        appProps.setProperty("X-AppPort", "41100");
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.configHost + "/YamahaExtendedControl/v1/system/getDeviceInfo", appProps, null, "",connectionTimeout);
            logger.debug("{}", httpResponse);
        } catch (IOException e) {
            logger.warn("UDP refresh failed - {}", e.toString());
        }
    }

    // End General/System API calls
}
 