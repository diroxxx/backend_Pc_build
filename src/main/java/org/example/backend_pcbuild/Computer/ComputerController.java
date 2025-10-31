package org.example.backend_pcbuild.Computer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/computerApi")
@RequiredArgsConstructor
@Slf4j
public class ComputerController {

    private final ComputerService computerService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
@GetMapping("/user/{email}/computers")
public ResponseEntity<List<ComputerDto>> getAllComputersByUserEmail(@PathVariable String email) {
    List<ComputerDto> allComputersByUserEmail = computerService.getAllComputersByUserEmail(email);
        System.out.println(allComputersByUserEmail.size());
        for (ComputerDto computer : allComputersByUserEmail) {
            System.out.println(computer.getName());
            System.out.println(computer.getOffers().size());
            System.out.println("-".repeat(100));
        }
    return ResponseEntity.ok(allComputersByUserEmail);
}

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/user/{email}/computers")
    public ResponseEntity<?> saveComputersByUserEmail(@PathVariable String email, @RequestBody List<ComputerDto> computers) {

        System.out.println(computers.size());
        for (ComputerDto computer : computers) {
            System.out.println(computer.getName());
//            System.out.println(computer.getComponents().size());
            System.out.println("-".repeat(100));
        }
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be null or empty.");
        }
        try {
            computerService.saveComputersByUserEmail(email, computers);
            return ResponseEntity.ok("Computers have been successfully saved or updated for user with email: " + email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (StackOverflowError e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("StackOverflow error - check entity relationships");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }



}
