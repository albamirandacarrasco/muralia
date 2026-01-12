import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ImageGallery from '../components/ImageGallery';

export default function Home() {
  const { isAuthenticated } = useAuth();
  const [refreshKey, setRefreshKey] = useState(0);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center">
            <h1 className="text-5xl font-extrabold mb-4">
              Galería Muralia
            </h1>
            <p className="text-xl text-indigo-100 mb-8">
              Explora, descubre y comparte imágenes que inspiran
            </p>
            {isAuthenticated ? (
              <Link
                to="/upload"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-indigo-600 bg-white hover:bg-indigo-50 transition-colors"
              >
                <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                Subir imagen
              </Link>
            ) : (
              <div className="space-x-4">
                <Link
                  to="/login"
                  className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-indigo-600 bg-white hover:bg-indigo-50 transition-colors"
                >
                  Iniciar sesión
                </Link>
                <Link
                  to="/register"
                  className="inline-flex items-center px-6 py-3 border-2 border-white text-base font-medium rounded-md text-white hover:bg-indigo-700 transition-colors"
                >
                  Crear cuenta
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Gallery Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Últimas imágenes</h2>
          <p className="text-gray-600">Explora las subidas más recientes de nuestra comunidad</p>
        </div>

        <ImageGallery refreshTrigger={refreshKey} />
      </div>

      {/* Floating Action Button - Mobile */}
      {isAuthenticated && (
        <Link
          to="/upload"
          className="fixed bottom-6 right-6 bg-indigo-600 text-white p-4 rounded-full shadow-lg hover:bg-indigo-700 transition-all hover:scale-110 lg:hidden"
          title="Subir imagen"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </Link>
      )}
    </div>
  );
}
