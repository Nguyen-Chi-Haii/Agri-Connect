# Agri-Connect

Agri-Connect is a platform connecting farmers and consumers, facilitating efficient agricultural trade and communication. This repository contains the source code for both the Backend API and the Mobile Application.

## Project Structure

- **API/**: Backend server built with **Spring Boot** and **MongoDB**.
- **FE/Android/**: Mobile application built with **Android (Java)**.

## Prerequisites

- **Java JDK 17+**
- **MongoDB** (Running locally or cloud)
- **Android Studio** (Latest version recommended)
- **ADB** (Android Debug Bridge) installed and configured in PATH.

---

## 🚀 Getting Started

### 1. Backend (API)

The API must be running for the mobile app to function.

1.  Navigate to the API directory:
    ```bash
    cd API
    ```
2.  Configure environment variables in `.env` (check `application.properties` for details).
3.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```
    *The server will start on port `8080`.*

### 2. Mobile App (Android)

1.  Open **Android Studio**.
2.  Select **Open an existing Android Studio project** and point to `FE/Android`.
3.  Let Gradle sync and build the project.

---

## 📱 Connectivity & Configuration

The Android application supports **Dynamic Runtime Configuration** for the API URL. This allows you to switch between testing environments (Emulator, Real Device, LAN) without rebuilding the app.

### Scenario 1: Android Emulator (Default)
**Best for:** Quick local development on the same PC.

*   **Setup:** None required.
*   **How it works:** The app defaults to `http://10.0.2.2:8080/`, which is the special alias for `localhost` inside the Android Emulator.
*   **Action:** Just run the app.

### Scenario 2: Real Android Device (USB Cable)
**Best for:** Testing on real hardware with stable connection.

*   **Setup:**
    1.  Connect phone via USB.
    2.  Enable **USB Debugging** on the phone.
    3.  Run the following command on your PC to map the port:
        ```bash
        adb reverse tcp:8080 tcp:8080
        ```
*   **Configuration:**
    Run these commands to create the directory and point the app to `localhost`:
    ```bash
    adb shell "mkdir -p /sdcard/Android/data/com.agriconnect.agri_connect/files/"
    adb shell "echo http://localhost:8080/ > /sdcard/Android/data/com.agriconnect.agri_connect/files/api_url.txt"
    ```
*   **Action:** Restart the app.

### Scenario 3: External Devices / LAN / VMware (iOS Mock)
**Best for:** Testing from other devices on the same WiFi, or Virtual Machines (e.g., iOS Simulator on VMware MacOS).

*   **Setup:**
    1.  Find your PC's Local IP address (e.g., `192.168.1.5`).
    2.  Ensure your PC and the device/VM are on the same network.
    3.  (Windows) Ensure Firewall allows inbound connections on port `8080`.
*   **Configuration:**
    Run these commands (or create the file manually on the device) with your LAN IP:
    ```bash
    adb shell "mkdir -p /sdcard/Android/data/com.agriconnect.agri_connect/files/"
    adb shell "echo http://192.168.1.5:8080/ > /sdcard/Android/data/com.agriconnect.agri_connect/files/api_url.txt"
    ```
    *(Replace `192.168.1.5` with your actual IP address)*.
*   **Action:** Restart the app.

---

## 🔧 Troubleshooting

- **App cannot connect?**
    - Check if the API server is running (`localhost:8080` on PC).
    - If using Scenario 2: Did you run `adb reverse`?
    - If using Scenario 3: Check Windows Firewall.
- **Reset to Default:**
    To switch back to Emulator mode (10.0.2.2), delete the config file:
    ```bash
    adb shell "rm /sdcard/Android/data/com.agriconnect.agri_connect/files/api_url.txt"
    ```
