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
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastStateDescriptionProvider;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastConfiguration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.PercentType;



import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
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
    private @Nullable ScheduledFuture<?> refreshTask;
    private @NonNullByDefault({}) YamahaMusiccastConfiguration config;
    private @NonNullByDefault({}) String httpResponse;
    
    JsonParser parser = new JsonParser();
    String tmpString = "";
    Integer tmpInteger = 0;
    Integer ConnectionTimeout = 5000;
    Integer LongConnectionTimeout = 60000;
    String ResponseCode = "";
    String PowerState = "";
    String MuteState = "";
    Integer VolumeState = 0;
    Integer MaxVolumeState = 0;
    String InputState = "";
    String InputText = "";
    Integer PresetNumber = 0;
    String SoundProgramState = "";
    String PresetState = "";
    String PresetStateTuner = "";
    String ListPresetsState = "";
    String PlayerState = "";
    String TopicAVR = "";
    String Zone = "main";
    String Channel = "";
    String ZoneChannelCombo = "";
    Integer SleepState = 0;
    @NonNullByDefault({}) String ThingLabel = "";

    private YamahaMusiccastStateDescriptionProvider stateDescriptionProvider;
    
    public YamahaMusiccastHandler(Thing thing, YamahaMusiccastStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        
        if (command instanceof RefreshType) {
            //refreshProcess();
        } else  {
            logger.info("Handling command {} for channel {}", command, channelUID);
            //ZoneChannelCombo = channelUID.getId();
            //Zone = GetZoneFromChannelID(ZoneChannelCombo);
            //Channel = GetChannelFromChannelID(ZoneChannelCombo);
            Channel = channelUID.getIdWithoutGroup();
            switch (Channel) { //channelUID.getId()
                case CHANNEL_POWER:
                    if (command.equals(OnOffType.ON)) {
                        httpResponse = setPower("on", Zone);
                        tmpString = GetResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF); 
                        }
                    } else if (command.equals(OnOffType.OFF)) {
                        httpResponse = setPower("standby", Zone);
                        tmpString = GetResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.ON); 
                        }
                    }
                    break;  
                case CHANNEL_MUTE:
                    if (command.equals(OnOffType.ON)) {
                        httpResponse = setMute("true", Zone);
                        tmpString = GetResponseCode(httpResponse);
                        if (!tmpString.equals("0")) {
                            updateState(channelUID, OnOffType.OFF); 
                        }
                    } else if (command.equals(OnOffType.OFF)) {
                        httpResponse = setMute("false", Zone);
                        tmpString = GetResponseCode(httpResponse);
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
                        tmpInteger = (MaxVolumeState * tmpInteger)/100;
                        if (config.config_FullLogs == true) {
                            logger.info("Pushed Volume:" + tmpString + "/Calculated Volume:" + tmpInteger);
                        }    
                        setVolume(tmpInteger, Zone);
                    } catch (Exception e) {
                        //Wait for refresh
                    }                    
                    break;
                case CHANNEL_INPUT:
                    tmpString = command.toString();
                    if (config.config_FullLogs == true) {
                        logger.info("setInput:" + tmpString);
                    }
                    setInput(tmpString, Zone);
                    break;
                case CHANNEL_SOUNDPROGRAM:
                    tmpString = command.toString();
                    if (config.config_FullLogs == true) {
                        logger.info("setSoundProgram:" + tmpString);
                    }
                    setSoundProgram(tmpString, Zone);
                    break;
                case CHANNEL_SELECTPRESET:
                    tmpString = command.toString();
                    if (config.config_FullLogs == true) {
                        logger.info("setPreset:" + tmpString);
                    }
                    setPreset(tmpString, Zone);
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
                    tmpString = command.toString();
                    setSleep(tmpString, Zone);
                    break;
            }            
        }
    }

    @Override
    public void initialize() {
        ThingLabel = thing.getLabel();
        logger.info("YXC - Start initializing! - {}", ThingLabel);
        this.config = getConfigAs(YamahaMusiccastConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        if (config.config_host.equals("")) {
            logger.info("YXC - No host found");
        } else {
            if (config.config_refreshInterval > 0) {
                startAutomaticRefresh();
            }
            updateStatus(ThingStatus.ONLINE);
            logger.info("YXC - Finished initializing! - {}", ThingLabel);    
        }
    }


    private void startAutomaticRefresh() {
        refreshTask = scheduler.scheduleWithFixedDelay(this::refreshProcess, 0, config.config_refreshInterval,TimeUnit.SECONDS);
        logger.info("YXC - Start automatic refresh ({} seconds - {}) ", config.config_refreshInterval,ThingLabel);
    }

    private void refreshProcess() {
        // Zone main is always present        
        UpdateStatusZone("main");
        if (config.config_Zone2 == true) {
            UpdateStatusZone("zone2");
        }
        if (config.config_Zone3 == true) {
            UpdateStatusZone("zone3");
        }
        if (config.config_Zone4 == true) {
            UpdateStatusZone("zone4");
        }
        //Not Zone related
        UpdatePresets();
        fetchOtherDevices();
    }

    @Override
    public void dispose() { 
            refreshTask.cancel(true);
    }
    // Various functions 
    private void UpdateStatusZone(String ZoneToUpdate) {
        tmpString = getStatus(ZoneToUpdate);
        try {
            //JsonElement jsonTree = parser.parse(tmpString);
            //JsonObject jsonObject = jsonTree.getAsJsonObject();
            //ResponseCode = jsonObject.get("response_code").getAsString();
            //PowerState = jsonObject.get("power").getAsString();
            //MuteState = jsonObject.get("mute").getAsString();
            //VolumeState = jsonObject.get("volume").getAsInt();
            //MaxVolumeState = jsonObject.get("max_volume").getAsInt();
            //InputState = jsonObject.get("input").getAsString();
            //SoundProgramState = jsonObject.get("sound_program").getAsString();
            //PresetState = jsonObject.get("input").getAsString(); // TODO : still needed?
            //InputText = "";

            Status targetObject = new Status();
            targetObject = new Gson().fromJson(tmpString, Status.class);
            ResponseCode = targetObject.getResponseCode();
            PowerState = targetObject.getPower();
            MuteState = targetObject.getMute();
            VolumeState = targetObject.getVolume();
            MaxVolumeState = targetObject.getMaxVolume();
            InputState = targetObject.getInput();
            SoundProgramState = targetObject.getSoundProgram();
            SleepState = targetObject.getSleep();
 
        } catch (Exception e) {
            ResponseCode = "999";
        }
        
        switch (ResponseCode) {
            case "0":
                for (Channel channel : getThing().getChannels()) {
                    ChannelUID channelUID = channel.getUID();

                    // = channelUID.getId();
                    //Zone = GetZoneFromChannelID(ZoneChannelCombo);
                    //Channel = GetChannelFromChannelID(ZoneChannelCombo);
                    Channel = channelUID.getIdWithoutGroup();
                    switch (Channel) { //channelUID.getId()
                        case CHANNEL_POWER:
                            if (PowerState.equals("on")) {
                                if (Zone.equals(ZoneToUpdate)) {
                                    updateState(channelUID, OnOffType.ON); 
                                }
                            } else if (PowerState.equals("standby")) {
                                if (Zone.equals(ZoneToUpdate)) {
                                    updateState(channelUID, OnOffType.OFF);
                                }
                            }
                            break; 
                        case CHANNEL_MUTE:
                            if (MuteState.equals("true")) {
                                if (Zone.equals(ZoneToUpdate)) {
                                    updateState(channelUID, OnOffType.ON); 
                                }
                            } else if (MuteState == "false") {
                                if (Zone.equals(ZoneToUpdate)) {
                                    updateState(channelUID, OnOffType.OFF);
                                }
                            }
                            break;
                        case CHANNEL_VOLUME:
                            if (Zone.equals(ZoneToUpdate)) {
                                updateState(channelUID, new PercentType((VolumeState * 100) / MaxVolumeState));
                            }   
                            break;
                        case CHANNEL_INPUT:
                            if (Zone.equals(ZoneToUpdate)) {
                                updateState(channelUID, StringType.valueOf(InputState));
                            }
                            break;
                        case CHANNEL_SOUNDPROGRAM:
                            if (Zone.equals(ZoneToUpdate)) {
                                updateState(channelUID, StringType.valueOf(SoundProgramState));
                            }   
                            break;
                        case CHANNEL_SLEEP:
                            if (Zone.equals(ZoneToUpdate)) {
                                updateState(channelUID, new DecimalType(SleepState));
                            }   
                            break;
                        }
                }    
                break;
            case "999":
                if (config.config_FullLogs == true) {
                    logger.info("YXC - Nothing to do! - {} ({})", ThingLabel, ZoneToUpdate);
                }   
                break;
        }
    }

    private void UpdatePresets() {
        InputText = GetLastInput(); // Without zone
        tmpString = getPresetInfo(); // Without zone
        try {
            JsonElement jsonTree = parser.parse(tmpString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonArray presetsArray = jsonObject.getAsJsonArray("preset_info");
            tmpInteger = 0;
            tmpString = "";
            PresetNumber = 0;
            List<StateOption> optionsPresets = new ArrayList<>();
            for (JsonElement pr : presetsArray) {
                tmpInteger = tmpInteger + 1;
                JsonObject presetObject = pr.getAsJsonObject();
                String Input = presetObject.get("input").getAsString();
                String Text = presetObject.get("text").getAsString();
                if (!Text.equals("")) {
                    //tmpString = tmpString + tmpInteger + ":" + Text + " | ";
                    optionsPresets.add(new StateOption(tmpInteger.toString(), Text));                
                    if (InputText.equals(Text)) {
                        PresetNumber = tmpInteger;
                    }
                }
            }
            //ListPresetsState = tmpString;
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                Channel = channelUID.getIdWithoutGroup();
                switch (Channel) { //channelUID.getId()
                    //case CHANNEL_PRESETS:
                    //    updateState(channelUID,StringType.valueOf(ListPresetsState));
                    //    break;
                    case CHANNEL_SELECTPRESET:
                        stateDescriptionProvider.setStateOptions(channelUID, optionsPresets);
                        updateState(channelUID,StringType.valueOf(PresetNumber.toString()));
                        break;
                }
            }
        } catch (Exception e) {
            logger.info("Something went wrong with fetching Presets");
        } 
    }

    private String GetResponseCode(String json) {
        JsonElement jsonTree = parser.parse(json);
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        return jsonObject.get("response_code").getAsString();
    }

    private String GetZoneFromChannelID(String Value) {
        String[] parts = Value.split("#");
        return parts[0];
    }

    private String GetChannelFromChannelID(String Value) {
        String[] parts = Value.split("#");
        return parts[1];
    }

    private String GetLastInput() {
        String Text = "";
        tmpString = getRecentInfo();
        ResponseCode = GetResponseCode(tmpString);
        if (ResponseCode.equals("0")) {
            JsonElement jsonTree = parser.parse(tmpString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonArray recentsArray = jsonObject.getAsJsonArray("recent_info");
            for (JsonElement re : recentsArray) {
                JsonObject recentObject = re.getAsJsonObject();                
                Text = recentObject.get("text").getAsString();
                if (config.config_FullLogs == true) {
                    logger.info("Last input: {}", Text);
                }
                break;
            }
        }
        return Text;
    }

    private void fetchOtherDevices() {
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://127.0.0.1:8080/rest/things", LongConnectionTimeout);               
            List<StateOption> options = new ArrayList<>();
            Gson gson = new Gson(); 
            ThingsRest[] resultArray = gson.fromJson(httpResponse, ThingsRest[].class);
            
            
            for(ThingsRest result : resultArray) {
                if (result.getThingTypeUID().equals("yamahamusiccast:Device")) {
                    String label = result.getLabel();
                    JsonObject jsonObject = result.getConfiguration();
                    String host = jsonObject.get("config_host").getAsString();

                    options.add(new StateOption(host + "#" + "main", label + "#main"));                    

                    Boolean zone2 = jsonObject.get("config_Zone2").getAsBoolean();
                    if (zone2 == true) {
                        options.add(new StateOption(host + "#" + "zone2", label + "#zone2"));
                    }
                    Boolean zone3 = jsonObject.get("config_Zone3").getAsBoolean();
                    if (zone3 == true) {
                        options.add(new StateOption(host + "#" + "zone3", label + "#zone3"));
                    }
                    Boolean zone4 = jsonObject.get("config_Zone4").getAsBoolean();
                    if (zone4 == true) {
                        options.add(new StateOption(host + "#" + "zone4", label + "#zone4"));
                    }
                }

            }
            ChannelUID testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelServer");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient1");
            stateDescriptionProvider.setStateOptions(testchannel, options);
        } catch (IOException e) {
        }
    }

    // End Various functions

    // API calls to AVR

    // Start Zone Related

    private String getStatus(String Zone) {
        TopicAVR = "Status";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/getStatus", ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPower(String Value, String Zone) {
        TopicAVR = "Power";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setPower?power=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setMute(String Value, String Zone) {
        TopicAVR = "Mute";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setMute?enable=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setVolume(Integer Value, String Zone) {
        TopicAVR = "Volume";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setVolume?volume=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setInput(String Value, String Zone) {
        TopicAVR = "setInput";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setInput?input=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSoundProgram(String Value, String Zone) {
        TopicAVR = "setSoundProgram";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setSoundProgram?program=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPreset(String Value, String Zone) {
        TopicAVR = "setPreset";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/recallPreset?zone=" + Zone + "&num=" + Value, LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSleep(String Value, String Zone) {
        TopicAVR = "setSleep";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + Zone + "/setSleep?sleep=" + Value, ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    // End Zone Related

    // Start Net Radio/USB Related

    private String getPresetInfo() {
        TopicAVR = "PresetInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v2/netusb/getPresetInfo", LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse; 
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getRecentInfo() {
        TopicAVR = "RecentInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/getRecentInfo", LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getPlayInfo() {
        TopicAVR = "PlayInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/getPlayInfo", LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPlayback(String Value) {
        TopicAVR = "Playback";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/setPlayback?playback=" + Value, LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }


    // End Net Radio/USB Related

    //Unused API calls to AVR
    private String getDeviceInfo() {
        TopicAVR = "DeviceInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/system/getDeviceInfo", ConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }
    private String storePreset() {
        TopicAVR = "storePreset";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/storePreset?num=1", LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getFeatures() {
        TopicAVR = "Features";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/system/getFeatures", LongConnectionTimeout);
            if (config.config_FullLogs == true) {
                logger.info(httpResponse);
            }
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + TopicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }


}
