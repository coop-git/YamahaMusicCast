# Yamaha MusicCast Binding

Binding to control AV Receiver(s) from Yamaha via their MusicCast protocol (aka Yamaha Extended Control).
With support for 4 zones : main, zone2, zone3, zone4. Main is always present. Zone2, Zone3, Zone4 to enable.

## Supported Things

Each AV Receiver is a Thing.

## Discovery

No auto discovery

## Binding Configuration

N/A

## Thing Configuration

| Parameter              | type    | description                                             | Advanced | Required      |
|------------------------|---------|---------------------------------------------------------|----------|---------------|
| config_host            | String  | IP address of AVR                                       | false    | true          |
| config_refreshInterval | Integer | The refresh interval in seconds (0=disable).            | false    | Default=60    |
| config_Zone2           | Booelan | Is Zone 2 active?                                       | true     | Default=false |
| config_Zone3           | Boolean | Is Zone 3 active?                                       | true     | Default=false |
| config_Zone4           | Boolean | Is Zone 4 active?                                       | true     | Default=false |
| config_FullLogs        | Boolean | Log everything?                                         | true     | Default=false |

Thing yamahamusiccast:Device:zone#Living "YXC Living" [config_host="1.2.3.4"]

## Channels



| channel              | type   | description                                         |
|----------------------|--------|-----------------------------------------------------|
| channelPower         | Switch | Power ON/OFF                                        |
| channelMute          | Switch | Mute ON/OFF                                         |
| channelVolume        | Dimmer | Volume 0-100 (recalculated based on Max Volume AVR) |
| channelInput         | String | See below for list                                  |
| channelSoundProgram  | String | See below for list                                  |
| channelSelectPreset  | String | Select Netradio/USB preset (favorite)               |
| channelPresets       | String | List of Presets to facilitate creation of sitemaps  |
| channelPlayer        | Player | PLAY/PAUSE/NEXT/PREVIOUS/REWIND/FASTFORWARD         |

## Full Example

N/A

## Input List

cd / tuner / multi_ch / phono / hdmi1 / hdmi2 / hdmi3 / hdmi4 / hdmi5 / hdmi6 / hdmi7 /
hdmi8 / hdmi / av1 / av2 / av3 / av4 / av5 / av6 / av7 / v_aux / aux1 / aux2 / aux / audio1 /
audio2 / audio3 / audio4 / audio_cd / audio / optical1 / optical2 / optical / coaxial1 / coaxial2 /
coaxial / digital1 / digital2 / digital / line1 / line2 / line3 / line_cd / analog / tv / bd_dvd /
usb_dac / usb / bluetooth / server / net_radio / rhapsody / napster / pandora / siriusxm /
spotify / juke / airplay / radiko / qobuz / mc_link / main_sync / none

## Sound Program

munich_a / munich_b / munich / frankfurt / stuttgart / vienna / amsterdam / usa_a / usa_b /
tokyo / freiburg / royaumont / chamber / concert / village_gate / village_vanguard /
warehouse_loft / cellar_club / jazz_club / roxy_theatre / bottom_line / arena / sports /
action_game / roleplaying_game / game / music_video / music / recital_opera / pavilion /
disco / standard / spectacle / sci-fi / adventure / drama / talk_show / tv_program /
mono_movie / movie / enhanced / 2ch_stereo / 5ch_stereo / 7ch_stereo / 9ch_stereo /
11ch_stereo / stereo / surr_decoder / my_surround / target / straight / off

## Tested Devices

RX-D485 / WX-010 / WX-030 / ISX-80 / YSP-1600 /RX-A860 / R-N303D / EX-A1080 