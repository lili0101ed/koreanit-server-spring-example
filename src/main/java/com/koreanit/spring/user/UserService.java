package com.koreanit.spring.user;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.koreanit.spring.common.error.ApiException;
import com.koreanit.spring.common.error.ErrorCode;
import com.koreanit.spring.security.SecurityUtils;

@Service
public class UserService {

    private static final int MAX_LIMIT = 1000;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "limit 은 1 이상 입력해주세요");
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public Long create(String username, String password, String nickname, String email) {
        username = username.trim().toLowerCase();
        nickname = nickname.trim().toLowerCase();

        String normalizedEmail = (email == null) ? null : email.toLowerCase();

        String hash = passwordEncoder.encode(password);

        return userRepository.save(username, hash, nickname, normalizedEmail);
    }

    // 사용자 단건 조회
    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
    public User get(Long id) {
        try {
            UserEntity e = userRepository.findById(id);
            return UserMapper.toDomain(e);

        } catch (EmptyResultDataAccessException e) {
            throw new ApiException(
                    ErrorCode.NOT_FOUND_RESOURCE,
                    "존재하지 않는 사용자입니다. id=" + id);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> list(int limit) {
        int safeLimit = normalizeLimit(limit);
        return UserMapper.toDomainList(userRepository.findAll(safeLimit));
    }

    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
    public void changeNickname(Long id, String nickname) {
        nickname = nickname.trim().toLowerCase();

        // 1) 대상 존재 여부 확인 (없으면 여기서 404)
        User user = get(id);

        // 2) 값이 동일하면 변경 없음 → 정상 처리
        if (user.getNickname().equals(nickname)) {
            return;
        }

        // 3) 실제 변경
        int updated = userRepository.updateNickname(id, nickname);

        if (updated == 0) {
            throw new ApiException(
                    ErrorCode.NOT_FOUND_RESOURCE,
                    "닉네임 변경에 실패하였습니다. id=" + id);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
    public void changePassword(Long id, String password) {
        User user = get(id);
        boolean ok = passwordEncoder.matches(password, user.getPassword());

        if (ok) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "기존 비밀번호와 동일한 값은 사용할 수 없습니다.");
        }

        String passwordHash = passwordEncoder.encode(password);
        int updated = userRepository.updatePassword(id, passwordHash);

        if (updated == 0) {
            throw new ApiException(
                    ErrorCode.NOT_FOUND_RESOURCE,
                    "존재하지 않는 사용자입니다. id=" + id);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
    public void delete(Long id) {
        int deleted = userRepository.deleteById(id);

        if (deleted == 0) {
            throw new ApiException(
                    ErrorCode.NOT_FOUND_RESOURCE,
                    "존재하지 않는 사용자입니다. id=" + id);
        }
    }

    public Long login(String username, String password) {
        try {
            UserEntity en = userRepository.findByUsername(username);
            boolean ok = passwordEncoder.matches(password, en.getPassword());
            if (!ok) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "비밀번호가 잘못되었습니다.");
            }
            return en.getId();
        } catch (EmptyResultDataAccessException e) {
            throw new ApiException(ErrorCode.NOT_FOUND_RESOURCE, "존재하지 않는 사용자입니다. username=" + username);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
    public void changeEmail(Long id, String email) {
        try {
            userRepository.updateEmail(id, email);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "이메일 값이 중복되었습니다.");
        }
    }

    public boolean isSelf(Long userId) {
        Long currentUserId = SecurityUtils.currentUserId();
        return currentUserId != null && userId != null && currentUserId.equals(userId);
    }
}