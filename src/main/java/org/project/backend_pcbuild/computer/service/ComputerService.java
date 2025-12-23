package org.project.backend_pcbuild.computer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.computer.dto.ComputerComponentsStatsDto;
import org.project.backend_pcbuild.computer.dto.ComputerDto;
import org.project.backend_pcbuild.computer.model.Computer;
import org.project.backend_pcbuild.computer.model.ComputerOffer;
import org.project.backend_pcbuild.offer.dto.BaseOfferDto;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.computer.repository.ComputerOfferRepository;
import org.project.backend_pcbuild.computer.repository.ComputerRepository;
import org.project.backend_pcbuild.offer.repository.OfferRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
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
    private final ComputerOfferRepository computerOfferRepository;
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
    public void saveComputerByUserEmail(String email, ComputerDto computer) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        List<Computer> computerList = computerRepository.findAllByUserEmail(email);
//        System.out.println(computer.toString());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            List<Computer> computersToSave = new ArrayList<>();

            computerList.stream().filter(c -> c.getName().equals(computer.getName())).findFirst().ifPresentOrElse(computer1 -> {
                }, () -> {
                    Computer newComputer = createNewComputer(computer, user);
                    computerRepository.save(newComputer);
                        }
                        );

            computerRepository.saveAll(computersToSave);
        } else {
            throw new IllegalArgumentException("User with email " + email + " not found.");
        }
    }

    @Transactional
    public void updateComputerFromDto(Long computerId, String offerUrl) {
        offerUrl = offerUrl.trim().replace("\"", "");

        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new IllegalArgumentException("Computer with ID " + computerId + " not found."));

        String finalOfferUrl = offerUrl;
        Offer offer = offerRepository.findByWebsiteUrl(offerUrl)
                .orElseThrow(() -> new IllegalArgumentException("Offer with URL " + finalOfferUrl + " not found."));

        ComponentType newType = offer.getComponent().getComponentType();

        ComputerOffer existingOffer = computer.getComputer_offer().stream()
                .filter(co -> {
                    ComponentType existingType = co.getOffer().getComponent().getComponentType();
                    System.out.printf("Comparing existing=%s vs new=%s%n", existingType, newType);

                    return existingType != null && existingType.equals(newType);
                })
                .findFirst()
                .orElse(null);

        if (existingOffer != null) {
            System.out.println("🗑 Removing old " + newType + " from " + computer.getName());
            computer.getComputer_offer().remove(existingOffer);
            existingOffer.setComputer(null);
        }

        ComputerOffer newComputerOffer = new ComputerOffer();
        newComputerOffer.setComputer(computer);
        newComputerOffer.setOffer(offer);
        computer.getComputer_offer().add(newComputerOffer);

        double totalPrice = computer.getComputer_offer().stream()
                .mapToDouble(co -> co.getOffer().getPrice())
                .sum();
        computer.setPrice(totalPrice);

        computerRepository.save(computer);

        System.out.printf("Added %s (%s). New total price: %.2f%n",
                newType, offer.getComponent().getModel(), totalPrice);
    }

    public void updateComputerName(String name, Long computerId) {
        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new IllegalArgumentException("Computer with ID " + computerId + " not found."));
        computer.setName(name);
        computerRepository.save(computer);
    }

    private Computer createNewComputer(ComputerDto computerDto, User user) {
        Computer computer = new Computer();

        computer.setName(computerDto.getName());
        computer.setPrice(computerDto.getPrice());
        computer.setIs_visible(computerDto.getIsVisible());
        computer.setUser(user);


        if (computerDto.getOffers() != null) {
            for (BaseOfferDto componentDto : computerDto.getOffers()) {
                offerRepository.findByWebsiteUrl(componentDto.getWebsiteUrl()).ifPresentOrElse(offer -> {
                    System.out.println("znaleziony komp: " + componentDto.getModel());
                    ComputerOffer computer_offer = new ComputerOffer();
                    computer_offer.setOffer(offer);
                    offer.getComputerOffers().add(computer_offer);
                    computer_offer.setComputer(computer);
                    computer.getComputer_offer().add(computer_offer);
                    System.out.println("Added ComputerOffer. Total now: " + computer.getComputer_offer().size());

                }, () -> {
                    System.out.println(" nie znaleziony komp: " + componentDto.getModel());
                });
            }
        }

        return computer;
    }

    @Transactional
    public void deleteComputer(Long computerId) {

        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() -> new IllegalArgumentException("Computer with ID " + computerId + " not found."));

        if (!computer.getComputer_offer().isEmpty()) {
            computerOfferRepository.deleteAll(new HashSet<>(computer.getComputer_offer()));
            computer.getComputer_offer().clear();
        }

        computerRepository.delete(computer);
        System.out.println("Deleted computer: " + computer.getName() + " (ID: " + computerId + ")");
    }

    @Transactional
    public  void updateUserComputerFromGuest(String email, List<ComputerDto> computerDtos) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User user1 = user.get();

            computerDtos.stream()
                    .forEach(computerDto -> {

                        Computer computer = new Computer();
                        computer.setName(computerDto.getName());
                        computer.setPrice(computerDto.getPrice());
                        computer.setUser(user1);
                        user1.getComputers().add(computer);
                        Computer save = computerRepository.save(computer);

                        computerDto.getOffers().stream()
                                        .forEach(baseOfferDto -> {

                                            updateComputerFromDto(save.getId(), baseOfferDto.getWebsiteUrl());
                                        });
                    });
        }
    }


    public ComputerComponentsStatsDto getComputerComponentsStats() {


        return null;
    }

}
