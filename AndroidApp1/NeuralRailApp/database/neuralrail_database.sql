-- =====================================================
-- NeuralRail Database Schema for Supabase (PostgreSQL)
-- Sustainable Travel Companion App
-- Compatible with Supabase Free Tier
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- DROP EXISTING OBJECTS (for clean setup)
-- =====================================================

DROP TABLE IF EXISTS quiz_answers CASCADE;
DROP TABLE IF EXISTS quiz_questions CASCADE;
DROP TABLE IF EXISTS education_content CASCADE;
DROP TABLE IF EXISTS facts_of_the_day CASCADE;
DROP TABLE IF EXISTS energy_reports CASCADE;
DROP TABLE IF EXISTS report_upvotes CASCADE;
DROP TABLE IF EXISTS user_contributions CASCADE;
DROP TABLE IF EXISTS offset_projects CASCADE;
DROP TABLE IF EXISTS challenge_completions CASCADE;
DROP TABLE IF EXISTS daily_challenges CASCADE;
DROP TABLE IF EXISTS user_badges CASCADE;
DROP TABLE IF EXISTS badges CASCADE;
DROP TABLE IF EXISTS eco_trips CASCADE;
DROP TABLE IF EXISTS station_stops CASCADE;
DROP TABLE IF EXISTS train_status CASCADE;
DROP TABLE IF EXISTS trains CASCADE;
DROP TABLE IF EXISTS stations CASCADE;
DROP TABLE IF EXISTS smart_routes CASCADE;
DROP TABLE IF EXISTS city_energy_summary CASCADE;
DROP TABLE IF EXISTS weekly_leaderboard CASCADE;
DROP TABLE IF EXISTS qr_scans CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS user_preferences CASCADE;
DROP TABLE IF EXISTS user_sessions CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop custom types if exist
DROP TYPE IF EXISTS train_type CASCADE;
DROP TYPE IF EXISTS train_status_type CASCADE;
DROP TYPE IF EXISTS delay_reason_type CASCADE;
DROP TYPE IF EXISTS stop_status_type CASCADE;
DROP TYPE IF EXISTS transport_mode_type CASCADE;
DROP TYPE IF EXISTS badge_type CASCADE;
DROP TYPE IF EXISTS challenge_type CASCADE;
DROP TYPE IF EXISTS project_type CASCADE;
DROP TYPE IF EXISTS report_type CASCADE;
DROP TYPE IF EXISTS report_status_type CASCADE;
DROP TYPE IF EXISTS congestion_type CASCADE;
DROP TYPE IF EXISTS content_type CASCADE;
DROP TYPE IF EXISTS difficulty_type CASCADE;
DROP TYPE IF EXISTS scan_type CASCADE;
DROP TYPE IF EXISTS booking_status_type CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;

-- =====================================================
-- CREATE CUSTOM ENUM TYPES
-- =====================================================

CREATE TYPE train_type AS ENUM ('EXPRESS', 'SUPERFAST', 'RAJDHANI', 'SHATABDI', 'LOCAL', 'METRO', 'FREIGHT');
CREATE TYPE train_status_type AS ENUM ('ON_TIME', 'DELAYED', 'STOPPED', 'CANCELLED', 'DIVERTED', 'RESCHEDULED');
CREATE TYPE delay_reason_type AS ENUM ('SIGNAL_FAILURE', 'TRACK_MAINTENANCE', 'WEATHER_CONDITIONS', 'TECHNICAL_ISSUE', 
                                       'PASSENGER_EMERGENCY', 'SECURITY_CHECK', 'CONGESTION', 'ACCIDENT_AHEAD', 
                                       'POWER_FAILURE', 'CREW_CHANGE', 'UNKNOWN');
CREATE TYPE stop_status_type AS ENUM ('COMPLETED', 'CURRENT', 'UPCOMING', 'SKIPPED');
CREATE TYPE transport_mode_type AS ENUM ('RAIL', 'METRO', 'BUS', 'WALK', 'CYCLE');
CREATE TYPE badge_type AS ENUM ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'SPECIAL');
CREATE TYPE challenge_type AS ENUM ('TRAVEL', 'WALK', 'OFF_PEAK', 'SHARE', 'REPORT');
CREATE TYPE project_type AS ENUM ('SOLAR', 'REFORESTATION', 'EV_CHARGING', 'WIND');
CREATE TYPE report_type AS ENUM ('LIGHTS_ON', 'IDLING_ENGINE', 'FAULTY_SOLAR', 'AC_WASTE', 'OTHER');
CREATE TYPE report_status_type AS ENUM ('PENDING', 'INVESTIGATING', 'RESOLVED');
CREATE TYPE congestion_type AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE content_type AS ENUM ('ARTICLE', 'INFOGRAPHIC', 'VIDEO', 'QUIZ');
CREATE TYPE difficulty_type AS ENUM ('EASY', 'MEDIUM', 'HARD');
CREATE TYPE scan_type AS ENUM ('TRAIN_INFO', 'TICKET', 'STATION', 'UNKNOWN');
CREATE TYPE booking_status_type AS ENUM ('CONFIRMED', 'WAITING', 'RAC', 'CANCELLED');
CREATE TYPE notification_type AS ENUM ('TRAIN_DELAY', 'CHALLENGE', 'BADGE', 'OFFSET', 'GENERAL');
CREATE TYPE preferred_mode_type AS ENUM ('RAIL', 'METRO', 'BUS', 'ANY');

-- =====================================================
-- USERS & AUTHENTICATION
-- =====================================================

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    total_co2_saved DECIMAL(10,2) DEFAULT 0.00,
    total_trips INT DEFAULT 0,
    streak_days INT DEFAULT 0,
    total_points INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    last_login TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_sessions (
    session_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- STATIONS & TRAINS
-- =====================================================

CREATE TABLE stations (
    station_id SERIAL PRIMARY KEY,
    station_code VARCHAR(10) NOT NULL UNIQUE,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    platforms INT DEFAULT 1,
    is_green_station BOOLEAN DEFAULT FALSE,
    solar_capacity_kw DECIMAL(10,2) DEFAULT 0.00,
    amenities JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE trains (
    train_id SERIAL PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL UNIQUE,
    train_name VARCHAR(100) NOT NULL,
    train_type train_type NOT NULL,
    source_station_id INT REFERENCES stations(station_id),
    destination_station_id INT REFERENCES stations(station_id),
    total_distance_km DECIMAL(10,2),
    is_electric BOOLEAN DEFAULT TRUE,
    energy_efficiency_rating DECIMAL(3,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE train_status (
    status_id SERIAL PRIMARY KEY,
    train_id INT NOT NULL REFERENCES trains(train_id),
    current_status train_status_type NOT NULL,
    current_location VARCHAR(255),
    current_station_id INT REFERENCES stations(station_id),
    next_station_id INT REFERENCES stations(station_id),
    expected_arrival TIME,
    delay_minutes INT DEFAULT 0,
    delay_reason delay_reason_type,
    current_energy_usage_kwh DECIMAL(10,2),
    regenerative_braking_recovery DECIMAL(5,4),
    renewable_energy_percent DECIMAL(5,4),
    last_updated TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE station_stops (
    stop_id SERIAL PRIMARY KEY,
    train_id INT NOT NULL REFERENCES trains(train_id),
    station_id INT NOT NULL REFERENCES stations(station_id),
    stop_sequence INT NOT NULL,
    scheduled_arrival TIME,
    scheduled_departure TIME,
    actual_arrival TIME,
    actual_departure TIME,
    platform VARCHAR(10),
    stop_status stop_status_type DEFAULT 'UPCOMING'
);

-- =====================================================
-- ECO COMMUTE & TRIPS
-- =====================================================

CREATE TABLE eco_trips (
    trip_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    trip_date DATE NOT NULL,
    from_station_id INT REFERENCES stations(station_id),
    to_station_id INT REFERENCES stations(station_id),
    from_location VARCHAR(255),
    to_location VARCHAR(255),
    transport_mode transport_mode_type NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL,
    co2_saved_kg DECIMAL(10,2) NOT NULL,
    duration_minutes INT,
    train_id INT REFERENCES trains(train_id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- BADGES & ACHIEVEMENTS
-- =====================================================

CREATE TABLE badges (
    badge_id SERIAL PRIMARY KEY,
    badge_name VARCHAR(100) NOT NULL,
    badge_description VARCHAR(500),
    badge_type badge_type NOT NULL,
    icon_url VARCHAR(500),
    requirement_type VARCHAR(50),
    requirement_value DECIMAL(10,2),
    points_reward INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_badges (
    user_badge_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    badge_id INT NOT NULL REFERENCES badges(badge_id),
    is_unlocked BOOLEAN DEFAULT FALSE,
    progress DECIMAL(5,4) DEFAULT 0.0000,
    unlocked_at TIMESTAMPTZ,
    UNIQUE (user_id, badge_id)
);

-- =====================================================
-- DAILY CHALLENGES & GAMIFICATION
-- =====================================================

CREATE TABLE daily_challenges (
    challenge_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    challenge_type challenge_type NOT NULL,
    points_reward INT NOT NULL,
    target_value DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    valid_from DATE,
    valid_until DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE challenge_completions (
    completion_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    challenge_id INT NOT NULL REFERENCES daily_challenges(challenge_id),
    completed_at TIMESTAMPTZ DEFAULT NOW(),
    points_earned INT
);

-- =====================================================
-- CARBON OFFSET PROJECTS
-- =====================================================

CREATE TABLE offset_projects (
    project_id SERIAL PRIMARY KEY,
    project_name VARCHAR(200) NOT NULL,
    project_description TEXT,
    project_type project_type NOT NULL,
    target_amount DECIMAL(12,2) NOT NULL,
    current_amount DECIMAL(12,2) DEFAULT 0.00,
    impact_per_unit VARCHAR(100),
    location VARCHAR(255),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_contributions (
    contribution_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    project_id INT NOT NULL REFERENCES offset_projects(project_id),
    amount DECIMAL(10,2) NOT NULL,
    co2_offset_kg DECIMAL(10,2),
    impact_description VARCHAR(500),
    transaction_id VARCHAR(100),
    contributed_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- COMMUNITY ENERGY WATCH
-- =====================================================

CREATE TABLE energy_reports (
    report_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    report_type report_type NOT NULL,
    location VARCHAR(255) NOT NULL,
    station_id INT REFERENCES stations(station_id),
    description TEXT,
    status report_status_type DEFAULT 'PENDING',
    upvotes INT DEFAULT 0,
    image_url VARCHAR(500),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    resolved_at TIMESTAMPTZ,
    energy_saved_kwh DECIMAL(10,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE report_upvotes (
    upvote_id SERIAL PRIMARY KEY,
    report_id INT NOT NULL REFERENCES energy_reports(report_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (report_id, user_id)
);

-- =====================================================
-- SMART TRAVEL PLANNER
-- =====================================================

CREATE TABLE smart_routes (
    route_id SERIAL PRIMARY KEY,
    from_station_id INT NOT NULL REFERENCES stations(station_id),
    to_station_id INT NOT NULL REFERENCES stations(station_id),
    carbon_footprint_kg DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL,
    is_green_recommended BOOLEAN DEFAULT FALSE,
    renewable_stations JSONB,
    congestion_level congestion_type DEFAULT 'MEDIUM',
    energy_efficiency_score DECIMAL(5,4),
    via_trains JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- CITY ENERGY SUMMARY
-- =====================================================

CREATE TABLE city_energy_summary (
    summary_id SERIAL PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    summary_date DATE NOT NULL,
    total_energy_saved_kwh DECIMAL(12,2) DEFAULT 0.00,
    total_trains_running INT DEFAULT 0,
    renewable_powered_percent DECIMAL(5,4) DEFAULT 0.0000,
    todays_badge VARCHAR(100),
    co2_saved_tonnes DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (city_name, summary_date)
);

-- =====================================================
-- EDUCATION HUB
-- =====================================================

CREATE TABLE education_content (
    content_id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    content_type content_type NOT NULL,
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    read_time_minutes INT,
    is_featured BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE quiz_questions (
    question_id SERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    options JSONB NOT NULL,
    correct_answer_index INT NOT NULL,
    explanation TEXT,
    difficulty difficulty_type DEFAULT 'MEDIUM',
    points INT DEFAULT 10,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE quiz_answers (
    answer_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    question_id INT NOT NULL REFERENCES quiz_questions(question_id),
    selected_answer_index INT NOT NULL,
    is_correct BOOLEAN,
    points_earned INT DEFAULT 0,
    answered_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE facts_of_the_day (
    fact_id SERIAL PRIMARY KEY,
    fact_text TEXT NOT NULL,
    source VARCHAR(255),
    related_stat VARCHAR(255),
    display_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- LEADERBOARD & RANKINGS
-- =====================================================

CREATE TABLE weekly_leaderboard (
    leaderboard_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    week_start_date DATE NOT NULL,
    total_points INT DEFAULT 0,
    rank_position INT,
    co2_saved_kg DECIMAL(10,2) DEFAULT 0.00,
    trips_completed INT DEFAULT 0,
    challenges_completed INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, week_start_date)
);

-- =====================================================
-- QR CODE SCANS
-- =====================================================

CREATE TABLE qr_scans (
    scan_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    scan_type scan_type NOT NULL,
    raw_value TEXT,
    train_id INT REFERENCES trains(train_id),
    station_id INT REFERENCES stations(station_id),
    pnr_number VARCHAR(20),
    scanned_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- TICKETS
-- =====================================================

CREATE TABLE tickets (
    ticket_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    pnr_number VARCHAR(20) NOT NULL UNIQUE,
    train_id INT NOT NULL REFERENCES trains(train_id),
    from_station_id INT NOT NULL REFERENCES stations(station_id),
    to_station_id INT NOT NULL REFERENCES stations(station_id),
    journey_date DATE NOT NULL,
    coach VARCHAR(10),
    seat_number VARCHAR(10),
    passenger_name VARCHAR(100),
    booking_status booking_status_type DEFAULT 'CONFIRMED',
    fare DECIMAL(10,2),
    booked_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- NOTIFICATIONS
-- =====================================================

CREATE TABLE notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    notification_type notification_type NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- USER PREFERENCES
-- =====================================================

CREATE TABLE user_preferences (
    preference_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    weekly_goal_kg DECIMAL(10,2) DEFAULT 50.00,
    preferred_transport_mode preferred_mode_type DEFAULT 'ANY',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    dark_mode_enabled BOOLEAN DEFAULT FALSE,
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_eco_trips_user ON eco_trips(user_id);
CREATE INDEX idx_eco_trips_date ON eco_trips(trip_date);
CREATE INDEX idx_train_status_train ON train_status(train_id);
CREATE INDEX idx_energy_reports_status ON energy_reports(status);
CREATE INDEX idx_challenge_completions_user ON challenge_completions(user_id);
CREATE INDEX idx_user_contributions_user ON user_contributions(user_id);
CREATE INDEX idx_weekly_leaderboard_week ON weekly_leaderboard(week_start_date);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);

-- =====================================================
-- TRIGGER FOR updated_at
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

CREATE OR REPLACE VIEW v_user_stats AS
SELECT 
    u.user_id,
    u.email,
    u.full_name,
    u.total_co2_saved,
    u.total_trips,
    u.streak_days,
    u.total_points,
    COUNT(DISTINCT ub.badge_id) as badges_earned,
    COUNT(DISTINCT cc.challenge_id) as challenges_completed
FROM users u
LEFT JOIN user_badges ub ON u.user_id = ub.user_id AND ub.is_unlocked = TRUE
LEFT JOIN challenge_completions cc ON u.user_id = cc.user_id
GROUP BY u.user_id, u.email, u.full_name, u.total_co2_saved, u.total_trips, u.streak_days, u.total_points;

CREATE OR REPLACE VIEW v_train_live_status AS
SELECT 
    t.train_number,
    t.train_name,
    ts.current_status,
    ts.current_location,
    cs.station_name as current_station,
    ns.station_name as next_station,
    ts.expected_arrival,
    ts.delay_minutes,
    ts.delay_reason,
    ts.current_energy_usage_kwh,
    ts.regenerative_braking_recovery,
    ts.renewable_energy_percent,
    ts.last_updated
FROM trains t
JOIN train_status ts ON t.train_id = ts.train_id
LEFT JOIN stations cs ON ts.current_station_id = cs.station_id
LEFT JOIN stations ns ON ts.next_station_id = ns.station_id;

CREATE OR REPLACE VIEW v_leaderboard AS
SELECT 
    u.user_id,
    u.full_name,
    u.total_points,
    u.total_co2_saved,
    RANK() OVER (ORDER BY u.total_points DESC) as global_rank
FROM users u
WHERE u.is_active = TRUE
ORDER BY u.total_points DESC;

-- =====================================================
-- FUNCTIONS (PostgreSQL style)
-- =====================================================

-- Function to log eco trip and update user stats
CREATE OR REPLACE FUNCTION log_eco_trip(
    p_user_id INT,
    p_from_location VARCHAR(255),
    p_to_location VARCHAR(255),
    p_transport_mode transport_mode_type,
    p_distance_km DECIMAL(10,2)
) RETURNS DECIMAL(10,2) AS $$
DECLARE
    v_co2_saved DECIMAL(10,2);
BEGIN
    -- Calculate CO2 saved (compared to car travel: ~0.21 kg CO2 per km)
    v_co2_saved := p_distance_km * 0.168; -- 80% savings vs car
    
    -- Insert trip
    INSERT INTO eco_trips (user_id, trip_date, from_location, to_location, transport_mode, distance_km, co2_saved_kg)
    VALUES (p_user_id, CURRENT_DATE, p_from_location, p_to_location, p_transport_mode, p_distance_km, v_co2_saved);
    
    -- Update user stats
    UPDATE users 
    SET total_co2_saved = total_co2_saved + v_co2_saved,
        total_trips = total_trips + 1,
        streak_days = streak_days + 1
    WHERE user_id = p_user_id;
    
    RETURN v_co2_saved;
END;
$$ LANGUAGE plpgsql;

-- Function to complete a challenge
CREATE OR REPLACE FUNCTION complete_challenge(
    p_user_id INT,
    p_challenge_id INT
) RETURNS INT AS $$
DECLARE
    v_points INT;
BEGIN
    SELECT points_reward INTO v_points FROM daily_challenges WHERE challenge_id = p_challenge_id;
    
    INSERT INTO challenge_completions (user_id, challenge_id, points_earned)
    VALUES (p_user_id, p_challenge_id, v_points);
    
    UPDATE users SET total_points = total_points + v_points WHERE user_id = p_user_id;
    
    RETURN v_points;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate CO2 saved
CREATE OR REPLACE FUNCTION calculate_co2_saved(
    p_distance_km DECIMAL(10,2),
    p_transport_mode transport_mode_type
) RETURNS DECIMAL(10,2) AS $$
DECLARE
    v_car_emission DECIMAL(10,4) := 0.21;
    v_mode_emission DECIMAL(10,4);
BEGIN
    CASE p_transport_mode
        WHEN 'RAIL' THEN v_mode_emission := 0.041;
        WHEN 'METRO' THEN v_mode_emission := 0.035;
        WHEN 'BUS' THEN v_mode_emission := 0.089;
        WHEN 'WALK' THEN v_mode_emission := 0.000;
        WHEN 'CYCLE' THEN v_mode_emission := 0.000;
        ELSE v_mode_emission := 0.041;
    END CASE;
    
    RETURN ROUND(p_distance_km * (v_car_emission - v_mode_emission), 2);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- SAMPLE DATA INSERTS
-- =====================================================

-- Insert sample badges
INSERT INTO badges (badge_name, badge_description, badge_type, requirement_type, requirement_value, points_reward) VALUES
('Green Starter', 'Complete 10 eco trips', 'BRONZE', 'TRIPS', 10, 100),
('Carbon Crusher', 'Save 100kg CO2', 'SILVER', 'CO2_SAVED', 100, 250),
('Eco Warrior', 'Save 500kg CO2', 'GOLD', 'CO2_SAVED', 500, 500),
('Planet Protector', 'Save 1000kg CO2', 'PLATINUM', 'CO2_SAVED', 1000, 1000),
('Challenge Champion', 'Complete 50 challenges', 'GOLD', 'CHALLENGES', 50, 400),
('Community Hero', 'Submit 20 energy reports', 'SILVER', 'REPORTS', 20, 300),
('Streak Master', 'Maintain 30 day streak', 'GOLD', 'STREAK', 30, 500);

-- Insert sample stations
INSERT INTO stations (station_code, station_name, city, state, platforms, is_green_station, solar_capacity_kw, amenities) VALUES
('MMCT', 'Mumbai Central', 'Mumbai', 'Maharashtra', 6, TRUE, 250.00, '["WiFi", "Food Court", "Waiting Room", "ATM", "EV Charging"]'),
('NDLS', 'New Delhi', 'New Delhi', 'Delhi', 16, TRUE, 500.00, '["WiFi", "Food Court", "Lounge", "ATM", "EV Charging", "Metro Connect"]'),
('BRC', 'Vadodara Junction', 'Vadodara', 'Gujarat', 6, TRUE, 180.00, '["WiFi", "Food Court", "Waiting Room"]'),
('ST', 'Surat Junction', 'Surat', 'Gujarat', 4, FALSE, 0.00, '["Food Court", "Waiting Room", "ATM"]'),
('HWH', 'Howrah Junction', 'Kolkata', 'West Bengal', 23, TRUE, 350.00, '["WiFi", "Food Court", "Lounge", "ATM"]'),
('MAS', 'Chennai Central', 'Chennai', 'Tamil Nadu', 12, TRUE, 280.00, '["WiFi", "Food Court", "Waiting Room", "ATM"]'),
('SBC', 'Bangalore City', 'Bangalore', 'Karnataka', 10, TRUE, 320.00, '["WiFi", "Food Court", "Lounge", "Metro Connect"]');

-- Insert sample trains
INSERT INTO trains (train_number, train_name, train_type, source_station_id, destination_station_id, total_distance_km, is_electric, energy_efficiency_rating) VALUES
('12045', 'Mumbai Rajdhani Express', 'RAJDHANI', 1, 2, 1384, TRUE, 0.92),
('12951', 'Mumbai Rajdhani', 'RAJDHANI', 1, 2, 1384, TRUE, 0.90),
('12301', 'Howrah Rajdhani', 'RAJDHANI', 5, 2, 1451, TRUE, 0.88),
('12627', 'Karnataka Express', 'SUPERFAST', 2, 7, 2444, TRUE, 0.85),
('12839', 'Chennai Mail', 'EXPRESS', 5, 6, 1663, TRUE, 0.82);

-- Insert sample daily challenges
INSERT INTO daily_challenges (title, description, challenge_type, points_reward, target_value, is_active) VALUES
('Rail Rider', 'Take the train instead of cab today', 'TRAVEL', 50, 1, TRUE),
('Walk the Mile', 'Walk 1 km instead of using auto', 'WALK', 30, 1, TRUE),
('Off-Peak Hero', 'Travel during off-peak hours', 'OFF_PEAK', 40, 1, TRUE),
('Share & Care', 'Share your eco stats with friends', 'SHARE', 20, 1, TRUE),
('Energy Watcher', 'Report an energy waste issue', 'REPORT', 35, 1, TRUE),
('Green Commuter', 'Use public transport for all trips today', 'TRAVEL', 60, 3, TRUE),
('Metro Master', 'Take 2 metro rides today', 'TRAVEL', 45, 2, TRUE);

-- Insert sample offset projects
INSERT INTO offset_projects (project_name, project_description, project_type, target_amount, current_amount, impact_per_unit, location) VALUES
('Solar Village Initiative', 'Powering rural homes with solar energy', 'SOLAR', 10000.00, 6500.00, '1 unit = 10kg CO2 offset', 'Rajasthan'),
('Green Forest Project', 'Planting trees in urban areas', 'REFORESTATION', 5000.00, 3200.00, '1 unit = 5kg CO2 offset', 'Maharashtra'),
('Community EV Charging', 'Installing EV chargers in communities', 'EV_CHARGING', 8000.00, 4100.00, '1 unit = 8kg CO2 offset', 'Karnataka'),
('Wind Power Expansion', 'Supporting wind energy projects', 'WIND', 15000.00, 8500.00, '1 unit = 12kg CO2 offset', 'Tamil Nadu');

-- Insert sample education content
INSERT INTO education_content (title, content, content_type, read_time_minutes, is_featured) VALUES
('How Regenerative Braking Works', 'When a train brakes, the electric motors run in reverse, acting as generators. This converts kinetic energy back into electrical energy, which is fed back into the power grid or stored in batteries.', 'ARTICLE', 5, TRUE),
('India''s Green Railway Journey', 'Indian Railways has committed to becoming net-zero by 2030. With over 90% electrification complete and solar panels on 960+ stations, it''s leading the sustainable transport revolution.', 'INFOGRAPHIC', 3, TRUE),
('Solar-Powered Stations', 'Over 960 stations now run on solar power, reducing grid dependency and carbon emissions significantly. These green stations save millions of units of electricity annually.', 'ARTICLE', 4, FALSE),
('The Future of Sustainable Rail', 'From hydrogen-powered trains to AI-optimized energy management, discover the innovations shaping the future of eco-friendly rail travel.', 'VIDEO', 8, TRUE);

-- Insert sample quiz questions
INSERT INTO quiz_questions (question_text, options, correct_answer_index, explanation, difficulty, points) VALUES
('What percentage of Indian Railways is electrified?', '["50%", "70%", "90%", "95%"]', 2, 'As of 2024, over 90% of Indian Railways is electrified!', 'MEDIUM', 10),
('How much CO2 does rail save vs road per km?', '["50%", "70%", "80%", "90%"]', 2, 'Rail transport produces about 80% less CO2 than road transport.', 'MEDIUM', 10),
('What is regenerative braking?', '["Using solar panels", "Converting kinetic energy to electricity", "Using wind power", "Manual braking"]', 1, 'Regenerative braking converts the train''s kinetic energy back into electrical energy during braking.', 'EASY', 5),
('By what year has Indian Railways committed to becoming net-zero?', '["2025", "2030", "2040", "2050"]', 1, 'Indian Railways aims to achieve net-zero carbon emissions by 2030.', 'HARD', 15);

-- Insert sample facts of the day
INSERT INTO facts_of_the_day (fact_text, source, related_stat, display_date) VALUES
('Indian Railways saved 12,000 tonnes of CO2 today through regenerative braking!', 'Railway Energy Dashboard', 'That''s equivalent to planting 550,000 trees!', CURRENT_DATE),
('A single train journey saves 80% more CO2 compared to the same journey by car.', 'Environmental Research Institute', 'Choose rail, save the planet!', CURRENT_DATE + INTERVAL '1 day'),
('Solar panels on railway stations generate enough power to light up 50,000 homes daily.', 'Indian Railways Green Initiative', 'Clean energy powering your journey!', CURRENT_DATE + INTERVAL '2 days');

-- Insert sample city energy summary
INSERT INTO city_energy_summary (city_name, summary_date, total_energy_saved_kwh, total_trains_running, renewable_powered_percent, todays_badge, co2_saved_tonnes) VALUES
('Mumbai', CURRENT_DATE, 45000.00, 156, 0.72, '🌟 Super Green Day!', 12.5),
('Delhi', CURRENT_DATE, 62000.00, 210, 0.78, '🌿 Eco Champion City!', 18.2),
('Kolkata', CURRENT_DATE, 38000.00, 125, 0.65, '💚 Green Progress!', 10.8),
('Chennai', CURRENT_DATE, 35000.00, 98, 0.70, '☀️ Solar Powered!', 9.5),
('Bangalore', CURRENT_DATE, 42000.00, 145, 0.75, '🚂 Rail Revolution!', 11.2);

-- Insert sample users
INSERT INTO users (email, phone_number, password_hash, full_name, total_co2_saved, total_trips, streak_days, total_points, is_verified) VALUES
('rahul.sharma@email.com', '+919876543210', '$2b$12$hash1', 'Rahul Sharma', 245.50, 42, 15, 2850, TRUE),
('priya.patel@email.com', '+919876543211', '$2b$12$hash2', 'Priya Patel', 189.25, 35, 8, 2100, TRUE),
('amit.kumar@email.com', '+919876543212', '$2b$12$hash3', 'Amit Kumar', 312.75, 58, 22, 3650, TRUE),
('sneha.reddy@email.com', '+919876543213', '$2b$12$hash4', 'Sneha Reddy', 156.00, 28, 5, 1750, TRUE),
('vikram.singh@email.com', '+919876543214', '$2b$12$hash5', 'Vikram Singh', 420.30, 72, 30, 4500, TRUE);

-- Insert sample train status
INSERT INTO train_status (train_id, current_status, current_location, current_station_id, next_station_id, expected_arrival, delay_minutes, delay_reason, current_energy_usage_kwh, regenerative_braking_recovery, renewable_energy_percent) VALUES
(1, 'ON_TIME', 'Between Vadodara and Ratlam', 3, NULL, '14:30:00', 0, NULL, 2450.50, 0.1850, 0.7200),
(2, 'DELAYED', 'Approaching Kota Junction', NULL, NULL, '16:45:00', 25, 'SIGNAL_FAILURE', 2680.25, 0.1650, 0.6800),
(3, 'ON_TIME', 'Departed Allahabad', NULL, NULL, '18:20:00', 0, NULL, 3120.75, 0.1920, 0.7500),
(4, 'STOPPED', 'Secunderabad Junction', NULL, NULL, '20:00:00', 45, 'CREW_CHANGE', 4250.00, 0.1780, 0.7100),
(5, 'ON_TIME', 'Near Vijayawada', NULL, NULL, '22:15:00', 0, NULL, 2890.50, 0.1880, 0.6900);

-- =====================================================
-- ROW LEVEL SECURITY (RLS) FOR SUPABASE
-- =====================================================

-- Enable RLS on tables that need user-specific access
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE eco_trips ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_badges ENABLE ROW LEVEL SECURITY;
ALTER TABLE challenge_completions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_contributions ENABLE ROW LEVEL SECURITY;
ALTER TABLE energy_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE qr_scans ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_sessions ENABLE ROW LEVEL SECURITY;

-- Policies for users table
CREATE POLICY "Users can view own profile" ON users
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid()::text = user_id::text);

-- Policies for eco_trips
CREATE POLICY "Users can view own trips" ON eco_trips
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can insert own trips" ON eco_trips
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

-- Policies for notifications
CREATE POLICY "Users can view own notifications" ON notifications
    FOR SELECT USING (auth.uid()::text = user_id::text);

CREATE POLICY "Users can update own notifications" ON notifications
    FOR UPDATE USING (auth.uid()::text = user_id::text);

-- =====================================================
-- END OF SUPABASE SCHEMA
-- =====================================================
