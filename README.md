# Yamaha MusicCast Binding

Binding to control Yamaha models via their MusicCast protocol (aka Yamaha Extended Control).
With support for 4 zones : main, zone2, zone3, zone4. Main is always present. Zone2, Zone3, Zone4 are read from the model.

For Music Cast, the different Things are read via the OpenHAB REST API.

## Supported Things

Each model (AV Receiver, ...) is a Thing.

## Discovery

No auto discovery

## Binding Configuration

N/A

## Thing Configuration

| Parameter              | type    | description                                             | Advanced | Required      |
|------------------------|---------|---------------------------------------------------------|----------|---------------|
| config_host            | String  | IP address of the Yamaha model (AVR, ...)               | false    | true          |
| config_refreshInterval | Integer | The refresh interval in seconds (0=disable).            | false    | Default=60    |

Thing yamahamusiccast:Device:zone#Living "YXC Living" [config_host="1.2.3.4"]

## Channels

| channel               | type   | description                                           |
|-----------------------|--------|-------------------------------------------------------|
| channelPower          | Switch | Power ON/OFF                                          |
| channelMute           | Switch | Mute ON/OFF                                           |
| channelVolume         | Dimmer | Volume 0-100 (recalculated based on Max Volume Model) |
| channelInput          | String | See below for list                                    |
| channelSoundProgram   | String | See below for list                                    |
| channelSelectPreset   | String | Select Netradio/USB preset (favorite)                 |
| channelSleep          | Number | Fixed values for Sleep : 0/30/60/90/120               |
| channelMCServer       | String | Select your MusicCast Server                          |
| channelUnlinkMCServer | Switch | Disband MusicCast Link on Master                      |
| channelPlayer         | Player | PLAY/PAUSE/NEXT/PREVIOUS/REWIND/FASTFORWARD           |
| channelRecallScene    | Number | Select a scene                                        |


| Zones                | description                                          |
|----------------------|------------------------------------------------------|
| Zone1-4              | Zone 1 to 4 to control Power, Volume, ...            |
| playerControls       | Separate zone for Play, Pause, ...                   |

## Full Example

N/A

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

## Tested Models

RX-D485 / WX-010 / WX-030 / ISX-80 / YSP-1600 / RX-A860 / R-N303D / EX-A1080 / WXA-050 / HTR-4068 (RX-V479)
MusicCast 20 / WCX-50 / RX-V6A / YAS-306

## Changelog

###### To Do / Wishlist

- [ ] UDP event push feature
- [ ] Add Play Info (Artist, track, …)
- [ ] Autodiscovery
- [ ] HDMI out selection
- [ ] Create a pull request for OH3


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