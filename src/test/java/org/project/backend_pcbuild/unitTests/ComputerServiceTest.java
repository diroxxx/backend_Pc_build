package org.project.backend_pcbuild.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.backend_pcbuild.computer.dto.ComputerDto;
import org.project.backend_pcbuild.computer.model.Computer;
import org.project.backend_pcbuild.computer.model.ComputerOffer;
import org.project.backend_pcbuild.computer.repository.ComputerOfferRepository;
import org.project.backend_pcbuild.computer.repository.ComputerRepository;
import org.project.backend_pcbuild.computer.service.ComputerService;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.offer.repository.OfferRepository;
import org.project.backend_pcbuild.pcComponents.model.Component;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.usersManagement.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComputerServiceTest {

    @Mock
    private ComputerRepository computerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private ComputerService computerService;


    @Test
    void getAllComputersByUserEmail_returnsEmptyForNullOrBlank() {
        assertThat(computerService.getAllComputersByUserEmail(null)).isEmpty();
        assertThat(computerService.getAllComputersByUserEmail("   ")).isEmpty();
    }

    @Test
    void getAllComputersByUserEmail_mapsEntitiesToDtos() {
        Computer c = new Computer();
        c.setName("MyPC");
        c.setPrice(123.45);
        when(computerRepository.findAllByUserEmail("u@test")).thenReturn(List.of(c));

        List<ComputerDto> result = computerService.getAllComputersByUserEmail("u@test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MyPC");
    }

    @Test
    void saveComputerByUserEmail_savesWhenNameNotExists() {
        User user = new User();
        user.setEmail("u@test");
        when(userRepository.findByEmail("u@test")).thenReturn(Optional.of(user));
        when(computerRepository.findAllByUserEmail("u@test")).thenReturn(List.of());
        ComputerDto dto = new ComputerDto();
        dto.setName("NewComp");
        dto.setPrice(10.0);
        dto.setIsVisible(true);

        computerService.saveComputerByUserEmail("u@test", dto);

        ArgumentCaptor<Computer> captor = ArgumentCaptor.forClass(Computer.class);
        verify(computerRepository).save(captor.capture());
        Computer saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("NewComp");
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getPrice()).isEqualTo(10.0);
    }

    @Test
    void saveComputerByUserEmail_doesNotSaveWhenNameExists() {
        User user = new User();
        user.setEmail("u@test");
        Computer existing = new Computer();
        existing.setName("Existing");
        when(userRepository.findByEmail("u@test")).thenReturn(Optional.of(user));
        when(computerRepository.findAllByUserEmail("u@test")).thenReturn(List.of(existing));

        ComputerDto dto = new ComputerDto();
        dto.setName("Existing");
        dto.setPrice(1.0);

        computerService.saveComputerByUserEmail("u@test", dto);

        verify(computerRepository, never()).save(argThat(c -> "Existing".equals(c.getName()) && c.getPrice() == 1.0));
    }

    @Test
    void updateComputerFromDto_addsOfferAndUpdatesPrice() {
        Long compId = 1L;
        String url = "http://offer";
        Computer computer = new Computer();
        computer.setId(compId);

        when(computerRepository.findById(compId)).thenReturn(Optional.of(computer));

        Offer offer = new Offer();
        offer.setWebsiteUrl(url);
        offer.setPrice(99.99);
        Component comp = new Component();
        comp.setComponentType(ComponentType.PROCESSOR);
        comp.setModel("i7");
        offer.setComponent(comp);

        when(offerRepository.findByWebsiteUrl(url)).thenReturn(Optional.of(offer));
        when(computerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        computerService.updateComputerFromDto(compId, "\"" + url + "\"");

        ArgumentCaptor<Computer> captor = ArgumentCaptor.forClass(Computer.class);
        verify(computerRepository).save(captor.capture());
        Computer saved = captor.getValue();

        assertThat(saved.getComputer_offer()).hasSize(1);
        assertThat(saved.getPrice()).isEqualTo(99.99);
        ComputerOffer co = saved.getComputer_offer().iterator().next();
        assertThat(co.getOffer()).isSameAs(offer);
        assertThat(co.getComputer()).isSameAs(saved);
    }

    @Test
    void updateComputerName_changesNameAndSaves() {
        Long id = 2L;
        Computer computer = new Computer();
        computer.setId(id);
        when(computerRepository.findById(id)).thenReturn(Optional.of(computer));

        computerService.updateComputerName("NewName", id);

        ArgumentCaptor<Computer> captor = ArgumentCaptor.forClass(Computer.class);
        verify(computerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("NewName");
    }


    @Test
    void saveComputerByUserEmail_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("no@user")).thenReturn(Optional.empty());
        ComputerDto dto = new ComputerDto();
        dto.setName("X");

        assertThrows(IllegalArgumentException.class, () -> computerService.saveComputerByUserEmail("no@user", dto));
    }
}
