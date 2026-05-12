import Button from './Button';

/**
 * Empty state placeholder with icon, title, description, optional action
 */
function EmptyState({
  icon: Icon,
  title = 'Nothing here yet',
  description,
  actionLabel,
  onAction,
  className = '',
}) {
  return (
    <div
      className={`flex flex-col items-center justify-center text-center py-16 px-6 ${className}`}
    >
      {Icon && (
        <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-5">
          <Icon size={32} className="text-gray-400" />
        </div>
      )}
      <h3 className="text-lg font-semibold text-gray-800 mb-2">{title}</h3>
      {description && (
        <p className="text-sm text-gray-500 max-w-sm mb-6">{description}</p>
      )}
      {actionLabel && onAction && (
        <Button variant="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
}

export default EmptyState;
