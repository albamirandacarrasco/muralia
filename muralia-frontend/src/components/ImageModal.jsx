import { useEffect } from 'react';

export default function ImageModal({ image, images, onClose, onPrevious, onNext }) {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowLeft') onPrevious();
      if (e.key === 'ArrowRight') onNext();
    };

    window.addEventListener('keydown', handleKeyDown);
    // Prevent body scroll when modal is open
    document.body.style.overflow = 'hidden';

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'unset';
    };
  }, [onClose, onPrevious, onNext]);

  if (!image) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-95 animate-fadeIn">
      {/* Backdrop - click to close */}
      <div
        className="absolute inset-0"
        onClick={onClose}
      />

      {/* Close Button */}
      <button
        onClick={onClose}
        className="absolute top-4 right-4 z-10 p-2 text-white hover:text-gray-300 transition-colors bg-black bg-opacity-50 rounded-full"
        title="Close (ESC)"
      >
        <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      {/* Previous Button */}
      {onPrevious && (
        <button
          onClick={onPrevious}
          className="absolute left-4 z-10 p-3 text-white hover:text-gray-300 transition-colors bg-black bg-opacity-50 rounded-full"
          title="Previous (←)"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      )}

      {/* Next Button */}
      {onNext && (
        <button
          onClick={onNext}
          className="absolute right-4 z-10 p-3 text-white hover:text-gray-300 transition-colors bg-black bg-opacity-50 rounded-full"
          title="Next (→)"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      )}

      {/* Image Container */}
      <div className="relative z-10 max-w-7xl max-h-screen px-4 py-16">
        <img
          src={image.url}
          alt={image.title || 'Image'}
          className="max-w-full max-h-[85vh] object-contain mx-auto rounded-lg shadow-2xl"
          onClick={(e) => e.stopPropagation()}
        />

        {/* Image Info Overlay */}
        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black to-transparent p-6 rounded-b-lg">
          <div className="text-white">
            {image.title && (
              <h2 className="text-2xl font-bold mb-2">{image.title}</h2>
            )}
            {image.description && (
              <p className="text-gray-200 mb-3">{image.description}</p>
            )}
            <div className="flex items-center space-x-3 text-sm">
              <div className="flex items-center space-x-2">
                <div className="bg-indigo-500 rounded-full w-8 h-8 flex items-center justify-center">
                  <span className="text-white font-semibold text-sm">
                    {image.customerUsername?.charAt(0).toUpperCase()}
                  </span>
                </div>
                <span className="font-medium">{image.customerUsername}</span>
              </div>
              <span className="text-gray-300">•</span>
              <span className="text-gray-300">
                {new Date(image.uploadedAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Image counter */}
      {images && images.length > 1 && (
        <div className="absolute top-4 left-1/2 transform -translate-x-1/2 bg-black bg-opacity-50 text-white px-4 py-2 rounded-full text-sm">
          {images.findIndex(img => img.id === image.id) + 1} / {images.length}
        </div>
      )}
    </div>
  );
}
