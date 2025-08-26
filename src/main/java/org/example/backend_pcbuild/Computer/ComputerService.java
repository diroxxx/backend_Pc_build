package org.example.backend_pcbuild.Computer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

        if (email == null) {
            return null;
        }
        List<Computer> computers = computerRepository.findAllByUserEmail(email);

        return computers.stream()
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
//                    computerRepository.save(newComputer);
                });
            }
            //usuwanie zestawów które sa w bazie ale nie ma ich w przekazanym parametrze computers w celu aktualizacji.
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

        for (ComponentDto componentDto : computerDto.getComponents()) {
            offerRepository.findByWebsiteUrl(componentDto.getWebsite_url()).ifPresent(offer -> {
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

        for (ComponentDto componentDto : computerDto.getComponents()) {
            offerRepository.findByWebsiteUrl(componentDto.getWebsite_url()).ifPresentOrElse(offer -> {
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


//    public void saveComputersByUserEmail(String email, List<ComputerDto> computers) {
//        Optional<User> user = userRepository.findByEmail(email);
//
//        if (user.isPresent()) {
//
//            for (ComputerDto computerDto : computers) {
//                computerRepository.findByName(computerDto.getName()).ifPresentOrElse(computer -> {
//                    computer.setComputer_offer(new HashSet<>());
//                    for (ComponentDto componentDto : computerDto.getComponentDtos()) {
//                        Optional<Offer> offer = offerRepository.findByWebsiteUrl(componentDto.getWebsite_url());
//                        if (offer.isPresent()) {
//                            computer.setName(computerDto.getName());
//                            computer.setPrice(computerDto.getPrice());
//                            computer.setIs_visible(computerDto.getIsVisible());
//
//                            Computer_Offer computer_offer = new Computer_Offer();
//                            computer_offer.setOffer(offer.get());
//                            computer_offer.setComputer(computer);
//                            computer.getComputer_offer().add(computer_offer);
//                            computerRepository.save(computer);
//                        }
//                    }
//                }, () -> {
//                    Computer computer = new Computer();
//                    computer.setName(computerDto.getName());
//                    computer.setPrice(computerDto.getPrice());
//                    computer.setIs_visible(computerDto.getIsVisible());
//                    for (ComponentDto componentDto : computerDto.getComponentDtos()) {
//                        Optional<Offer> offer = offerRepository.findByWebsiteUrl(componentDto.getWebsite_url());
//                        if (offer.isPresent()) {
//                            Computer_Offer computer_offer = new Computer_Offer();
//                            computer_offer.setOffer(offer.get());
//                            computer_offer.setComputer(computer);
//                            computer.getComputer_offer().add(computer_offer);
//                        }
//                    }
//                    computer.setUser(user.get());
//                    user.get().getComputers().add(computer);
//                    computerRepository.save(computer);
//                });
//            }
//        }
//    }
}
