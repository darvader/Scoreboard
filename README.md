# Scoreboard

An Android application for controlling LED matrix displays in sports scorekeeping scenarios. This app allows users to manage and display live scores, timeouts, and other game statistics on networked LED matrices.

## Features

- **Live Score Display**: Real-time score updates for sports matches
- **LED Matrix Detection**: Automatically discovers available LED matrices on the network
- **Score Management**: Control points, sets, and serving indicators
- **Timeout Functionality**: Built-in timeout timer with visual progress bar
- **Network Control**: Send commands to LED matrices via UDP
- **Broadcast Mode**: Control multiple matrices simultaneously
- **Brightness Control**: Adjust display brightness
- **Scroll Text**: Display custom messages on the matrix

## Architecture

This application communicates with LED matrix hardware running the [LedMatrix firmware](https://github.com/darvader/LedMatrix). The Android app acts as a remote control, sending UDP commands to discovered matrices on the network.

## Building and Running

### Prerequisites
- Android Studio (latest version recommended)
- JDK 21 or higher
- Android device or emulator (API 21+)

### Setup
1. Clone this repository:
   ```bash
   git clone https://github.com/darvader/Scoreboard.git
   cd Scoreboard
   ```

2. Open the project in Android Studio

3. Build and run on your device/emulator

### Network Setup
- Ensure your Android device and LED matrices are on the same network
- The app uses UDP port 4210 for matrix communication and 4445 for discovery

## Usage

1. Launch the app
2. In the Scoreboard activity, tap "Detect" to find available LED matrices
3. Select a matrix by tapping its IP address button
4. Use the controls to manage scores, timeouts, and display settings

## LiveScore Integration

The app features a LiveScore mode that fetches real-time match results from the official live ticker systems of SAMS for Thuringia (TVV) and Germany (DVV). This is accomplished using a WebSocket connection to the SAMS live ticker service.

### How it works

1. **WebSocket Connection**: The app establishes a WebSocket connection to the SAMS live ticker server for the selected region (TVV or DVV).
2. **Live Data Reception**: As the match progresses, live score updates are received instantly via the WebSocket.
3. **LED Matrix Update**: The app parses the received data and immediately sends the updated scores and set information to the selected LED matrix, ensuring the display is always in sync with the official live ticker.

This integration allows for fully automated, real-time score display on the LED matrix during matches, with no manual input required.

## Related Projects

- **[LedMatrix Firmware](https://github.com/darvader/LedMatrix)**: The ESP32-based firmware that runs on the LED matrix hardware controlled by this app

## License

This project is open source. Please check the license file for details.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.
