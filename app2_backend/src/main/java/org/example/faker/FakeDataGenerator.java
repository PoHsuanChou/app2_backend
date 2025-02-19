//package org.example.faker;
//
//import com.github.javafaker.Faker;
//import org.example.entity.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
//@Component
//public class FakeDataGenerator {
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    private final Faker faker = new Faker();
//
//    public void generateFakeData() {
//        // Create fake users
//        List<User> users = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            User user = createFakeUser();
//            users.add(user);
//        }
//        mongoTemplate.insertAll(users);
//
//        // Create fake matches
//        createMatches(users);
//
//        // Create fake chat rooms
//        createChatRooms(users);
//    }
//
//    private User createFakeUser() {
//        // Create a fake profile
//        Profile profile = Profile.builder()
//                .name(faker.name().fullName())
//                .birthDate(faker.date().birthday())
//                .gender(faker.demographic().sex())
////                .photos(Arrays.asList(faker.internet().image(), faker.internet().image()))
//                .bio(faker.lorem().sentence())
//                .interests(Arrays.asList("Reading", "Gaming", "Traveling"))
//                .build();
//
//        // Save Profile first to generate an ID
//        mongoTemplate.save(profile);
//
//        // Create a fake location
//        Location location = new Location();
//        location.setCoordinates(Arrays.asList(1111.111, 1111.111));
//
//        // Save Location first to generate an ID
//        mongoTemplate.save(location);
//
//        // Create roles
//        Role role = new Role("ROLE_USER");
//
//        // Create user and reference the saved profile and location
//        User user = User.builder()
//                .username(faker.name().username())
//                .email(faker.internet().emailAddress())
//                .password(faker.internet().password())
//                .profile(profile)  // Use the saved profile
//                .location(location)  // Use the saved location
//                .lastActive(new Date())
//                .createdAt(new Date())
//                .isOnline(faker.bool().bool())
//                .build();
//
//        return user;
//    }
//
//    private void createMatches(List<User> users) {
//        Random random = new Random();
//        List<Match> matches = new ArrayList<>();
//
//        for (int i = 0; i < users.size() - 1; i++) {
//            User user1 = users.get(i);
//            User user2 = users.get(i + 1);
//
//            // Simulate a match score (between 0 and 1)
//            double matchScore = random.nextDouble();
//
//            Match match = new Match();
//            match.setUser1(user1);
//            match.setUser2(user2);
//            match.setMatchScore(matchScore);
//
//            // Assign match status (e.g., accepted or pending)
//            Match.MatchStatus matchStatus = new Match.MatchStatus();
//            matchStatus.setUser1Status(random.nextBoolean() ? "accepted" : "pending");
//            matchStatus.setUser2Status(random.nextBoolean() ? "accepted" : "pending");
//
//            match.setStatus(matchStatus);
//            match.setMatchDate(new Date());
//            match.setLastInteractionDate(new Date());
//
//            matches.add(match);
//
//            // Log who matched, along with their email and password
//            System.out.println("Match found between " + user1.getUsername() + " and " + user2.getUsername());
//            System.out.println("User 1 Email: " + user1.getEmail() + ", Password: " + user1.getPassword());
//            System.out.println("User 2 Email: " + user2.getEmail() + ", Password: " + user2.getPassword());
//        }
//
//        mongoTemplate.insertAll(matches);
//
//        // Log which users matched
//        // Optional: Log the complete match details
//        for (Match match : matches) {
//            System.out.println("Match details: ");
//            System.out.println("User 1: " + match.getUser1().getUsername() + ", User 2: " + match.getUser2().getUsername());
//            System.out.println("Match Score: " + match.getMatchScore());
//        }
//    }
//
//    private void createChatRooms(List<User> users) {
//        Random random = new Random();
//        List<ChatRoom> chatRooms = new ArrayList<>();
//
//        for (int i = 0; i < users.size() - 1; i++) {
//            User user1 = users.get(i);
//            User user2 = users.get(i + 1);
//
//            // Create chat room for the match
//            ChatRoom chatRoom = new ChatRoom();
//            chatRoom.setParticipants(Arrays.asList(user1, user2));
//            chatRoom.setStatus("active");
//            chatRoom.setCreatedAt(new Date());
//            chatRoom.setLastMessageAt(new Date());
//
//            chatRooms.add(chatRoom);
//        }
//
//        mongoTemplate.insertAll(chatRooms);
//
//        // Log who can chat with whom
//        for (ChatRoom chatRoom : chatRooms) {
//            List<User> participants = chatRoom.getParticipants();
//            System.out.println("Chat room created for: " + participants.get(0).getUsername() + " and " + participants.get(1).getUsername());
//        }
//    }
//}