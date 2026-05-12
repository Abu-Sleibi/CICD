import { Github, Linkedin } from 'lucide-react';

const team = [
  {
    name: 'George Sleibi',
    role: 'Full-Stack Developer',
    initials: 'GS',
    color: 'from-amber-400 to-orange-400',
    bio: 'Passionate about building scalable Spring Boot APIs and modern React frontends.',
    github: '#',
    linkedin: '#',
  },
  {
    name: 'Francis Lolas',
    role: 'Full-Stack Developer',
    initials: 'FL',
    color: 'from-blue-400 to-indigo-500',
    bio: 'Focused on clean architecture, RESTful services, and delivering great user experiences.',
    github: '#',
    linkedin: '#',
  },
];

function TeamSection() {
  return (
    <section className="py-20 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Headline */}
        <div className="text-center mb-14">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
            Meet Our <span className="text-amber-500">Team</span>
          </h2>
          <p className="text-lg text-gray-500 max-w-xl mx-auto">
            A passionate group of developers and designers dedicated to transforming how you discover and book hotels.
          </p>
        </div>

        {/* Team grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 max-w-2xl mx-auto gap-6">
          {team.map(({ name, role, initials, color, bio, github, linkedin }) => (
            <div
              key={name}
              className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow text-center group"
            >
              {/* Avatar */}
              <div
                className={`w-20 h-20 rounded-2xl bg-gradient-to-br ${color} flex items-center justify-center text-white text-2xl font-bold mx-auto mb-4 group-hover:scale-105 transition-transform`}
              >
                {initials}
              </div>

              <h3 className="font-semibold text-gray-900 text-base mb-0.5">{name}</h3>
              <p className="text-sm text-amber-500 font-medium mb-3">{role}</p>
              <p className="text-xs text-gray-500 leading-relaxed mb-4">{bio}</p>

              {/* Social links */}
              <div className="flex items-center justify-center gap-3">
                <a
                  href={github}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
                  aria-label={`${name} GitHub`}
                >
                  <Github size={16} />
                </a>
                <a
                  href={linkedin}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-lg text-gray-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                  aria-label={`${name} LinkedIn`}
                >
                  <Linkedin size={16} />
                </a>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default TeamSection;
