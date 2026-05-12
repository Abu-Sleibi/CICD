import { Tag, RotateCcw, Headphones, Shield, Star, Globe } from 'lucide-react';

const features = [
  {
    icon: Tag,
    title: 'Best Prices',
    description:
      "We guarantee the lowest prices across thousands of hotels worldwide. Find a lower price and we'll match it.",
    color: 'bg-amber-50 text-amber-600',
  },
  {
    icon: RotateCcw,
    title: 'Free Cancellation',
    description:
      'Change your plans? No problem. Most of our hotels offer free cancellation up to 24 hours before check-in.',
    color: 'bg-green-50 text-green-600',
  },
  {
    icon: Headphones,
    title: '24/7 Support',
    description:
      'Our dedicated support team is available around the clock to assist you with any questions or issues.',
    color: 'bg-blue-50 text-blue-600',
  },
  {
    icon: Shield,
    title: 'Secure Booking',
    description:
      'All transactions are protected with industry-standard SSL encryption. Your data is always safe with us.',
    color: 'bg-purple-50 text-purple-600',
  },
  {
    icon: Star,
    title: 'Verified Reviews',
    description:
      'All reviews are from verified guests. Read honest opinions from real travelers before you book.',
    color: 'bg-yellow-50 text-yellow-600',
  },
  {
    icon: Globe,
    title: 'Global Reach',
    description:
      'Explore hotels in over 150 countries. From budget stays to luxury resorts — we have it all.',
    color: 'bg-rose-50 text-rose-600',
  },
];

function AboutSection() {
  return (
    <section id="about" className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Headline */}
        <div className="text-center mb-14">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
            Why Choose{' '}
            <span style={{ fontFamily: "'Playfair Display', Georgia, serif", color: '#C9A84C', letterSpacing: '0.1em' }}>VELOUR</span>?
          </h2>
          <p className="text-lg text-gray-500 max-w-2xl mx-auto">
            We're committed to making your hotel search and booking experience as easy,
            transparent, and enjoyable as possible.
          </p>
        </div>

        {/* Feature grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map(({ icon: Icon, title, description, color }) => (
            <div
              key={title}
              className="p-6 rounded-2xl border border-gray-100 hover:shadow-md transition-shadow group"
            >
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${color}`}>
                <Icon size={22} />
              </div>
              <h3 className="font-semibold text-gray-900 text-base mb-2">{title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{description}</p>
            </div>
          ))}
        </div>

        {/* Stats row */}
        <div className="mt-16 grid grid-cols-2 sm:grid-cols-4 gap-6">
          {[
            { value: '10,000+', label: 'Hotels' },
            { value: '150+', label: 'Countries' },
            { value: '2M+', label: 'Happy Guests' },
            { value: '4.9★', label: 'Average Rating' },
          ].map(({ value, label }) => (
            <div key={label} className="text-center">
              <p className="text-3xl font-extrabold text-amber-500 mb-1">{value}</p>
              <p className="text-sm text-gray-500">{label}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default AboutSection;
