package org.example.backend_pcbuild.Computer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Computer.dto.BaseComponentDto;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.ComputerRepository;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComputerService {

    private final ComputerRepository computerRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    public List<ComputerDto> getAllComputersByUserEmail(String email) {

        if (email == null || email.isBlank()) {
            return List.of();
        }
        return computerRepository.findAllByUserEmail(email)
                .stream()
                .map(ComputerDto::mapFromEntity)
                .toList();
    }

    @Transactional
    public void saveComputersByUserEmail(String email, List<ComputerDto> computers) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        List<Computer> computerList = computerRepository.findAllByUserEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            List<Computer> computersToSave = new ArrayList<>();

            HashSet<String> incomingComputerNames = new HashSet<>();

            for (ComputerDto computerDto : computers) {
                incomingComputerNames.add(computerDto.getName());
                computerRepository.findByName(computerDto.getName()).ifPresentOrElse(computer -> {

                    prepareComputerForUpdate(computer, computerDto);
                    computersToSave.add(computer);
                }, () -> {
                    Computer newComputer = createNewComputer(computerDto, user);
                    computersToSave.add(newComputer);
                });
            }
            for (Computer existingComputer : computerList) {
                if (!incomingComputerNames.contains(existingComputer.getName())) {
                    user.getComputers().remove(existingComputer);
                    computerRepository.delete(existingComputer);
                }
            }

            computerRepository.saveAll(computersToSave);
        } else {
            throw new IllegalArgumentException("User with email " + email + " not found.");
        }
    }

    private void prepareComputerForUpdate(Computer computer, ComputerDto computerDto) {
        if (computer.getComputer_offer() == null) {
            computer.setComputer_offer(new ArrayList<>());
        } else {
            computer.getComputer_offer().clear();
        }

        for (BaseComponentDto componentDto : computerDto.getComponents()) {
            offerRepository.findByWebsiteUrl(componentDto.getWebsiteUrl()).ifPresent(offer -> {
                ComputerOffer computer_offer = new ComputerOffer();
                computer_offer.setOffer(offer);
                computer_offer.setComputer(computer);
                computer.getComputer_offer().add(computer_offer);
            });
        }

        computer.setName(computerDto.getName());
        computer.setPrice(computerDto.getPrice());
        computer.setIs_visible(computerDto.getIsVisible());
    }

    private Computer createNewComputer(ComputerDto computerDto, User user) {
        Computer computer = new Computer();

        computer.setName(computerDto.getName());
        computer.setPrice(computerDto.getPrice());
        computer.setIs_visible(computerDto.getIsVisible());
        computer.setUser(user);

        for (BaseComponentDto componentDto : computerDto.getComponents()) {
            offerRepository.findByWebsiteUrl(componentDto.getWebsiteUrl()).ifPresentOrElse(offer -> {
                System.out.println("znaleziony komp: " + componentDto.getModel());
                ComputerOffer computer_offer = new ComputerOffer();
                computer_offer.setOffer(offer);
                offer.getComputer_offer().add(computer_offer);
                computer_offer.setComputer(computer);
                computer.getComputer_offer().add(computer_offer);
                System.out.println("Added ComputerOffer. Total now: " + computer.getComputer_offer().size());

            }, () -> {
                System.out.println(" nie znaleziony komp: " + componentDto.getModel());
            });
        }
        return computer;
    }
}
