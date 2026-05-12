import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

/**
 * Portal-based modal with backdrop click and escape key to close.
 * Slots: header (string or node), children (body), footer (node).
 */
function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = 'md',
  closeOnBackdrop = true,
}) {
  const overlayRef = useRef(null);

  // Escape key
  useEffect(() => {
    if (!isOpen) return;

    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [isOpen, onClose]);

  // Lock body scroll
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl',
  };

  const handleOverlayClick = (e) => {
    if (closeOnBackdrop && e.target === overlayRef.current) {
      onClose();
    }
  };

  return createPortal(
    <div
      ref={overlayRef}
      onClick={handleOverlayClick}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
    >
      <div
        className={`relative w-full ${sizeClasses[size] || sizeClasses.md} bg-white rounded-2xl shadow-2xl flex flex-col max-h-[90vh]`}
      >
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
              aria-label="Close modal"
            >
              <X size={18} />
            </button>
          </div>
        )}

        {/* Close button when no title */}
        {!title && (
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors z-10"
            aria-label="Close modal"
          >
            <X size={18} />
          </button>
        )}

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-6 py-4">{children}</div>

        {/* Footer */}
        {footer && (
          <div className="px-6 py-4 border-t border-gray-100 flex items-center justify-end gap-3">
            {footer}
          </div>
        )}
      </div>
    </div>,
    document.body
  );
}

export default Modal;
