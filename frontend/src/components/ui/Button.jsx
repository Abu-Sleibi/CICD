import { Loader2 } from 'lucide-react';
import { forwardRef } from 'react';

const variantClasses = {
  primary:
    'bg-amber-500 text-white hover:bg-amber-600 active:bg-amber-700 disabled:bg-amber-300 focus:ring-amber-500',
  secondary:
    'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 active:bg-gray-100 disabled:opacity-50 focus:ring-gray-400',
  danger:
    'bg-red-500 text-white hover:bg-red-600 active:bg-red-700 disabled:bg-red-300 focus:ring-red-500',
  ghost:
    'bg-transparent text-gray-600 hover:bg-gray-100 active:bg-gray-200 disabled:opacity-50 focus:ring-gray-400',
};

const sizeClasses = {
  sm: 'px-3 py-1.5 text-xs rounded-lg',
  md: 'px-4 py-2 text-sm rounded-lg',
  lg: 'px-6 py-3 text-base rounded-xl',
};

const Button = forwardRef(
  (
    {
      variant = 'primary',
      size = 'md',
      loading = false,
      disabled = false,
      children,
      className = '',
      leftIcon,
      rightIcon,
      ...props
    },
    ref
  ) => {
    const isDisabled = disabled || loading;

    return (
      <button
        ref={ref}
        disabled={isDisabled}
        className={`
          inline-flex items-center justify-center gap-2 font-medium transition-colors
          focus:outline-none focus:ring-2 focus:ring-offset-2
          disabled:cursor-not-allowed select-none
          ${variantClasses[variant] || variantClasses.primary}
          ${sizeClasses[size] || sizeClasses.md}
          ${className}
        `}
        {...props}
      >
        {loading ? (
          <Loader2 size={size === 'sm' ? 14 : size === 'lg' ? 20 : 16} className="animate-spin" />
        ) : (
          leftIcon && <span className="flex-shrink-0">{leftIcon}</span>
        )}
        {children}
        {!loading && rightIcon && <span className="flex-shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;
