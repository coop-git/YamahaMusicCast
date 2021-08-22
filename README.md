# Yamaha MusicCast Binding

Binding to control Yamaha models via their MusicCast protocol (aka Yamaha Extended Control).
With support for 4 zones : main, zone2, zone3, zone4. Main is always present. Zone2, Zone3, Zone4 are read from the model.

UDP events are captured to reflect changes in the binding for

- Power
- Mute
- Volume
- Input
- Presets
- Sleep
- Artist
- Track
- Album
- Album Art
- Repeat
- Shuffle
- Play Time
- Total Time
- Musiccast Link

## Supported Things

Each model (AV Receiver, ...) is a Thing (Thing Type ID: yamahamusiccast:device). Things are linked to a Bridge (Thing Type ID: yamahamusiccast:bridge) for receiving UDP events.

## Discovery

No auto discovery

## Thing Configuration

| Parameter          | Type    | Description                                             | Advanced | Required      |
|--------------------|---------|---------------------------------------------------------|----------|---------------|
| host               | String  | IP address of the Yamaha model (AVR, ...)               | false    | true          |
| syncVolume         | Boolean | Sync volume across linked models (default=false)        | false    | false         |
| defaultAfterMCLink | String  | Default Input value for client when MC Link is broken   | false    | false         |

Default value for *defaultAfterMCLink* is *NET RADIO* as most of the models have this on board.

## Channels

| channel        | type   | description                                                         |
|----------------|--------|---------------------------------------------------------------------|
| power          | Switch | Power ON/OFF                                                        |
| mute           | Switch | Mute ON/OFF                                                         |
| volume         | Dimmer | Volume as % (recalculated based on Max Volume Model)                |
| volumeAbs      | Number | Volume as absolute value                                            |
| input          | String | See below for list                                                  |
| soundProgram   | String | See below for list                                                  |
| selectPreset   | String | Select Netradio/USB preset (fetched from Model)                     |
| sleep          | Number | Fixed values for Sleep : 0/30/60/90/120 in minutes                  |
| recallScene    | Number | Select a scene (8 defaults scenes are foreseen)                     |
| player         | Player | PLAY/PAUSE/NEXT/PREVIOUS/REWIND/FASTFORWARD                         |
| artist         | String | Artist                                                              |
| track          | String | Track                                                               |
| album          | String | Album                                                               |
| albumArt       | Image  | Album Art                                                           |
| repeat         | String | Toggle Repeat. Available values: Off, One, All                      |
| shuffle        | String | Toggle Shuffle. Available values: Off, On, Songs, Album             |
| playTime       | String | Play time of current selection: radio, song, track, ...             |
| totalTime      | String | Total time of current selection: radio, song, track, ...            |
| mclinkStatus   | String | Select your Musiccast Server or set to Standalone, Server or Client |


| Zones                | description                                          |
|----------------------|------------------------------------------------------|
| zone1-4              | Zone 1 to 4 to control Power, Volume, ...            |
| playerControls       | Separate zone for Play, Pause, ...                   |

## Input List

Firmware v1

cd / tuner / multi_ch / phono / hdmi1 / hdmi2 / hdmi3 / hdmi4 / hdmi5 / hdmi6 / hdmi7 /
hdmi8 / hdmi / av1 / av2 / av3 / av4 / av5 / av6 / av7 / v_aux / aux1 / aux2 / aux / audio1 /
audio2 / audio3 / audio4 / audio_cd / audio / optical1 / optical2 / optical / coaxial1 / coaxial2 /
coaxial / digital1 / digital2 / digital / line1 / line2 / line3 / line_cd / analog / tv / bd_dvd /
usb_dac / usb / bluetooth / server / net_radio / rhapsody / napster / pandora / siriusxm /
spotify / juke / airplay / radiko / qobuz / mc_link / main_sync / none

Firmware v2

cd / tuner / multi_ch / phono / hdmi1 / hdmi2 / hdmi3 / hdmi4 / hdmi5 / hdmi6 / hdmi7 / 
hdmi8 / hdmi / av1 / av2 / av3 / av4 / av5 / av6 / av7 / v_aux / aux1 / aux2 / aux / audio1 / 
audio2 / audio3 / audio4 / **audio5** / audio_cd / audio / optical1 / optical2 / optical / coaxial1 / coaxial2 / 
coaxial / digital1 / digital2 / digital / line1 / line2 / line3 / line_cd / analog / tv / bd_dvd / 
usb_dac / usb / bluetooth / server / net_radio / ~~rhapsody~~ /napster / pandora / siriusxm / 
spotify / juke / airplay / radiko / qobuz / **tidal** / **deezer** / mc_link / main_sync / none

## Sound Program

munich_a / munich_b / munich / frankfurt / stuttgart / vienna / amsterdam / usa_a / usa_b /
tokyo / freiburg / royaumont / chamber / concert / village_gate / village_vanguard /
warehouse_loft / cellar_club / jazz_club / roxy_theatre / bottom_line / arena / sports /
action_game / roleplaying_game / game / music_video / music / recital_opera / pavilion /
disco / standard / spectacle / sci-fi / adventure / drama / talk_show / tv_program /
mono_movie / movie / enhanced / 2ch_stereo / 5ch_stereo / 7ch_stereo / 9ch_stereo /
11ch_stereo / stereo / surr_decoder / my_surround / target / straight / off

## Full Example

### Bridge & Thing(s)

```
Bridge yamahamusiccast:bridge:bridge "YXC Bridge" {
Thing yamahamusiccast:device:Living "YXC Living" [host="1.2.3.4"]
}
```

### Basic setup

```
Switch YamahaPower "" {channel="yamahamusiccast:device:Living:main#power"}
Switch YamahaMute "" {channel="yamahamusiccast:device:Living:main#mute"}
Dimmer YamahaVolume "" {channel="yamahamusiccast:device:Living:main#volume"}
Number YamahaVolumeAbs "" {channel="yamahamusiccast:device:Living:main#volumeAbs"}
String YamahaInput "" {channel="yamahamusiccast:device:Living:main#input"}
String YamahaSelectPreset "" {channel="yamahamusiccast:device:Living:main#selectPreset"}
String YamahaSoundProgram "" {channel="yamahamusiccast:device:Living:main#soundProgram"}
```

### Player controls

```
Player YamahaPlayer "" {channel="yamahamusiccast:device:Living:playerControls#player"}
String YamahaArt "" {channel="yamahamusiccast:device:Living:playerControls#albumArt"}
String YamahaArtist "" {channel="yamahamusiccast:device:Living:playerControls#artist"}
String YamahaTrack "" {channel="yamahamusiccast:device:Living:playerControls#track"}
String YamahaAlbum "" {channel="yamahamusiccast:device:Living:playerControls#album"}
```

### MusicCast setup

The idea here is to select what device/model will be the master. This needs to be done per device/model which will then be the slave.
If you want the *Living* to be the master for the *Kitchen*, select *Living - zone (IP)* from the thing *Kitchen*.
The binding will check if there is already a group active for which *Living* is the master. If yes, this group will be used and *Kitchen* will be added.
If not, a new group will be created.

*Device A*: Living with IP 192.168.1.1
*Device B*: Kitchen with IP 192.168.1.2

Set **mclinkStatus** to *Standalone* to remove the device/model from the current active group. The group will keep on exist with other devices/models.
If the device/model is the server, the group will be disbanded.

```
String YamahaMCLinkStatus "" {channel="yamahamusiccast:device:Living:main#mclinkStatus"}
```

During testing with the Yamaha Musiccast app, when removing a slave from the group, the status of the client remained *client* and **input** stayed on *mclink*. Only when changing input, the slave was set to *standalone*. Therefor you can set the parameter **defaultAfterMCLink** to an input value supported by your device to break the whole Musiccast Link in OH.

#### How to use this in a rule?

The label uses the format _Thinglabel - zone (IP)_.
The value which is sent to OH uses the format _IP***zone_.

```
sendCommand(Kitchen_YamahaMCServer, "192.168.1.1***main")
sendCommand(Kitchen_YamahaMCServer, "")
sendCommand(Kitchen_YamahaMCServer, "server")
sendCommand(Kitchen_YamahaMCServer, "client")
```

## Tested Models

RX-D485 / WX-010 / WX-030 / ISX-80 / YSP-1600 / RX-A860 / R-N303D / EX-A1080 / WXA-050 / HTR-4068 (RX-V479)
MusicCast 20 / WCX-50 / RX-V6A / YAS-306 / ISX-18D / WX-021 / YAS-408

## Changelog

###### To Do / Wishlist (last updated 17 Feb 2021)

- [ ] Create a pull request for OH3 (in progress, working on requested changes).
- [ ] Register binding as Audio Sink (currently not possible).
- [ ] Research if it is possible to only change volume of Master without changing config.
- [ ] Autodiscovery (no plans).
- [ ] One central power switch (no plans as not available in API).

###### v0.8x - In development

- Added channels for TotalTime and PlayTime updated with UDP events (v0.80).
- UDP event for mc_link are caught. Server/Client/Standalone are filled (v0.80).
- Another set of changes to avoid null values and be compliant with coding guidelines for Pull Request OH3 (v0.80).
- **BREAKING CHANGE**: _mcServer_ and _unlinkMCServer_ are replaced by _mclinkStatus_. Use this channel to select your Musiccast Server or set to Standalone, Server or Client (v0.81).
- Channels and zones are created dynamically based on number of zones supported by your model (v0.81).
- The new channel _mclinkStatus_ will also show the number of connected clients in case Thing is acting as server (v0.81).
- The channel _recallScene_ now has 8 defaults scenes numbered from 1 to 8 (v0.81).
- Small bugfixes (v0.82/v0.83).
- Sometimes scheduler for UDP events fails. When powering on device via binding, scheduler is checked and started if missing (v0.84).

###### v0.7x

- **BREAKING CHANGE**: Added a bridge to receive UDP events (Power, Mute, Volume, Input) by your OpenHAB instance from various devices. Each Thing will keep the connection alive. UDP events will be dispatched to the corresponding Thing (v0.70).
- channelVolumeAbs has been added to allow to set Volume in absolute value (v0.71).
- Code clean up for Music Cast Link to support common Volume for linked models (v0.72).
- UDP events now support PLAY/PAUSE/FFW/REW/Artist/Track/Album (v0.72).
- Removed refreshjob as UDP events now support Presets/Sleep (v0.73).
- Removed configuration for refreshInterval (v0.74).
- Added channel for AlbumArt/Shuffle/Repeat (v0.74).
- Fixed error which occured when updating Thing (v0.74).
- Other Things detected via Bridge instead of API (v0.74).
- Revert changes for Sync Volume and detect other Things via Bridge (v0.75).
- 2nd try for Sync Volume and detect other Things via Bridge (v0.76).
- Changed *empty value* to *Standalone* (v0.76).
- Update environment to OH 2.5.12, worked further on coding guidelines (v0.77).
- Changes to avoid null values and be compliant with coding guidelines for Pull Request OH3 (v0.78).
- **BREAKING CHANGE**: Thing type renamed from _Device_ to _device_ (v0.79).
- **BREAKING CHANGE**: Configuration parameter renamed from _configHost_ to _host_ (v0.79).
- **BREAKING CHANGE**: Configuration parameter renamed from _configSyncVolume_ to _syncVolume_ (v0.79).
- **BREAKING CHANGE**: Removed the word _channel_ in Channel names and Channel names are changed from upper to lower case.(v0.79).
- Set client to _Standalone_ when input is changed (v0.79).

###### v0.60

- **BREAKING CHANGE**: configuration parameters renamed. "config_host" is replaced with "configHost", "config_refreshInterval" is replaced with "configRefreshInterval"
- Added Artist, Track and Album to the playerControls
- When error occurs, 1 lines is saved instead of whole stacktrace
- Presets are now shown with a number

###### v0.50

- Number of zones are read from device, removed this from configuration
- Support added for Music Cast Link: channelMCServer and channelUnlinkMCServer have been added
- channelRecallScene has been added to select a Scene

###### v0.40

- Added Zone Support
- Favorites are fetched and made available as options
- Various changes under the hood

###### v0.30 / v 0.20 / v0.10

- Initial commits for basic functionality (Power, Mute, Input, ...)

###### v0.01

- Started from skeleton
