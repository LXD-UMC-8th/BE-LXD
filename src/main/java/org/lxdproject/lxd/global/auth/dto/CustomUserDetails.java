package org.lxdproject.lxd.global.auth.dto;

import org.lxdproject.lxd.domain.member.entity.Member;
import org.lxdproject.lxd.domain.member.entity.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public String getMemberEmail(){
        return member.getEmail();
    }

    public Role getRole() {
        return member.getRole();
    }

    public Member getMember() {
        return member;
    }

    public boolean isDeleted() {
        return member.isDeleted();
    }

    public LocalDateTime getDeletedAt() {
        return member.getDeletedAt();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
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
