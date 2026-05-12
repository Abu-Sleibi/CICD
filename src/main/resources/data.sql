-- Hotel seed data (10 hotels across different cities)
-- This file is provided as a SQL reference. The application uses LoadDatabase.java for seeding.
-- To run this file directly, set: spring.sql.init.mode=always and spring.jpa.defer-datasource-initialization=true

INSERT IGNORE INTO hotels (id, name, description, address, city, country, postal_code, phone, email, star_rating, active, created_at, updated_at) VALUES
(1,  'Grand Hotel Amman',        'Experience unparalleled luxury in the heart of Amman. Our 5-star hotel offers exquisite dining, a world-class spa, and stunning views of the city.',             '5th Circle, Zahran Street',        'Amman',    'Jordan',  '11183',     '+96265000001', 'reservations@grandhotelamman.com', 5, true, NOW(), NOW()),
(2,  'Seaside Resort Aqaba',     'Escape to paradise at our beachfront resort in Aqaba. Enjoy crystal-clear waters, vibrant coral reefs, and unforgettable sunsets over the Red Sea.',            'South Beach Road',                 'Aqaba',    'Jordan',  '77110',     '+96232000002', 'info@seasideresort.com',           4, true, NOW(), NOW()),
(3,  'Petra Mountain Lodge',     'Nestled in the mountains near the ancient city of Petra, our lodge offers a cozy retreat with breathtaking views and warm hospitality.',                        'Wadi Musa, Main Street',           'Petra',    'Jordan',  '71810',     '+96232150003', 'stay@petramountainlodge.com',      3, true, NOW(), NOW()),
(4,  'Dead Sea Spa Resort',      'Float in the mineral-rich waters of the Dead Sea and indulge in rejuvenating spa treatments at our premier wellness resort.',                                   'Dead Sea Road',                    'Sweimeh',  'Jordan',  '11953',     '+96253400004', 'wellness@deadseaspa.com',          5, true, NOW(), NOW()),
(5,  'Downtown Heritage Inn',    'Immerse yourself in the vibrant culture of downtown Amman. Our charming inn puts you steps away from the Roman Theatre, souks, and authentic restaurants.',    'King Faisal Street',               'Amman',    'Jordan',  '11111',     '+96264600005', 'hello@downtowninn.com',            3, true, NOW(), NOW()),
(6,  'Dubai Sky Tower Hotel',    'Soar above the city at our iconic tower hotel in Dubai''s business district. Modern rooms, rooftop pool, and views of the Burj Khalifa.',                     'Sheikh Zayed Road, Business Bay',  'Dubai',    'UAE',     '00000',     '+97143000006', 'info@dubaiskyhotel.com',           5, true, NOW(), NOW()),
(7,  'Paris Boulevard Boutique', 'Charming boutique hotel steps from the Champs-Élysées. Enjoy Parisian elegance with modern comfort and world-class dining.',                                   '42 Boulevard Haussmann',           'Paris',    'France',  '75009',     '+33140000007', 'bonjour@parisblvdhotel.com',       4, true, NOW(), NOW()),
(8,  'London Court Hotel',       'A classic London hotel located in the heart of Covent Garden. Proximity to West End theatres, museums, and iconic landmarks.',                                 '12 Strand, Covent Garden',         'London',   'UK',      'WC2N 5JJ',  '+442071000008','stay@londoncourthotel.com',        4, true, NOW(), NOW()),
(9,  'New York Midtown Suites',  'Premium suites in the heart of Manhattan. Walk to Times Square, Central Park, and the best dining New York has to offer.',                                     '555 5th Avenue, Midtown',          'New York', 'USA',     '10017',     '+12122000009', 'reservations@nymidtownsuites.com', 5, true, NOW(), NOW()),
(10, 'Tokyo Zen Garden Hotel',   'Experience authentic Japanese hospitality in Shinjuku. Traditional garden courtyard, onsen baths, and kaiseki cuisine.',                                       '3-7-1 Nishi-Shinjuku',             'Tokyo',    'Japan',   '160-0023',  '+81332000010', 'info@tokyozenhotel.com',           5, true, NOW(), NOW());

-- Room types (2-4 per hotel)
INSERT IGNORE INTO room_types (id, name, description, capacity, base_price, total_rooms, hotel_id, active) VALUES
(1,  'Deluxe King Room',             'Elegant room with a king-sized bed, marble bathroom, and stunning city views.',                                             2, 220.00,  25, 1, true),
(2,  'Executive Suite',              'Spacious suite with separate living area, dining table, and panoramic windows.',                                            3, 450.00,   8, 1, true),
(3,  'Presidential Suite',           'The epitome of luxury. Features a private terrace, grand piano, and 24-hour butler service.',                              4, 1200.00,  2, 1, true),
(4,  'Ocean View Double',            'Wake up to breathtaking ocean views. Room features two double beds and a private balcony.',                                  3, 180.00,  20, 2, true),
(5,  'Beachfront Bungalow',          'Steps away from the sand. These private bungalows offer direct beach access and an outdoor shower.',                        2, 320.00,   6, 2, true),
(6,  'Family Suite with Sea View',   'Perfect for families. Two connecting rooms with a shared living area and stunning sea views.',                              5, 420.00,   5, 2, true),
(7,  'Standard Mountain Room',       'Cozy room with rustic decor and views of the surrounding mountains.',                                                       2,  85.00,  12, 3, true),
(8,  'Family Cabin',                 'Spacious wooden cabin with two bedrooms, a fireplace, and a small kitchen.',                                                4, 150.00,   4, 3, true),
(9,  'Deluxe Sea View Room',         'Modern room with breathtaking views of the Dead Sea. Includes access to our private beach and spa facilities.',             2, 280.00,  30, 4, true),
(10, 'Wellness Suite',               'Designed for your well-being. Includes a private treatment room and in-suite spa bath.',                                    2, 550.00,   4, 4, true),
(11, 'Standard City Room',           'Simple, clean, and comfortable room in the heart of downtown.',                                                             2,  60.00,  15, 5, true),
(12, 'Terrace Room with View',       'Room with a private terrace overlooking the bustling downtown streets.',                                                    2,  95.00,   3, 5, true),
(13, 'Sky View King Room',           'Floor-to-ceiling windows with panoramic Dubai skyline views and luxury king bed.',                                          2, 350.00,  40, 6, true),
(14, 'Executive Penthouse Suite',    'Two-level penthouse with private pool, butler service, and 360-degree views of Dubai.',                                     4, 2500.00,  2, 6, true),
(15, 'Deluxe Twin Room',             'Spacious twin room with city views, perfect for business travelers.',                                                       2, 250.00,  30, 6, true),
(16, 'Classic Parisian Room',        'Elegant room with Haussmann-style decor, French press coffee, and courtyard views.',                                        2, 180.00,  18, 7, true),
(17, 'Junior Suite with Balcony',    'Spacious suite with private balcony overlooking the boulevard and Eiffel Tower glimpses.',                                  2, 320.00,   6, 7, true),
(18, 'Classic Double Room',          'Cozy double room with English heritage decor, ideal for exploring the West End.',                                           2, 160.00,  20, 8, true),
(19, 'Family Room',                  'Spacious family room with two bedrooms and a shared lounge, close to family attractions.',                                  4, 280.00,   8, 8, true),
(20, 'Manhattan Suite',              'Luxurious suite with Central Park views, kitchenette, and separate living area.',                                           3, 550.00,  12, 9, true),
(21, 'Studio King Room',             'Modern studio room in Midtown with skyline views and premium amenities.',                                                   2, 380.00,  25, 9, true),
(22, 'Traditional Tatami Room',      'Authentic Japanese room with futon bedding, shoji screens, and private garden access.',                                    2, 240.00,  10, 10, true),
(23, 'Modern Deluxe Room',           'Contemporary room blending Japanese aesthetics with Western comfort, featuring Mount Fuji view on clear days.',             2, 290.00,  20, 10, true);

-- Room type amenities
INSERT IGNORE INTO room_type_amenities (room_type_id, amenity) VALUES
(1,'King Bed'),(1,'Marble Bathroom'),(1,'City View'),(1,'Smart TV'),(1,'Mini Bar'),(1,'Espresso Machine'),(1,'Luxury Toiletries'),
(2,'Separate Living Area'),(2,'Dining Table'),(2,'Panoramic Windows'),(2,'King Bed'),(2,'Jacuzzi'),(2,'Nespresso Machine'),
(3,'Private Terrace'),(3,'Grand Piano'),(3,'Butler Service'),(3,'Private Elevator'),(3,'Steam Room'),(3,'Wine Fridge'),
(4,'Ocean View'),(4,'Private Balcony'),(4,'Two Double Beds'),(4,'Rain Shower'),(4,'Beach Towels'),(4,'Mini Fridge'),
(5,'Direct Beach Access'),(5,'Outdoor Shower'),(5,'King Bed'),(5,'Private Patio'),(5,'Beach Chairs'),(5,'Hammock'),
(6,'Two Connecting Rooms'),(6,'Sea View'),(6,'Living Area'),(6,'Kitchenette'),(6,'Two Bathrooms'),(6,'Bunk Beds for Kids'),
(7,'Mountain View'),(7,'Queen Bed'),(7,'Heating'),(7,'Work Desk'),(7,'Free WiFi'),
(8,'Two Bedrooms'),(8,'Fireplace'),(8,'Kitchenette'),(8,'Porch'),(8,'Mountain View'),(8,'Heating'),
(9,'Dead Sea View'),(9,'King Bed'),(9,'Spa Access'),(9,'Private Beach Access'),(9,'Rain Shower'),(9,'Mud Bath Products'),
(10,'Private Treatment Room'),(10,'Spa Bath'),(10,'Yoga Mat'),(10,'Meditation Area'),(10,'Herbal Tea Selection'),
(11,'City View'),(11,'Queen Bed'),(11,'Air Conditioning'),(11,'Free WiFi'),(11,'Work Desk'),
(12,'Private Terrace'),(12,'City View'),(12,'King Bed'),(12,'Coffee Maker'),
(13,'Panoramic View'),(13,'King Bed'),(13,'Smart TV'),(13,'Mini Bar'),(13,'Marble Bathroom'),(13,'WiFi'),(13,'Gym Access'),
(14,'Private Pool'),(14,'Butler Service'),(14,'360 View'),(14,'Home Theater'),(14,'Jacuzzi'),(14,'Private Elevator'),
(15,'City View'),(15,'Twin Beds'),(15,'Work Desk'),(15,'WiFi'),(15,'Mini Fridge'),(15,'Room Service'),
(16,'Courtyard View'),(16,'Queen Bed'),(16,'French Press'),(16,'WiFi'),(16,'Bathrobe'),(16,'Concierge'),
(17,'Private Balcony'),(17,'Eiffel Tower View'),(17,'King Bed'),(17,'Separate Living Area'),(17,'Champagne on Arrival'),
(18,'Double Bed'),(18,'WiFi'),(18,'Tea & Coffee'),(18,'Work Desk'),(18,'En-suite Bathroom'),
(19,'Two Bedrooms'),(19,'Lounge Area'),(19,'WiFi'),(19,'Tea & Coffee'),(19,'Smart TV'),
(20,'Central Park View'),(20,'King Bed'),(20,'Kitchenette'),(20,'Separate Living Area'),(20,'Nespresso'),(20,'Gym Access'),
(21,'Skyline View'),(21,'King Bed'),(21,'Smart TV'),(21,'Mini Bar'),(21,'WiFi'),(21,'Room Service'),
(22,'Tatami Floor'),(22,'Futon Bedding'),(22,'Garden View'),(22,'Onsen Access'),(22,'Yukata Provided'),(22,'Green Tea Set'),
(23,'City View'),(23,'King Bed'),(23,'WiFi'),(23,'Smart TV'),(23,'Onsen Access'),(23,'Slippers & Yukata'),(23,'Minibar');
