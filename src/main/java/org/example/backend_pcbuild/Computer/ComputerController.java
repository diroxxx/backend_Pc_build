package org.example.backend_pcbuild.Computer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
@Slf4j
public class ComputerController {

    private final ComputerService computerService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/users/{email}/computers")
    public ResponseEntity<List<ComputerDto>> getAllComputersByUserEmail(@PathVariable String email) {
        List<ComputerDto> allComputersByUserEmail = computerService.getAllComputersByUserEmail(email);

//            System.out.println(allComputersByUserEmail.size());
        for (ComputerDto computer : allComputersByUserEmail) {
            System.out.println(computer.getName());
            System.out.println(computer.getOffers().size());
            System.out.println("-".repeat(100));
        }
        return ResponseEntity.ok(allComputersByUserEmail);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/users/{email}/computers")
    public ResponseEntity<?> saveComputersByUserEmail(@PathVariable String email, @RequestBody ComputerDto computer) {

//        System.out.println(computer.toString());
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be null or empty.");
        }
        try {
            computerService.saveComputerByUserEmail(email, computer);
            return ResponseEntity.ok("Computer has been successfully saved" + email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StackOverflowError e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("StackOverflow error - check entity relationships");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/computers/{computerId}/offers")
    public ResponseEntity<?> updateComputerByUserEmail(@PathVariable Long computerId, @RequestBody String offerUrl) {

        computerService.updateComputerFromDto(computerId, offerUrl);
        return ResponseEntity.ok("Computer has been successfully updated");
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("computers/{computerId}")
    public ResponseEntity<?> deleteComputerByUserEmail(@PathVariable Long computerId) {
        computerService.deleteComputer(computerId);
        return ResponseEntity.ok("Computer has been successfully deleted");
    }


    @PutMapping("/computers/{computerId}/name")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updateComputerName(@PathVariable Long computerId, @RequestBody Map<String, String> body) {
        String newName = body.get("name").trim();

        computerService.updateComputerName(newName, computerId);
        return ResponseEntity.ok("Computer has been successfully updated");
    }

    @PostMapping("/users/{email}/computers/migrate")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updateComputers(@PathVariable String email, @RequestBody List<ComputerDto> computers) {

        if (email.isBlank() || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email cannot be null or empty.");
        }
        if (computers.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        computerService.updateUserComputerFromGuest(email, computers);
        return ResponseEntity.ok("Computers have been successfully updated");
    }
}