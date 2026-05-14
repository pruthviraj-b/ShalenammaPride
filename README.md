# 🏫 Shale-Namma Pride (ಶಾಲಾ ನಮ್ಮ ಹೆಮ್ಮೆ)

**A Full-Stack Educational Governance & Parent Engagement Ecosystem**

[![Live Portal](https://img.shields.io/badge/Live-Parent%20Portal-green?style=for-the-badge)](https://shalenammapride-b4fc8.web.app/)
[![Project Type](https://img.shields.io/badge/Project-Admin%20App%20%2B%20Web%20Portal-orange?style=for-the-badge)](https://github.com/pruthviraj-b/ShalenammaPride)
[![Download APK](https://img.shields.io/badge/Download-Android%20APK-blue?style=for-the-badge&logo=android)](./Shale-Namma-Pride-Premium-Auth.apk)

---

## 📌 Project Overview
**Shale-Namma Pride** is a technical solution designed to solve the communication and transparency gap between school administration and parents. Unlike traditional systems, this project provides a **dual-platform synchronization** where school admins manage data via a high-fidelity Android app, and parents consume updates through a lightweight, responsive web portal.

### 🎯 The Problem & Solution
*   **The Problem:** Lack of real-time visibility into mid-day meals, school infrastructure, and daily activities leads to parent anxiety and administrative inefficiency.
*   **The Solution:** A centralized Firebase-backed ecosystem where every admin action (like updating a meal menu or facility status) is instantly reflected on the parent's web feed.

---

## 🛠️ Technical Implementation & Architecture

### 📱 Android Admin App (Kotlin / Jetpack Compose)
*   **Architecture:** MVVM with Clean Architecture principles.
*   **UI/UX Logic:** Implemented **Glassmorphism** and a **SaaS-style premium aesthetic** to ensure a high-end user experience for school staff.
*   **Bilingual Engine:** A custom localization layer supporting **English & Kannada**, ensuring the tool is accessible to all demographics.
*   **Security:** Firebase Authentication with Google Sign-In and Email providers, including a secure password-reset flow.
*   **Key Modules:** Meal Management, Facility Auditing, Student Achievement Wall (Stars), and Announcement Broadcasting.

### 🌐 Parent Web Portal (React.js)
*   **Performance:** Built with **React and Vite** for near-instant load times.
*   **Live Sync:** Utilizes Firebase Real-time listeners to ensure parents see updates without manual refreshing.
*   **Responsive Design:** Fully optimized for mobile browsers, ensuring accessibility on low-end devices.

---

## 🚀 Live Demo & Links
*   **Parent Portal (Live):** [https://shalenammapride-b4fc8.web.app/](https://shalenammapride-b4fc8.web.app/)
*   **GitHub Repository:** [pruthviraj-b/ShalenammaPride](https://github.com/pruthviraj-b/ShalenammaPride)
*   **Download APK:** [Shale-Namma-Pride-Premium-Auth.apk](./Shale-Namma-Pride-Premium-Auth.apk)

---

## 🔧 Core Tech Stack
| Layer | Technologies |
| :--- | :--- |
| **Mobile Frontend** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Web Frontend** | React.js, Vanilla CSS, Vite |
| **Backend/BaaS** | Firebase (Firestore, Auth, Storage) |
| **Design Tokens** | Custom HSL Color Palette, Glassmorphic Surfaces, Google Fonts |

---

## 📈 Key Technical Challenges Solved
1.  **Dual-Platform Sync:** Ensuring that complex data structures in Kotlin matched the consumption patterns in React.
2.  **Localization:** Building a scalable system to switch between English and Kannada without UI breakage.
3.  **UI Performance:** Maintaining 60fps animations in Compose while handling real-time data streams.

---

## 👤 Developer
**Pruthviraj B**  
*Passionate about building scalable solutions for educational modernization.*
