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
import org.openhab.binding.yamahamusiccast.internal.model.DistributionInfo;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastStateDescriptionProvider;
import org.openhab.binding.yamahamusiccast.internal.YamahaMusiccastConfiguration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;
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
import java.util.UUID;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
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
    Integer connectionTimeout = 5000;
    Integer longConnectionTimeout = 60000;
    String responseCode = "";
    String powerState = "";
    String muteState = "";
    Integer volumeState = 0;
    Integer maxVolumeState = 0;
    String inputState = "";
    String inputText = "";
    Integer presetNumber = 0;
    String soundProgramState = "";
    Integer sleepState = 0;
    String topicAVR = "";
    @NonNullByDefault({}) String zone = "main";
    String channelWithoutGroup = "";
    @NonNullByDefault({}) String thingLabel = "";
    @NonNullByDefault({}) String mclink1Server = "";
    @NonNullByDefault({}) String mclink1Client1 = "";
    @NonNullByDefault({}) String mclink1Client2 = "";
    @NonNullByDefault({}) String mclink1Client3 = "";
    @NonNullByDefault({}) String mclink1Client4 = "";
    @NonNullByDefault({}) String mclink1Client5 = "";
    @NonNullByDefault({}) String mclink1Client6 = "";
    @NonNullByDefault({}) String mclink1Client7 = "";
    @NonNullByDefault({}) String mclink1Client8 = "";
    @NonNullByDefault({}) String mclink1Client9 = "";
    @NonNullByDefault({}) String mclinkSetupServer = "";
    @NonNullByDefault({}) String mclinkSetupClient1 = "";
    @NonNullByDefault({}) String mclinkSetupClient2 = "";
    @NonNullByDefault({}) String mclinkSetupClient3 = "";
    @NonNullByDefault({}) String mclinkSetupClient4 = "";
    @NonNullByDefault({}) String mclinkSetupClient5 = "";
    @NonNullByDefault({}) String mclinkSetupClient6 = "";
    @NonNullByDefault({}) String mclinkSetupClient7 = "";
    @NonNullByDefault({}) String mclinkSetupClient8 = "";
    @NonNullByDefault({}) String mclinkSetupClient9 = "";
    Boolean mclinkServerFound = false;
    Boolean mclinkClientsFound = false;
    String url = "";
    String json = "";

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
            channelWithoutGroup = channelUID.getIdWithoutGroup();
            zone = channelUID.getGroupId();
            switch (channelWithoutGroup) { //channelUID.getId()
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
                        logger.debug("Pushed Volume:" + tmpString + "/Calculated Volume:" + tmpInteger);
                        setVolume(tmpInteger, zone);
                    } catch (Exception e) {
                        //Wait for refresh
                    }                    
                    break;
                case CHANNEL_INPUT:
                    tmpString = command.toString();
                    setInput(tmpString, zone);
                    break;
                case CHANNEL_SOUNDPROGRAM:
                    tmpString = command.toString();
                    setSoundProgram(tmpString, zone);
                    break;
                case CHANNEL_SELECTPRESET:
                    tmpString = command.toString();
                    setPreset(tmpString, zone);
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
                    setSleep(tmpString, zone);
                    break;
                case CHANNEL_SERVER:
                    mclink1Server = command.toString();
                    break;
                case CHANNEL_CLIENT1:
                    mclink1Client1 = command.toString();
                    break;
                case CHANNEL_CLIENT2:
                    mclink1Client2 = command.toString();
                    break;
                case CHANNEL_CLIENT3:
                    mclink1Client3 = command.toString();
                    break;
                case CHANNEL_CLIENT4:
                    mclink1Client4 = command.toString();
                    break;
                case CHANNEL_CLIENT5:
                    mclink1Client5 = command.toString();
                    break;
                case CHANNEL_CLIENT6:
                    mclink1Client6 = command.toString();
                    break;
                case CHANNEL_CLIENT7:
                    mclink1Client7 = command.toString();
                    break;
                case CHANNEL_CLIENT8:
                    mclink1Client8 = command.toString();
                    break;
                case CHANNEL_CLIENT9:
                    mclink1Client9 = command.toString();
                    break;
                case CHANNEL_DISTRIBUTION:
                    mclinkServerFound = false;
                    mclinkClientsFound = false;
                    ArrayList<String> musiccastClients = new ArrayList<String>();
                    if (command.equals(OnOffType.ON)) {
                        logger.info("mclink Server: {}", mclink1Server);
                        logger.info("mclink Client1: {}", mclink1Client1);
                        String[] parts = mclink1Server.split("#");

                        ChannelUID tempchannel = new ChannelUID(getThing().getUID(), "Link1", CHANNEL_SERVER);
                        if (isLinked(tempchannel)) {
                            logger.info("channel server linked");
                            if (!mclink1Server.equals("")) {
                                mclinkServerFound = true;
                                parts = mclink1Server.split("#");
                                mclinkSetupServer = parts[0];
                            }
                        } 
 
                        tempchannel = new ChannelUID(getThing().getUID(), "Link1", CHANNEL_CLIENT1);
                        if (isLinked(tempchannel)) {
                            logger.info("channel client1 linked");
                            if (!mclink1Client1.equals("")) {
                                mclinkClientsFound = true;
                                parts = mclink1Client1.split("#");
                                mclinkSetupClient1 = parts[0];
                                musiccastClients.add(parts[0]);
                            } 
                        }

                        tempchannel = new ChannelUID(getThing().getUID(), "Link1", CHANNEL_CLIENT2);
                        if (isLinked(tempchannel)) {
                            logger.info("channel client2 linked");
                            if (!mclink1Client2.equals("")) {
                                mclinkClientsFound = true;
                                parts = mclink1Client2.split("#");
                                mclinkSetupClient2 = parts[0];
                                musiccastClients.add(parts[0]);
                            } 
                        }




                        if (mclinkServerFound == false) {
                            updateState(channelUID, OnOffType.OFF); 
                        }
                        if (mclinkClientsFound == false) {
                            updateState(channelUID, OnOffType.OFF); 
                        }

                    } else { //Unlink devices

                    }

                    //for (int i = 0; i < cars.size(); i++) {
                    //    System.out.println(cars.get(i));
                    //}


                    String testJSON = "{\"group_id\":\"9A237BF5AB80ED3C7251DFF49825CA42\", \"zone\":\"main\", \"type\":\"add\", \"client_list\":[\"" + mclinkSetupClient1 + "\"]}";
                    logger.info("group json: {}", testJSON);
                    InputStream is = new ByteArrayInputStream(testJSON.getBytes());
                    try {
                        url = "http://" + mclinkSetupServer + "/YamahaExtendedControl/v1/dist/setServerInfo";
                        httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout);               
                        logger.info("serverinfo : {}", httpResponse);
                    } catch (IOException e) {
                        logger.info("serverinfo : {}",e.toString());
                    }
                    testJSON = "{\"group_id\":\"9A237BF5AB80ED3C7251DFF49825CA42\", \"zone\":[\"main\"]}";
                    is = new ByteArrayInputStream(testJSON.getBytes());
                    try {
                        url = "http://" + mclinkSetupClient1 + "/YamahaExtendedControl/v1/dist/setClientInfo";
                        httpResponse = HttpUtil.executeUrl("POST", url, is, "", longConnectionTimeout);               
                        logger.info("clientinfo : {}", httpResponse);
                    } catch (IOException e) {
                        logger.info("clientinfo : {}",e.toString());
                    }   
                    try {
                        url = "http://" + mclinkSetupServer + "/YamahaExtendedControl/v1/dist/startDistribution?num=1";
                        httpResponse = HttpUtil.executeUrl("GET", url, longConnectionTimeout);               
                        logger.info("start distribution: {}", httpResponse);
                    } catch (IOException e) {
                        logger.info("start distribution: {}",e.toString());
                    } 


                    break; 
                //END DISTRIBUTION
                case CHANNEL_MCSERVER:
                    String groupId = "";
                    String role = "";
                    tmpString = command.toString();
                    String[] parts2 = tmpString.split("#");
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

                    if (!groupId.equals("")) {
                        // create JSON with new client, IP = config.host and zone is zone :)
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":\"" + zone + "\", \"type\":\"add\", \"client_list\":[\"" + config.config_host + "\"]}";
                        logger.info("group json: {}", json);
                        InputStream is2 = new ByteArrayInputStream(json.getBytes());
                        try {
                            url = "http://" + mclinkSetupServer + "/YamahaExtendedControl/v1/dist/setServerInfo";
                            httpResponse = HttpUtil.executeUrl("POST", url, is2, "", longConnectionTimeout);               
                            logger.info("serverinfo : {}", httpResponse);
                        } catch (IOException e) {
                            logger.info("serverinfo : {}",e.toString());
                        }
                        json = "{\"group_id\":\"" + groupId + "\", \"zone\":[\"" + zone + "\"]}";
                        is2 = new ByteArrayInputStream(json.getBytes());
                        try {
                            url = "http://" + config.config_host + "/YamahaExtendedControl/v1/dist/setClientInfo";
                            httpResponse = HttpUtil.executeUrl("POST", url, is2, "", longConnectionTimeout);               
                            logger.info("clientinfo : {}", httpResponse);
                        } catch (IOException e) {
                            logger.info("clientinfo : {}",e.toString());
                        }   
                        try {
                            url = "http://" + mclinkSetupServer + "/YamahaExtendedControl/v1/dist/startDistribution?num=1";
                            httpResponse = HttpUtil.executeUrl("GET", url, longConnectionTimeout);               
                            logger.info("start distribution: {}", httpResponse);
                        } catch (IOException e) {
                            logger.info("start distribution: {}",e.toString());
                        } 
                    }
                    break;
            }  // END Switch Channel          
        }
    }

    @Override
    public void initialize() {
        thingLabel = thing.getLabel();
        logger.info("YXC - Start initializing! - {}", thingLabel);
        this.config = getConfigAs(YamahaMusiccastConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        if (config.config_host.equals("")) {
            logger.info("YXC - No host found");
        } else {
            if (config.config_refreshInterval > 0) {
                startAutomaticRefresh();
            }
            updateStatus(ThingStatus.ONLINE);
            logger.info("YXC - Finished initializing! - {}", thingLabel);    
        }
    }


    private void startAutomaticRefresh() {
        refreshTask = scheduler.scheduleWithFixedDelay(this::refreshProcess, 0, config.config_refreshInterval,TimeUnit.SECONDS);
        logger.info("YXC - Start automatic refresh ({} seconds - {}) ", config.config_refreshInterval,thingLabel);
    }

    private void refreshProcess() {
        // Zone main is always present        
        updateStatusZone("main");
        if (config.config_Zone2 == true) {
            updateStatusZone("zone2");
        }
        if (config.config_Zone3 == true) {
            updateStatusZone("zone3");
        }
        if (config.config_Zone4 == true) {
            updateStatusZone("zone4");
        }
        //Not Zone related
        updatePresets();
        fetchOtherDevices();
        
    }

    @Override
    public void dispose() { 
        refreshTask.cancel(true);
    }
    // Various functions 
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
                        }
                }    
                break;
            case "999":
                    logger.info("YXC - Nothing to do! - {} ({})", thingLabel, zoneToUpdate);
                break;
        }
    }

    private void updatePresets() {
        inputText = getLastInput(); // Without zone
        tmpString = getPresetInfo(); // Without zone
        try {
            JsonElement jsonTree = parser.parse(tmpString);
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonArray presetsArray = jsonObject.getAsJsonArray("preset_info");
            tmpInteger = 0;
            tmpString = "";
            presetNumber = 0;
            List<StateOption> optionsPresets = new ArrayList<>();
            for (JsonElement pr : presetsArray) {
                tmpInteger = tmpInteger + 1;
                JsonObject presetObject = pr.getAsJsonObject();
                //String Input = presetObject.get("input").getAsString();
                String text = presetObject.get("text").getAsString();
                if (!text.equals("")) {
                    optionsPresets.add(new StateOption(tmpInteger.toString(), text));                
                    if (inputText.equals(text)) {
                        presetNumber = tmpInteger;
                    }
                }
            }
            for (Channel channel : getThing().getChannels()) {
                ChannelUID channelUID = channel.getUID();
                channelWithoutGroup = channelUID.getIdWithoutGroup();
                switch (channelWithoutGroup) { //channelUID.getId()
                    case CHANNEL_SELECTPRESET:
                        stateDescriptionProvider.setStateOptions(channelUID, optionsPresets);
                        updateState(channelUID,StringType.valueOf(presetNumber.toString()));
                        break;
                }
            }
        } catch (Exception e) {
            logger.info("Something went wrong with fetching Presets");
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

    private void fetchOtherDevices() {
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://127.0.0.1:8080/rest/things", longConnectionTimeout);               
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
            options.add(new StateOption("", ""));
            ChannelUID testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelServer");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient1");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient2");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient3");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient4");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient5");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient6");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient7");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient8");
            stateDescriptionProvider.setStateOptions(testchannel, options);
            testchannel = new ChannelUID(getThing().getUID(), "Link1", "channelClient9");
            stateDescriptionProvider.setStateOptions(testchannel, options);

            testchannel = new ChannelUID(getThing().getUID(), "main", "channelMCServer");
            stateDescriptionProvider.setStateOptions(testchannel, options);


        } catch (IOException e) {
        }
    }

    private String generateGroupId () {
        return UUID.randomUUID().toString().replace("-","").substring(0,32);
    }

    // End Various functions

    // API calls to AVR

    // Start Zone Related

    private String getStatus(String zone) {
        topicAVR = "Status";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/getStatus", connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPower(String Value, String zone) {
        topicAVR = "Power";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setPower?power=" + Value, connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setMute(String Value, String zone) {
        topicAVR = "Mute";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setMute?enable=" + Value, connectionTimeout);
            logger.debug(httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setVolume(Integer Value, String zone) {
        topicAVR = "Volume";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setVolume?volume=" + Value, connectionTimeout);
            logger.debug(httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setInput(String Value, String zone) {
        topicAVR = "setInput";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setInput?input=" + Value, connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSoundProgram(String Value, String zone) {
        topicAVR = "setSoundProgram";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setSoundProgram?program=" + Value, connectionTimeout);
            logger.debug(httpResponse);            
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPreset(String Value, String zone) {
        topicAVR = "setPreset";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/recallPreset?zone=" + zone + "&num=" + Value, longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setSleep(String Value, String zone) {
        topicAVR = "setSleep";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/" + zone + "/setSleep?sleep=" + Value, connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    // End Zone Related

    // Start Net Radio/USB Related

    private String getPresetInfo() {
        topicAVR = "PresetInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v2/netusb/getPresetInfo", longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse; 
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getRecentInfo() {
        topicAVR = "RecentInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/getRecentInfo", longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getPlayInfo() {
        topicAVR = "PlayInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/getPlayInfo", longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String setPlayback(String Value) {
        topicAVR = "Playback";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/setPlayback?playback=" + Value, longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }


    // End Net Radio/USB Related

    // Start Music Cast API calls
    private String getDistributionInfo(String Value) {
        topicAVR = "DistributionInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + Value + "/YamahaExtendedControl/v1/dist/getDistributionInfo", connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }
    // End Music Cast API calls

    //Unused API calls to AVR
    private String getDeviceInfo() {
        topicAVR = "DeviceInfo";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/system/getDeviceInfo", connectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }
    private String storePreset() {
        topicAVR = "storePreset";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/netusb/storePreset?num=1", longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }

    private String getFeatures() {
        topicAVR = "Features";
        try {
            httpResponse = HttpUtil.executeUrl("GET", "http://" + config.config_host + "/YamahaExtendedControl/v1/system/getFeatures", longConnectionTimeout);
            logger.debug(httpResponse);
            return httpResponse;
        } catch (IOException e) {
            logger.warn("IO Exception - " + topicAVR, e);
            return "{\"response_code\":\"999\"}";
        }
    }


}
