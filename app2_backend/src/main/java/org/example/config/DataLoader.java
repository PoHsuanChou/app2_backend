package org.example.config;

import org.example.entity.Location;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.repository.LocationRepository;
import org.example.repository.ProfileRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final LocationRepository locationRepository;

    public DataLoader(UserRepository userRepository, ProfileRepository profileRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 創建 Profile
        Profile profile1 = new Profile();
        profile1.setName("User1");
        profile1.setBirthDate(new Date());
        profile1.setGender("Male");
        profile1.setPhotos(Arrays.asList("photo1.jpg", "photo2.jpg"));
        profile1.setBio("Bio of user 1");
        profile1.setInterests(Arrays.asList("Reading", "Music"));

        Profile profile2 = new Profile();
        profile2.setName("User2");
        profile2.setBirthDate(new Date());
        profile2.setGender("Female");
        profile2.setPhotos(Arrays.asList("photo3.jpg", "photo4.jpg"));
        profile2.setBio("Bio of user 2");
        profile2.setInterests(Arrays.asList("Travel", "Cooking"));

        Profile profile3 = new Profile();
        profile3.setName("User3");
        profile3.setBirthDate(new Date());
        profile3.setGender("Male");
        profile3.setPhotos(Arrays.asList("photo5.jpg"));
        profile3.setBio("Bio of user 3");
        profile3.setInterests(Arrays.asList("Gaming", "Sports"));

        Profile profile4 = new Profile();
        profile4.setName("User4");
        profile4.setBirthDate(new Date());
        profile4.setGender("Female");
        profile4.setPhotos(Arrays.asList("photo6.jpg"));
        profile4.setBio("Bio of user 4");
        profile4.setInterests(Arrays.asList("Photography", "Art"));

        Profile profile5 = new Profile();
        profile5.setName("User5");
        profile5.setBirthDate(new Date());
        profile5.setGender("Male");
        profile5.setPhotos(Arrays.asList("photo7.jpg", "photo8.jpg"));
        profile5.setBio("Bio of user 5");
        profile5.setInterests(Arrays.asList("Movies", "Music"));

        profileRepository.saveAll(Arrays.asList(profile1, profile2, profile3, profile4, profile5));

        // 創建 Location
        Location location1 = new Location();
        location1.setCoordinates(Arrays.asList(121.5654, 25.0330));  // Taipei (longitude, latitude)

        Location location2 = new Location();
        location2.setCoordinates(Arrays.asList(120.9820, 23.9739));  // Kaohsiung (longitude, latitude)

        Location location3 = new Location();
        location3.setCoordinates(Arrays.asList(-74.0060, 40.7128));  // New York (longitude, latitude)

        Location location4 = new Location();
        location4.setCoordinates(Arrays.asList(-0.1276, 51.5074));  // London (longitude, latitude)

        Location location5 = new Location();
        location5.setCoordinates(Arrays.asList(139.6917, 35.6895));  // Tokyo (longitude, latitude)

        locationRepository.saveAll(Arrays.asList(location1, location2, location3, location4, location5));

        // 創建 User 並保存
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setProfile(profile1);
        user1.setLocation(location1);
        user1.setLastActive(new Date());
        user1.setCreatedAt(new Date());
        user1.setOnline(true);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setProfile(profile2);
        user2.setLocation(location2);
        user2.setLastActive(new Date());
        user2.setCreatedAt(new Date());
        user2.setOnline(true);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        user3.setPassword("password3");
        user3.setProfile(profile3);
        user3.setLocation(location3);
        user3.setLastActive(new Date());
        user3.setCreatedAt(new Date());
        user3.setOnline(false);

        User user4 = new User();
        user4.setUsername("user4");
        user4.setEmail("user4@example.com");
        user4.setPassword("password4");
        user4.setProfile(profile4);
        user4.setLocation(location4);
        user4.setLastActive(new Date());
        user4.setCreatedAt(new Date());
        user4.setOnline(false);

        User user5 = new User();
        user5.setUsername("user5");
        user5.setEmail("user5@example.com");
        user5.setPassword("password5");
        user5.setProfile(profile5);
        user5.setLocation(location5);
        user5.setLastActive(new Date());
        user5.setCreatedAt(new Date());
        user5.setOnline(true);

        // 保存使用者
        userRepository.saveAll(Arrays.asList(user1, user2, user3, user4, user5));
    }
}