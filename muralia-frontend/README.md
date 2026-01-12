# Muralia Frontend

Modern React frontend for the Muralia image management platform.

## Features

- **Authentication**: User registration and login with JWT
- **Image Upload**: Upload images with title and description
- **Image Gallery**: Browse latest images with pagination
- **Responsive Design**: Mobile-friendly UI with Tailwind-inspired utilities
- **Protected Routes**: Secure authenticated pages

## Tech Stack

- **React 19** - UI library
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Context API** - State management

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── ImageGallery.jsx
│   ├── ImageUpload.jsx
│   ├── Navbar.jsx
│   └── ProtectedRoute.jsx
├── context/            # React Context providers
│   └── AuthContext.jsx
├── pages/              # Page components
│   ├── Home.jsx
│   ├── Login.jsx
│   └── Register.jsx
├── services/           # API services
│   └── api.js
├── App.jsx            # Main app component
├── main.jsx           # App entry point
└── index.css          # Global styles
```

## Setup Instructions

### 1. Install Dependencies

First, make sure you have Node.js installed (v18+ recommended). Then install the project dependencies:

```bash
cd /home/ricardo/dev/muralia-frontend
npm install
```

### 2. Configure Environment

The `.env` file is already created with default values:

```env
VITE_API_BASE_URL=http://localhost:8080
```

If your backend runs on a different port or URL, update this value.

### 3. Start Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:5173`

### 4. Build for Production

```bash
npm run build
```

The production build will be in the `dist/` directory.

## Backend Integration

This frontend connects to the Muralia backend API (Spring Boot) at:
- Default: `http://localhost:8080`
- Configured via `VITE_API_BASE_URL` environment variable

### API Endpoints Used

**Authentication:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/logout` - Logout user
- `GET /api/auth/me` - Get current user

**Images:**
- `POST /api/images` - Upload image (multipart/form-data)
- `GET /api/images` - Get latest images (pagination)
- `GET /api/images/{id}` - Get image metadata
- `GET /api/images/{id}/file` - Get image file
- `DELETE /api/images/{id}` - Delete image

## Development Workflow

### Running with Backend

1. Start the backend server (Gradle):
   ```bash
   cd /home/ricardo/dev/muralia
   ./gradlew bootRun
   ```

2. Start the frontend (in a separate terminal):
   ```bash
   cd /home/ricardo/dev/muralia-frontend
   npm run dev
   ```

3. Open browser to `http://localhost:5173`

### Authentication Flow

1. Register a new account at `/register`
2. Login at `/login`
3. JWT token is stored in `localStorage`
4. Token is automatically added to all API requests
5. Upload images and browse the gallery

### CORS Configuration

The backend must allow requests from `http://localhost:5173` during development. Ensure your Spring Boot app has proper CORS configuration.

## Features Overview

### Image Upload
- Supports: JPEG, PNG, GIF, WebP
- Preview before upload
- Optional title and description
- Real-time upload progress

### Image Gallery
- Grid layout (responsive: 1-4 columns)
- Lazy loading with "Load More" pagination
- Delete own images
- Image metadata display

### Authentication
- JWT-based authentication
- Auto-redirect on 401 errors
- Persistent login (localStorage)
- Protected routes for authenticated users

## Styling

The app uses custom CSS utility classes inspired by Tailwind CSS, providing a clean and modern UI without external dependencies.

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## License

Private project - All rights reserved
