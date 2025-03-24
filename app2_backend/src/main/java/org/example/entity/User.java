package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(def = "{'email': 1}", unique = true)
public class User implements UserDetails {
    @Id
    private String id;
    private String username;

    @Indexed(unique = true)
    private String email;
    private String password;
    private String googleId;
    private boolean isGoogleUser;
    private String picture;

    private Profile profile; // 嵌入式 Profile

    private String locationId; // 只存儲 ID
    @Indexed
    private Date lastActive;
    private Date createdAt;
    private boolean isOnline;
    private List<String> roles;  // Roles list
    private List<String> seenUserIds;  // 存放已經看過的使用者 ID
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用戶權限列表
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    //TODO
    public String getUsername() {
        // 使用 email 作為 username
        return email;
    }

    public String getNickName(){
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}