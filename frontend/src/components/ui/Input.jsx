import { forwardRef } from 'react';

const Input = forwardRef(
  (
    {
      label,
      error,
      helperText,
      icon: Icon,
      iconPosition = 'left',
      className = '',
      wrapperClassName = '',
      required,
      id,
      ...props
    },
    ref
  ) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
    const hasLeftIcon = Icon && iconPosition === 'left';
    const hasRightIcon = Icon && iconPosition === 'right';

    return (
      <div className={`flex flex-col gap-1 ${wrapperClassName}`}>
        {label && (
          <label
            htmlFor={inputId}
            className="text-sm font-medium text-gray-700"
          >
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}

        <div className="relative">
          {hasLeftIcon && (
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
              <Icon size={16} />
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            className={`
              w-full px-3 py-2.5 text-sm text-gray-900 bg-white border rounded-lg
              placeholder:text-gray-400
              focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500
              disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed
              transition-colors
              ${error ? 'border-red-400 focus:ring-red-400 focus:border-red-400' : 'border-gray-300'}
              ${hasLeftIcon ? 'pl-9' : ''}
              ${hasRightIcon ? 'pr-9' : ''}
              ${className}
            `}
            {...props}
          />

          {hasRightIcon && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
              <Icon size={16} />
            </div>
          )}
        </div>

        {error && (
          <p className="text-xs text-red-500 mt-0.5">{error}</p>
        )}
        {helperText && !error && (
          <p className="text-xs text-gray-500 mt-0.5">{helperText}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
