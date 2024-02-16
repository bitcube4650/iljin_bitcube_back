package iljin.framework.core.security.user;

import iljin.framework.core.util.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Error> userNotFound(UserException e) {
        Error error = new Error(2001, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user")
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/user/id/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userService.getUser(Long.parseLong(id));
    }

    @GetMapping("/user/login-id/{loginId}")
    public List<User> getSearchUser(@PathVariable String loginId) {
        return userService.getSearchUser(loginId);
    }

    @PostMapping("/user")
    public ResponseEntity<Object> addUser(@RequestBody UserDto dto) {
        return userService.addUser(dto);
    }

    @DeleteMapping("/user/{loginId}")
    public ResponseEntity<String> deleteUser(@PathVariable("loginId") String loginId) {
        return userService.deleteUser(loginId);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody UserDto dto) {
        return userService.updateUser(id, dto);
    }
}
