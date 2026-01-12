import { useState, useEffect } from 'react';
import { imagesAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import ImageModal from './ImageModal';

export default function ImageGallery({ refreshTrigger }) {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const { user } = useAuth();

  const limit = 12;

  useEffect(() => {
    loadImages(true);
  }, [refreshTrigger]);

  const loadImages = async (reset = false) => {
    try {
      if (reset) {
        setLoading(true);
        setPage(0);
      } else {
        setLoadingMore(true);
      }

      const currentPage = reset ? 0 : page;
      const response = await imagesAPI.getLatestImages(limit, currentPage * limit);
      const newImages = response.data.images;

      if (reset) {
        setImages(newImages);
      } else {
        setImages((prev) => [...prev, ...newImages]);
      }

      setHasMore(newImages.length === limit);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load images');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  const handleDelete = async (imageId) => {
    if (!window.confirm('Are you sure you want to delete this image?')) {
      return;
    }

    try {
      await imagesAPI.deleteImage(imageId);
      setImages((prev) => prev.filter((img) => img.id !== imageId));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete image');
    }
  };

  const loadMore = () => {
    setPage((prev) => prev + 1);
    loadImages(false);
  };

  const handleImageClick = (image) => {
    setSelectedImage({
      ...image,
      url: imagesAPI.getImageFileUrl(image.id)
    });
  };

  const handleCloseModal = () => {
    setSelectedImage(null);
  };

  const handlePreviousImage = () => {
    if (!selectedImage) return;
    const currentIndex = images.findIndex(img => img.id === selectedImage.id);
    if (currentIndex > 0) {
      const prevImage = images[currentIndex - 1];
      setSelectedImage({
        ...prevImage,
        url: imagesAPI.getImageFileUrl(prevImage.id)
      });
    }
  };

  const handleNextImage = () => {
    if (!selectedImage) return;
    const currentIndex = images.findIndex(img => img.id === selectedImage.id);
    if (currentIndex < images.length - 1) {
      const nextImage = images[currentIndex + 1];
      setSelectedImage({
        ...nextImage,
        url: imagesAPI.getImageFileUrl(nextImage.id)
      });
    }
  };

  // Loading State
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <svg className="animate-spin h-12 w-12 text-indigo-600 mb-4" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p className="text-gray-600">Loading images...</p>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="rounded-md bg-red-50 p-6 border border-red-200">
        <div className="flex items-center">
          <svg className="h-6 w-6 text-red-400 mr-3" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
          </svg>
          <p className="text-red-800">{error}</p>
        </div>
      </div>
    );
  }

  // Empty State
  if (images.length === 0) {
    return (
      <div className="text-center py-20">
        <svg
          className="mx-auto h-20 w-20 text-gray-300"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
        <h3 className="mt-4 text-xl font-medium text-gray-900">No images yet</h3>
        <p className="mt-2 text-gray-500">Be the first to share something beautiful!</p>
      </div>
    );
  }

  // Gallery Grid
  return (
    <div>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {images.map((image) => (
          <div
            key={image.id}
            className="group bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
          >
            {/* Image */}
            <div
              className="relative aspect-square bg-gray-100 overflow-hidden cursor-pointer"
              onClick={() => handleImageClick(image)}
            >
              <img
                src={imagesAPI.getImageFileUrl(image.id)}
                alt={image.title || 'Image'}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                loading="lazy"
              />

              {/* Overlay on Hover */}
              <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-opacity duration-300 flex items-center justify-center">
                <svg
                  className="w-12 h-12 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7"
                  />
                </svg>
              </div>
            </div>

            {/* Details */}
            <div className="p-4">
              {/* Title */}
              <h3 className="text-base font-semibold text-gray-900 truncate mb-1">
                {image.title || image.fileName}
              </h3>

              {/* Description */}
              {image.description && (
                <p className="text-sm text-gray-600 line-clamp-2 mb-3">
                  {image.description}
                </p>
              )}

              {/* Footer */}
              <div className="flex items-center justify-between">
                {/* User & Date */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <div className="bg-indigo-100 rounded-full w-6 h-6 flex items-center justify-center flex-shrink-0">
                      <span className="text-indigo-600 font-semibold text-xs">
                        {image.customerUsername?.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-gray-700 truncate">
                        {image.customerUsername}
                      </p>
                      <p className="text-xs text-gray-500">
                        {new Date(image.uploadedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Delete Button */}
                {user && user.id === image.customerId && (
                  <button
                    onClick={() => handleDelete(image.id)}
                    className="ml-2 p-2 text-red-600 hover:bg-red-50 rounded-full transition-colors"
                    title="Delete image"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Load More Button */}
      {hasMore && (
        <div className="mt-12 text-center">
          <button
            onClick={loadMore}
            disabled={loadingMore}
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loadingMore ? (
              <>
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Loading...
              </>
            ) : (
              <>
                Load More
                <svg className="ml-2 -mr-1 w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </>
            )}
          </button>
        </div>
      )}

      {/* Image Modal */}
      {selectedImage && (
        <ImageModal
          image={selectedImage}
          images={images}
          onClose={handleCloseModal}
          onPrevious={images.findIndex(img => img.id === selectedImage.id) > 0 ? handlePreviousImage : null}
          onNext={images.findIndex(img => img.id === selectedImage.id) < images.length - 1 ? handleNextImage : null}
        />
      )}
    </div>
  );
}
