# WriterRecognitionApp

WriterRecognitionApp is a mobile application developed in Kotlin that uses machine learning algorithms to recognize the author of handwritten texts. The app allows users to upload images of handwritten text, associate them with an author, and identify the author of a new text image.

## Features

- **Upload and Association**: Users can upload images of handwritten text and associate them with specific authors.
- **Author Recognition**: Through an API based on Flask and Support Vector Machine (SVM), the app identifies the author of a test image by comparing it with the saved ones.
- **Accuracy Evaluation**: Each prediction displays an accuracy probability score.
- **Future Improvement Potential**: The app architecture supports future expansions, including a database to store images and a system for simultaneous test cases.

## Technology Stack

### Mobile
- **Kotlin**: Main programming language for the app.
- **Android Studio**: IDE used for development.
- **OkHttp**: Used to handle HTTP requests to the backend.

### Backend
The backend is built with Flask and handles author recognition using machine learning techniques. For more details on the backend, refer to the [WriterRecognitionAPI repository](https://github.com/fraciccio2/WriterRecognitionAPI).

## Requirements

- **Android Studio** (recommended version 2024.2.1+)

## Installation and Setup

### Mobile

1. Clone the repository:
   ```bash
   git clone https://github.com/fraciccio2/WriterRecognitionApp.git
   ```

2. Open the project in Android Studio and sync the dependencies.

3. Run the app on an Android emulator or device.

## Contributing

1. Fork the project.
2. Create a new branch for your changes (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a pull request.

Distributed under the MIT License. See `LICENSE` for more information.

---

This README provides a clear project overview along with instructions on setup and contributing.
