package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;


import javax.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getByIdsPageable(Collection<Long> ids, int from, int size) {
        if (from % size != 0) {
            throw new InvalidParameterException(String.format(
                    "Parameter 'from'(%d) must be a multiple of parameter 'size'(%d)", from, size));
        }
        Pageable pageable = PageRequest.of(from/size, size);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).toList();
        } else {
            users = userRepository.findByIdIn(ids, pageable);
        }
        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto addNew(NewUserDto newUserDto) {
        User newUser = userRepository.save(userMapper.toUser(newUserDto));
        //todo проверить заполнился ли айдишник пользователя без явного присвоения
        return userMapper.toUserDto(newUser);
    }

    @Override
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("User ID = %d not found!", id)));
    }
}
