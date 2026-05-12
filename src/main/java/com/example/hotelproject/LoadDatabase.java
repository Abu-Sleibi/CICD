package com.example.hotelproject;

import com.example.hotelproject.auth.entity.Role;
import com.example.hotelproject.auth.entity.User;
import com.example.hotelproject.auth.repository.UserRepository;
import com.example.hotelproject.booking.entity.Guest;
import com.example.hotelproject.booking.repository.GuestRepository;
import com.example.hotelproject.catalog.hotel.Hotel;
import com.example.hotelproject.catalog.hotel.HotelRepository;
import com.example.hotelproject.catalog.room.RoomType;
import com.example.hotelproject.catalog.room.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            GuestRepository guestRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (hotelRepository.count() > 0) {
                log.info("Database already initialized, skipping seed data insertion.");
                return;
            }

            Hotel grandHotel = new Hotel("Grand Hotel Amman",
                    "Experience unparalleled luxury in the heart of Amman. Our 5-star hotel offers exquisite dining, a world-class spa, and stunning views of the city.",
                    "5th Circle, Zahran Street", "Amman", "Jordan");
            grandHotel.setPostalCode("11183");
            grandHotel.setPhone("+96265000001");
            grandHotel.setEmail("reservations@grandhotelamman.com");
            grandHotel.setStarRating(5);
            grandHotel.setActive(true);
            grandHotel.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80",
                    "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&q=80"
            ));

            Hotel seasideResort = new Hotel("Seaside Resort Aqaba",
                    "Escape to paradise at our beachfront resort in Aqaba. Enjoy crystal-clear waters, vibrant coral reefs, and unforgettable sunsets over the Red Sea.",
                    "South Beach Road", "Aqaba", "Jordan");
            seasideResort.setPostalCode("77110");
            seasideResort.setPhone("+96232000002");
            seasideResort.setEmail("info@seasideresort.com");
            seasideResort.setStarRating(4);
            seasideResort.setActive(true);
            seasideResort.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=80",
                    "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=800&q=80"
            ));

            Hotel mountainLodge = new Hotel("Petra Mountain Lodge",
                    "Nestled in the mountains near the ancient city of Petra, our lodge offers a cozy retreat with breathtaking views and warm hospitality.",
                    "Wadi Musa, Main Street", "Petra", "Jordan");
            mountainLodge.setPostalCode("71810");
            mountainLodge.setPhone("+96232150003");
            mountainLodge.setEmail("stay@petramountainlodge.com");
            mountainLodge.setStarRating(3);
            mountainLodge.setActive(true);
            mountainLodge.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1521401830884-6c03c1c87ebb?w=800&q=80",
                    "https://images.unsplash.com/photo-1586611292717-f828b167408c?w=800&q=80"
            ));

            Hotel deadSeaSpa = new Hotel("Dead Sea Spa Resort",
                    "Float in the mineral-rich waters of the Dead Sea and indulge in rejuvenating spa treatments at our premier wellness resort.",
                    "Dead Sea Road", "Sweimeh", "Jordan");
            deadSeaSpa.setPostalCode("11953");
            deadSeaSpa.setPhone("+96253400004");
            deadSeaSpa.setEmail("wellness@deadseaspa.com");
            deadSeaSpa.setStarRating(5);
            deadSeaSpa.setActive(true);
            deadSeaSpa.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800&q=80",
                    "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&q=80"
            ));

            Hotel downtownInn = new Hotel("Downtown Heritage Inn",
                    "Immerse yourself in the vibrant culture of downtown Amman. Our charming inn puts you steps away from the Roman Theatre, souks, and authentic restaurants.",
                    "King Faisal Street", "Amman", "Jordan");
            downtownInn.setPostalCode("11111");
            downtownInn.setPhone("+96264600005");
            downtownInn.setEmail("hello@downtowninn.com");
            downtownInn.setStarRating(3);
            downtownInn.setActive(true);
            downtownInn.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800&q=80",
                    "https://images.unsplash.com/photo-1568495248636-6432b97bd949?w=800&q=80"
            ));

            Hotel dubaiTower = new Hotel("Dubai Sky Tower Hotel",
                    "Soar above the city at our iconic tower hotel in Dubai's business district. Modern rooms, rooftop pool, and views of the Burj Khalifa.",
                    "Sheikh Zayed Road, Business Bay", "Dubai", "UAE");
            dubaiTower.setPostalCode("00000");
            dubaiTower.setPhone("+97143000006");
            dubaiTower.setEmail("info@dubaiskyhotel.com");
            dubaiTower.setStarRating(5);
            dubaiTower.setActive(true);
            dubaiTower.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&q=80",
                    "https://images.unsplash.com/photo-1605130284535-11dd9eedc58a?w=800&q=80"
            ));

            Hotel parisBlvd = new Hotel("Paris Boulevard Boutique",
                    "Charming boutique hotel steps from the Champs-Élysées. Enjoy Parisian elegance with modern comfort and world-class dining.",
                    "42 Boulevard Haussmann", "Paris", "France");
            parisBlvd.setPostalCode("75009");
            parisBlvd.setPhone("+33140000007");
            parisBlvd.setEmail("bonjour@parisblvdhotel.com");
            parisBlvd.setStarRating(4);
            parisBlvd.setActive(true);
            parisBlvd.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1455587734955-081b22074882?w=800&q=80",
                    "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&q=80"
            ));

            Hotel londonCourt = new Hotel("London Court Hotel",
                    "A classic London hotel located in the heart of Covent Garden. Proximity to West End theatres, museums, and iconic landmarks.",
                    "12 Strand, Covent Garden", "London", "UK");
            londonCourt.setPostalCode("WC2N 5JJ");
            londonCourt.setPhone("+442071000008");
            londonCourt.setEmail("stay@londoncourthotel.com");
            londonCourt.setStarRating(4);
            londonCourt.setActive(true);
            londonCourt.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1506059612708-99d6128b4e20?w=800&q=80",
                    "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800&q=80"
            ));

            Hotel nyMidtown = new Hotel("New York Midtown Suites",
                    "Premium suites in the heart of Manhattan. Walk to Times Square, Central Park, and the best dining New York has to offer.",
                    "555 5th Avenue, Midtown", "New York", "USA");
            nyMidtown.setPostalCode("10017");
            nyMidtown.setPhone("+12122000009");
            nyMidtown.setEmail("reservations@nymidtownsuites.com");
            nyMidtown.setStarRating(5);
            nyMidtown.setActive(true);
            nyMidtown.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?w=800&q=80",
                    "https://images.unsplash.com/photo-1541346183200-e8e117d945b4?w=800&q=80"
            ));

            Hotel tokyoZen = new Hotel("Tokyo Zen Garden Hotel",
                    "Experience authentic Japanese hospitality in Shinjuku. Traditional garden courtyard, onsen baths, and kaiseki cuisine.",
                    "3-7-1 Nishi-Shinjuku", "Tokyo", "Japan");
            tokyoZen.setPostalCode("160-0023");
            tokyoZen.setPhone("+81332000010");
            tokyoZen.setEmail("info@tokyozenhotel.com");
            tokyoZen.setStarRating(5);
            tokyoZen.setActive(true);
            tokyoZen.setImages(Arrays.asList(
                    "https://images.unsplash.com/photo-1542051841857-5f90071e7989?w=800&q=80",
                    "https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?w=800&q=80"
            ));

            log.info("Saving hotels...");
            hotelRepository.saveAll(List.of(grandHotel, seasideResort, mountainLodge, deadSeaSpa, downtownInn,
                    dubaiTower, parisBlvd, londonCourt, nyMidtown, tokyoZen));

            RoomType grandDeluxe = new RoomType("Deluxe King Room",
                    "Elegant room with a king-sized bed, marble bathroom, and stunning city views.",
                    2, new BigDecimal("220.00"), 25);
            grandDeluxe.setAmenities(Arrays.asList("King Bed", "Marble Bathroom", "City View", "Smart TV", "Mini Bar", "Espresso Machine", "Luxury Toiletries", "Bathrobe", "Slippers"));
            grandDeluxe.setHotel(grandHotel);

            RoomType grandExecutive = new RoomType("Executive Suite",
                    "Spacious suite with separate living area, dining table, and panoramic windows.",
                    3, new BigDecimal("450.00"), 8);
            grandExecutive.setAmenities(Arrays.asList("Separate Living Area", "Dining Table", "Panoramic Windows", "King Bed", "Jacuzzi", "Walk-in Closet", "Nespresso Machine", "Bose Sound System"));
            grandExecutive.setHotel(grandHotel);

            RoomType grandPresidential = new RoomType("Presidential Suite",
                    "The epitome of luxury. Features a private terrace, grand piano, and 24-hour butler service.",
                    4, new BigDecimal("1200.00"), 2);
            grandPresidential.setAmenities(Arrays.asList("Private Terrace", "Grand Piano", "Butler Service", "Private Elevator", "Steam Room", "Home Theater System", "Wine Fridge", "Billiard Table"));
            grandPresidential.setHotel(grandHotel);

            RoomType seasideOceanView = new RoomType("Ocean View Double",
                    "Wake up to breathtaking ocean views. Room features two double beds and a private balcony.",
                    3, new BigDecimal("180.00"), 20);
            seasideOceanView.setAmenities(Arrays.asList("Ocean View", "Private Balcony", "Two Double Beds", "Rain Shower", "Beach Towels", "Mini Fridge"));
            seasideOceanView.setHotel(seasideResort);

            RoomType seasideBeachBungalow = new RoomType("Beachfront Bungalow",
                    "Steps away from the sand. These private bungalows offer direct beach access and an outdoor shower.",
                    2, new BigDecimal("320.00"), 6);
            seasideBeachBungalow.setAmenities(Arrays.asList("Direct Beach Access", "Outdoor Shower", "King Bed", "Private Patio", "Beach Chairs", "Umbrella", "Hammock"));
            seasideBeachBungalow.setHotel(seasideResort);

            RoomType seasideFamilySuite = new RoomType("Family Suite with Sea View",
                    "Perfect for families. Two connecting rooms with a shared living area and stunning sea views.",
                    5, new BigDecimal("420.00"), 5);
            seasideFamilySuite.setAmenities(Arrays.asList("Two Connecting Rooms", "Sea View", "Living Area", "Kitchenette", "Two Bathrooms", "Bunk Beds for Kids"));
            seasideFamilySuite.setHotel(seasideResort);

            RoomType mountainStandard = new RoomType("Standard Mountain Room",
                    "Cozy room with rustic decor and views of the surrounding mountains.",
                    2, new BigDecimal("85.00"), 12);
            mountainStandard.setAmenities(Arrays.asList("Mountain View", "Queen Bed", "Heating", "Work Desk", "Free WiFi"));
            mountainStandard.setHotel(mountainLodge);

            RoomType mountainFamilyCabin = new RoomType("Family Cabin",
                    "Spacious wooden cabin with two bedrooms, a fireplace, and a small kitchen.",
                    4, new BigDecimal("150.00"), 4);
            mountainFamilyCabin.setAmenities(Arrays.asList("Two Bedrooms", "Fireplace", "Kitchenette", "Porch", "Mountain View", "Heating"));
            mountainFamilyCabin.setHotel(mountainLodge);

            RoomType deadSeaDeluxe = new RoomType("Deluxe Sea View Room",
                    "Modern room with breathtaking views of the Dead Sea. Includes access to our private beach and spa facilities.",
                    2, new BigDecimal("280.00"), 30);
            deadSeaDeluxe.setAmenities(Arrays.asList("Dead Sea View", "King Bed", "Spa Access", "Private Beach Access", "Rain Shower", "Mud Bath Products"));
            deadSeaDeluxe.setHotel(deadSeaSpa);

            RoomType deadSeaWellnessSuite = new RoomType("Wellness Suite",
                    "Designed for your well-being. Includes a private treatment room and in-suite spa bath.",
                    2, new BigDecimal("550.00"), 4);
            deadSeaWellnessSuite.setAmenities(Arrays.asList("Private Treatment Room", "Spa Bath", "Yoga Mat", "Meditation Area", "Herbal Tea Selection", "Aromatherapy Diffuser"));
            deadSeaWellnessSuite.setHotel(deadSeaSpa);

            RoomType downtownStandard = new RoomType("Standard City Room",
                    "Simple, clean, and comfortable room in the heart of downtown.",
                    2, new BigDecimal("60.00"), 15);
            downtownStandard.setAmenities(Arrays.asList("City View", "Queen Bed", "Air Conditioning", "Free WiFi", "Work Desk"));
            downtownStandard.setHotel(downtownInn);

            RoomType downtownTerraceRoom = new RoomType("Terrace Room with View",
                    "Room with a private terrace overlooking the bustling downtown streets.",
                    2, new BigDecimal("95.00"), 3);
            downtownTerraceRoom.setAmenities(Arrays.asList("Private Terrace", "City View", "King Bed", "Coffee Maker", "Local Artwork"));
            downtownTerraceRoom.setHotel(downtownInn);

            RoomType dubaiSkyRoom = new RoomType("Sky View King Room",
                    "Floor-to-ceiling windows with panoramic Dubai skyline views and luxury king bed.",
                    2, new BigDecimal("350.00"), 40);
            dubaiSkyRoom.setAmenities(Arrays.asList("Panoramic View", "King Bed", "Smart TV", "Mini Bar", "Marble Bathroom", "WiFi", "Gym Access"));
            dubaiSkyRoom.setHotel(dubaiTower);

            RoomType dubaiPenthouse = new RoomType("Executive Penthouse Suite",
                    "Two-level penthouse with private pool, butler service, and 360-degree views of Dubai.",
                    4, new BigDecimal("2500.00"), 2);
            dubaiPenthouse.setAmenities(Arrays.asList("Private Pool", "Butler Service", "360 View", "Home Theater", "Jacuzzi", "Private Elevator"));
            dubaiPenthouse.setHotel(dubaiTower);

            RoomType dubaiDeluxe = new RoomType("Deluxe Twin Room",
                    "Spacious twin room with city views, perfect for business travelers.",
                    2, new BigDecimal("250.00"), 30);
            dubaiDeluxe.setAmenities(Arrays.asList("City View", "Twin Beds", "Work Desk", "WiFi", "Mini Fridge", "Room Service"));
            dubaiDeluxe.setHotel(dubaiTower);

            RoomType parisClassic = new RoomType("Classic Parisian Room",
                    "Elegant room with Haussmann-style decor, French press coffee, and courtyard views.",
                    2, new BigDecimal("180.00"), 18);
            parisClassic.setAmenities(Arrays.asList("Courtyard View", "Queen Bed", "French Press", "WiFi", "Bathrobe", "Concierge"));
            parisClassic.setHotel(parisBlvd);

            RoomType parisJuniorSuite = new RoomType("Junior Suite with Balcony",
                    "Spacious suite with private balcony overlooking the boulevard and Eiffel Tower glimpses.",
                    2, new BigDecimal("320.00"), 6);
            parisJuniorSuite.setAmenities(Arrays.asList("Private Balcony", "Eiffel Tower View", "King Bed", "Separate Living Area", "Champagne on Arrival"));
            parisJuniorSuite.setHotel(parisBlvd);

            RoomType londonClassic = new RoomType("Classic Double Room",
                    "Cozy double room with English heritage decor, ideal for exploring the West End.",
                    2, new BigDecimal("160.00"), 20);
            londonClassic.setAmenities(Arrays.asList("Double Bed", "WiFi", "Tea & Coffee", "Work Desk", "En-suite Bathroom"));
            londonClassic.setHotel(londonCourt);

            RoomType londonFamily = new RoomType("Family Room",
                    "Spacious family room with two bedrooms and a shared lounge, close to family attractions.",
                    4, new BigDecimal("280.00"), 8);
            londonFamily.setAmenities(Arrays.asList("Two Bedrooms", "Lounge Area", "WiFi", "Tea & Coffee", "Cot Available", "Smart TV"));
            londonFamily.setHotel(londonCourt);

            RoomType nySuite = new RoomType("Manhattan Suite",
                    "Luxurious suite with Central Park views, kitchenette, and separate living area.",
                    3, new BigDecimal("550.00"), 12);
            nySuite.setAmenities(Arrays.asList("Central Park View", "King Bed", "Kitchenette", "Separate Living Area", "Nespresso", "Gym Access", "Concierge"));
            nySuite.setHotel(nyMidtown);

            RoomType nyStudio = new RoomType("Studio King Room",
                    "Modern studio room in Midtown with skyline views and premium amenities.",
                    2, new BigDecimal("380.00"), 25);
            nyStudio.setAmenities(Arrays.asList("Skyline View", "King Bed", "Smart TV", "Mini Bar", "WiFi", "Room Service"));
            nyStudio.setHotel(nyMidtown);

            RoomType tokyoTraditional = new RoomType("Traditional Tatami Room",
                    "Authentic Japanese room with futon bedding, shoji screens, and private garden access.",
                    2, new BigDecimal("240.00"), 10);
            tokyoTraditional.setAmenities(Arrays.asList("Tatami Floor", "Futon Bedding", "Garden View", "Onsen Access", "Yukata Provided", "Green Tea Set"));
            tokyoTraditional.setHotel(tokyoZen);

            RoomType tokyoModern = new RoomType("Modern Deluxe Room",
                    "Contemporary room blending Japanese aesthetics with Western comfort, featuring Mount Fuji view on clear days.",
                    2, new BigDecimal("290.00"), 20);
            tokyoModern.setAmenities(Arrays.asList("City View", "King Bed", "WiFi", "Smart TV", "Onsen Access", "Slippers & Yukata", "Minibar"));
            tokyoModern.setHotel(tokyoZen);

            log.info("Saving room types...");
            roomTypeRepository.saveAll(List.of(
                    grandDeluxe, grandExecutive, grandPresidential,
                    seasideOceanView, seasideBeachBungalow, seasideFamilySuite,
                    mountainStandard, mountainFamilyCabin,
                    deadSeaDeluxe, deadSeaWellnessSuite,
                    downtownStandard, downtownTerraceRoom,
                    dubaiSkyRoom, dubaiPenthouse, dubaiDeluxe,
                    parisClassic, parisJuniorSuite,
                    londonClassic, londonFamily,
                    nySuite, nyStudio,
                    tokyoTraditional, tokyoModern
            ));

            Guest guest1 = new Guest();
            guest1.setFullName("Ahmad Hassan");
            guest1.setEmail("ahmad.hassan@email.com");
            guest1.setPhone("+962791234567");
            guest1.setAddress("Amman, Jordan");

            Guest guest2 = new Guest();
            guest2.setFullName("Layla Hassan");
            guest2.setEmail("layla.hassan@email.com");
            guest2.setPhone("+962791234568");
            guest2.setAddress("Amman, Jordan");

            Guest guest3 = new Guest();
            guest3.setFullName("Omar Khaled");
            guest3.setEmail("omar.khaled@email.com");
            guest3.setPhone("+962792345678");
            guest3.setAddress("Dubai, UAE");

            Guest guest4 = new Guest();
            guest4.setFullName("Nadia Ahmad");
            guest4.setEmail("nadia.ahmad@email.com");
            guest4.setPhone("+962793456789");
            guest4.setAddress("London, UK");

            Guest guest5 = new Guest();
            guest5.setFullName("Hussein Ibrahim");
            guest5.setEmail("hussein.ibrahim@email.com");
            guest5.setPhone("+962794567890");
            guest5.setAddress("New York, USA");

            Guest guest6 = new Guest();
            guest6.setFullName("Rania Yousef");
            guest6.setEmail("rania.yousef@email.com");
            guest6.setPhone("+962795678901");
            guest6.setAddress("Riyadh, KSA");

            log.info("Saving guests...");
            guestRepository.saveAll(List.of(guest1, guest2, guest3, guest4, guest5, guest6));

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@hotelbooking.com");
            adminUser.setPassword(passwordEncoder.encode("Admin123!"));
            adminUser.setFullName("System Administrator");
            adminUser.setRoles(Set.of(Role.ADMIN));
            adminUser.setPhone("+962799999999");
            adminUser.setEnabled(true);

            User manager1 = new User();
            manager1.setUsername("manager.grand");
            manager1.setEmail("gm@grandhotelamman.com");
            manager1.setPassword(passwordEncoder.encode("Manager123!"));
            manager1.setFullName("Ahmad Mansour");
            manager1.setRoles(Set.of(Role.HOTEL_MANAGER));
            manager1.setHotelId(grandHotel.getId());
            manager1.setPhone("+962798888888");
            manager1.setEnabled(true);

            User manager2 = new User();
            manager2.setUsername("manager.seaside");
            manager2.setEmail("gm@seasideresort.com");
            manager2.setPassword(passwordEncoder.encode("Manager123!"));
            manager2.setFullName("Lina Khalil");
            manager2.setRoles(Set.of(Role.HOTEL_MANAGER));
            manager2.setHotelId(seasideResort.getId());
            manager2.setPhone("+962797777777");
            manager2.setEnabled(true);

            User manager3 = new User();
            manager3.setUsername("manager.mountain");
            manager3.setEmail("gm@petramountainlodge.com");
            manager3.setPassword(passwordEncoder.encode("Manager123!"));
            manager3.setFullName("Khalid Yousef");
            manager3.setRoles(Set.of(Role.HOTEL_MANAGER));
            manager3.setHotelId(mountainLodge.getId());
            manager3.setPhone("+962796666666");
            manager3.setEnabled(true);

            User guestUser1 = new User();
            guestUser1.setUsername("ahmad_h");
            guestUser1.setEmail("ahmad.hassan@email.com");
            guestUser1.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser1.setFullName("Ahmad Hassan");
            guestUser1.setRoles(Set.of(Role.GUEST));
            guestUser1.setPhone("+962791234567");
            guestUser1.setEnabled(true);

            User guestUser2 = new User();
            guestUser2.setUsername("layla_h");
            guestUser2.setEmail("layla.hassan@email.com");
            guestUser2.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser2.setFullName("Layla Hassan");
            guestUser2.setRoles(Set.of(Role.GUEST));
            guestUser2.setPhone("+962791234568");
            guestUser2.setEnabled(true);

            User guestUser3 = new User();
            guestUser3.setUsername("omar_k");
            guestUser3.setEmail("omar.khaled@email.com");
            guestUser3.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser3.setFullName("Omar Khaled");
            guestUser3.setRoles(Set.of(Role.GUEST));
            guestUser3.setPhone("+962792345678");
            guestUser3.setEnabled(true);

            User guestUser4 = new User();
            guestUser4.setUsername("nadia_a");
            guestUser4.setEmail("nadia.ahmad@email.com");
            guestUser4.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser4.setFullName("Nadia Ahmad");
            guestUser4.setRoles(Set.of(Role.GUEST));
            guestUser4.setPhone("+962793456789");
            guestUser4.setEnabled(true);

            User guestUser5 = new User();
            guestUser5.setUsername("hussein_i");
            guestUser5.setEmail("hussein.ibrahim@email.com");
            guestUser5.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser5.setFullName("Hussein Ibrahim");
            guestUser5.setRoles(Set.of(Role.GUEST));
            guestUser5.setPhone("+962794567890");
            guestUser5.setEnabled(true);

            User guestUser6 = new User();
            guestUser6.setUsername("rania_y");
            guestUser6.setEmail("rania.yousef@email.com");
            guestUser6.setPassword(passwordEncoder.encode("Guest123!"));
            guestUser6.setFullName("Rania Yousef");
            guestUser6.setRoles(Set.of(Role.GUEST));
            guestUser6.setPhone("+962795678901");
            guestUser6.setEnabled(true);

            log.info("Saving users...");
            userRepository.saveAll(List.of(
                    adminUser, manager1, manager2, manager3,
                    guestUser1, guestUser2, guestUser3, guestUser4, guestUser5, guestUser6
            ));

            log.info("Database initialization completed successfully!");
        };
    }
}