# E-commerce App

An Android e-commerce mobile application built for the **Software for Mobile Devices (SMD)** course. Supports three user roles: **customers**, **vendors/admins**, and **unregistered users**.

## Features

### Customer
- Browse products with search, category filtering, and price range
- Product detail view with image gallery, sizes, colors, and stock info
- Shopping cart with quantity controls and checkout
- Wishlist management
- Order history with status tracking
- AI-powered chatbot assistant (Gemini 2.5 Flash)
- User profile management
- Google Sign-In / email & password authentication

### Admin / Vendor
- Dashboard analytics (user count, product count, orders, revenue)
- Product management (add/edit/delete with multi-image upload via Cloudinary)
- Category management (CRUD)
- Order management (view all orders, update status)
- Profile management

### Authentication & Security
- Email/password registration with OTP verification
- Google Sign-In
- Forgot password flow with OTP
- Role-based navigation (customer vs admin)

## Tech Stack

| Component | Technology |
|---|---|
| **Language** | Java |
| **Minimum SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 35 (Android 15) |
| **Architecture** | Single-Activity, multi-Fragment |
| **Backend** | Firebase Realtime Database |
| **Authentication** | Firebase Auth |
| **Image Hosting** | Cloudinary |
| **Image Loading** | Glide 4.16.0 |
| **AI Chat** | Google Gemini AI (0.9.0) |
| **Email** | Brevo (Sendinblue) API via OkHttp |
| **Build System** | Gradle 8.13 with Kotlin DSL |


## Getting Started

### Prerequisites
- Android Studio (Hedgehog or later)
- JDK 17+
- Android SDK (API 24–36)
- Physical Android device or emulator

### Setup

1. **Clone the repository** and open it in Android Studio.

2. **Sync Gradle** — the Gradle wrapper (8.13) downloads automatically.

3. **Set the Brevo API key** environment variable for email sending:
   ```bash
   export SENDINBLUE_API_KEY=your_brevo_api_key
   ```

4. **Build and run**:
   ```bash
   ./gradlew assembleDebug
   ```
   Or press **Run** in Android Studio.

### Configuration Notes
- Firebase config is bundled in `google-services.json`.
- Gemini AI key and Cloudinary credentials are currently hardcoded. Replace them with your own for production use.
- Firebase Realtime Database security rules must be configured in the Firebase Console.

