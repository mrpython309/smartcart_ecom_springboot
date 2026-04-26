import { useState } from 'react';

const SafeImage = ({ src, alt, className, ...props }) => {
  const [error, setError] = useState(false);
  
  const fallback = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" fill="%23f3f4f6"><rect width="100%" height="100%"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-family="system-ui" font-size="20" fill="%239ca3af">Image Not Available</text></svg>';

  return (
    <img
      src={error || !src ? fallback : src}
      alt={alt}
      className={className}
      onError={() => setError(true)}
      loading="lazy"
      {...props}
    />
  );
};

export default SafeImage;
