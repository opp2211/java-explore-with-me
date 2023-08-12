package ru.practicum.service.user;

import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {
    List<UserDto> getByIdsPageable(Collection<Long> ids, int from, int size);
    UserDto addNew(NewUserDto newUserDto);
    void delete(long userId);
}
