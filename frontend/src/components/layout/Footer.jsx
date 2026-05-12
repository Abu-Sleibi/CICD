import { Link } from 'react-router-dom';
import { Github, Linkedin, Instagram } from 'lucide-react';

function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="bg-gray-900 text-gray-400">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="md:col-span-1">
            <Link to="/" className="flex flex-col items-start no-underline mb-1 leading-none">
              <span style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.15em', color: '#C9A84C', fontSize: '1.4rem' }}>
                VELOUR
              </span>
              <span style={{ fontStyle: 'italic', fontSize: '0.7rem', letterSpacing: '0.05em', color: '#9ca3af', marginTop: '2px' }}>
                Redefine Your Stay
              </span>
            </Link>
            <p className="text-sm leading-relaxed mt-4">
              Discover and book the world's finest hotels with ease. Your perfect stay is just a click away.
            </p>
          </div>

          {/* Explore */}
          <div>
            <h3 className="text-white font-semibold mb-4">Explore</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/search" className="hover:text-amber-400 transition-colors no-underline">
                  Search Hotels
                </Link>
              </li>
              <li>
                <Link to="/search?rating=5" className="hover:text-amber-400 transition-colors no-underline">
                  5-Star Hotels
                </Link>
              </li>
              <li>
                <Link to="/search?city=New York" className="hover:text-amber-400 transition-colors no-underline">
                  New York
                </Link>
              </li>
              <li>
                <Link to="/search?city=Paris" className="hover:text-amber-400 transition-colors no-underline">
                  Paris
                </Link>
              </li>
            </ul>
          </div>

          {/* Account */}
          <div>
            <h3 className="text-white font-semibold mb-4">Account</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/login" className="hover:text-amber-400 transition-colors no-underline">
                  Login
                </Link>
              </li>
              <li>
                <Link to="/register" className="hover:text-amber-400 transition-colors no-underline">
                  Register
                </Link>
              </li>
              <li>
                <Link to="/my-bookings" className="hover:text-amber-400 transition-colors no-underline">
                  My Bookings
                </Link>
              </li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h3 className="text-white font-semibold mb-4">Support</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <a href="#" className="hover:text-amber-400 transition-colors no-underline">
                  Help Center
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-amber-400 transition-colors no-underline">
                  Cancellation Policy
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-amber-400 transition-colors no-underline">
                  Contact Us
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-amber-400 transition-colors no-underline">
                  Privacy Policy
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="border-t border-gray-800 mt-10 pt-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-sm">
            &copy; 2026 VELOUR. All rights reserved.
          </p>
          <div className="flex items-center gap-4">
            <a
              href="https://github.com"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-amber-400 transition-colors"
              aria-label="GitHub"
            >
              <Github size={20} />
            </a>
            <a
              href="https://linkedin.com"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-amber-400 transition-colors"
              aria-label="LinkedIn"
            >
              <Linkedin size={20} />
            </a>
            <a
              href="https://instagram.com"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-amber-400 transition-colors"
              aria-label="Instagram"
            >
              <Instagram size={20} />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
