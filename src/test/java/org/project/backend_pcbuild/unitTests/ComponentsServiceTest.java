package org.project.backend_pcbuild.unitTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.offer.repository.BrandRepository;
import org.project.backend_pcbuild.pcComponents.dto.ItemComponentMapper;
import org.project.backend_pcbuild.pcComponents.dto.ProcessorItemDto;
import org.project.backend_pcbuild.pcComponents.model.Component;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.pcComponents.model.Processor;
import org.project.backend_pcbuild.pcComponents.repository.ComponentRepository;
import org.project.backend_pcbuild.pcComponents.repository.GpuModelRepository;
import org.project.backend_pcbuild.pcComponents.service.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ComponentsServiceTest {

    @InjectMocks
    private ComponentService componentService;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private BrandRepository brandRepository;

    @Test
    void getAllBrands_shouldReturnEmptyList_whenNoBrandsExist() {
        when(componentRepository.findDistinctBrands()).thenReturn(List.of());

        List<String> result = componentService.getAllBrands();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getAllBrands_shouldReturnListOfBrands_whenBrandsExist() {
        List<String> mockBrands = List.of("Intel", "AMD", "NVIDIA");
        when(componentRepository.findDistinctBrands()).thenReturn(mockBrands);

        List<String> result = componentService.getAllBrands();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder("Intel", "AMD", "NVIDIA");
    }

    private Brand invokeGetOrCreateBrand(String name) throws Exception {
        Method m = ComponentService.class.getDeclaredMethod("getOrCreateBrand", String.class);
        m.setAccessible(true);
        return (Brand) m.invoke(componentService, name);
    }

    @Test
    void throwsWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                invokeGetOrCreateBrand(null);
            } catch (Exception e) {
                throw e.getCause() == null ? e : (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    void throwsWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                invokeGetOrCreateBrand("   ");
            } catch (Exception e) {
                throw e.getCause() == null ? e : (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    void returnsExistingBrand_whenRepositoryFindsIt() throws Exception {
        Brand existing = new Brand();
        existing.setName("Intel");

        when(brandRepository.findByNameIgnoreCase("Intel")).thenReturn(Optional.of(existing));

        Brand result = invokeGetOrCreateBrand("  Intel  ");

        assertThat(result).isSameAs(existing);
        verify(brandRepository).findByNameIgnoreCase("Intel");
        verify(brandRepository, never()).save(any());
    }

    @Test
    void createsAndSavesNewBrand_whenNotFound() throws Exception {
        when(brandRepository.findByNameIgnoreCase("Asus")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand b = invocation.getArgument(0);
            return b;
        });

        Brand result = invokeGetOrCreateBrand(" Asus ");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Asus");
        verify(brandRepository).findByNameIgnoreCase("Asus");
        verify(brandRepository).save(argThat(b -> "Asus".equals(b.getName())));
    }

    @Test
    void throwsWhenBrandOrModelNull() {
        ProcessorItemDto dto1 = new ProcessorItemDto();
        dto1.setBrand(null);
        dto1.setModel("i7");

        ProcessorItemDto dto2 = new ProcessorItemDto();
        dto2.setBrand("Intel");
        dto2.setModel(null);

        assertThrows(IllegalArgumentException.class, () -> componentService.saveComponent(dto1));
        assertThrows(IllegalArgumentException.class, () -> componentService.saveComponent(dto2));
    }

    @Test
    void createsAndSavesNewProcessor_whenComponentNotFound() {
        ProcessorItemDto dto = new ProcessorItemDto();
        dto.setBrand("Intel");
        dto.setModel("i7-12700K");
        dto.setCores(12);
        dto.setThreads(20);
        dto.setSocketType("LGA1700");
        dto.setBaseClock(3.6);
        dto.setBoostClock(4.9);
        dto.setIntegratedGraphics("Intel UHD");
        dto.setTdp(125);
        dto.setBenchmark(1500.5);

        Brand brand = new Brand();
        brand.setName("Intel");

        when(brandRepository.findByNameIgnoreCase("Intel")).thenReturn(Optional.of(brand));
        when(componentRepository.findByBrandAndModelIgnoreCase(brand, "i7-12700K")).thenReturn(Optional.empty());
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> invocation.getArgument(0));

        componentService.saveComponent(dto);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(componentRepository).save(captor.capture());
        Component saved = captor.getValue();

        assertThat(saved.getComponentType()).isEqualTo(ComponentType.PROCESSOR);
        assertThat(saved.getBrand()).isSameAs(brand);
        assertThat(saved.getModel()).isEqualTo("i7-12700K");

        Processor cpu = saved.getProcessor();
        assertThat(cpu).isNotNull();
        assertThat(cpu.getCores()).isEqualTo(12);
        assertThat(cpu.getThreads()).isEqualTo(20);
        assertThat(cpu.getSocketType()).isEqualTo("LGA1700");
        assertThat(cpu.getBaseClock()).isEqualTo(3.6);
        assertThat(cpu.getBoostClock()).isEqualTo(4.9);
        assertThat(cpu.getIntegratedGraphics()).isEqualTo("Intel UHD");
        assertThat(cpu.getTdp()).isEqualTo(125);
        assertThat(cpu.getBenchmark()).isEqualTo(1500.5);
    }

    @Test
    void updatesExistingProcessor_fieldsAreMergedAndSaved() {
        // Arrange existing component with processor
        Brand brand = new Brand();
        brand.setName("AMD");

        Component existing = new Component();
        existing.setBrand(brand);
        existing.setModel("Ryzen 9 5900X");
        existing.setComponentType(ComponentType.PROCESSOR);

        Processor existingCpu = new Processor();
        existingCpu.setCores(12);
        existingCpu.setThreads(24);
        existingCpu.setSocketType("AM4");
        existingCpu.setBaseClock(3.7);
        existingCpu.setBoostClock(4.8);
        existingCpu.setIntegratedGraphics(null);
        existingCpu.setTdp(105);
        existingCpu.setBenchmark(2000.0);
        existingCpu.setComponent(existing);
        existing.setProcessor(existingCpu);

        when(brandRepository.findByNameIgnoreCase("AMD")).thenReturn(Optional.of(brand));
        when(componentRepository.findByBrandAndModelIgnoreCase(brand, "Ryzen 9 5900X")).thenReturn(Optional.of(existing));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessorItemDto dto = new ProcessorItemDto();
        dto.setBrand("AMD");
        dto.setModel("Ryzen 9 5900X");
        dto.setCores(16);
        dto.setThreads(null);
        dto.setSocketType("AM4");
        dto.setBaseClock(null);
        dto.setBoostClock(5.0);
        dto.setIntegratedGraphics("None");
        dto.setTdp(null);
        dto.setBenchmark(2100.0);

        componentService.saveComponent(dto);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(componentRepository).save(captor.capture());
        Component saved = captor.getValue();

        Processor cpu = saved.getProcessor();
        assertThat(cpu.getCores()).isEqualTo(16);
        assertThat(cpu.getThreads()).isEqualTo(24);
        assertThat(cpu.getSocketType()).isEqualTo("AM4");
        assertThat(cpu.getBaseClock()).isEqualTo(3.7);
        assertThat(cpu.getBoostClock()).isEqualTo(5.0);
        assertThat(cpu.getIntegratedGraphics()).isEqualTo("None");
        assertThat(cpu.getTdp()).isEqualTo(105);
        assertThat(cpu.getBenchmark()).isEqualTo(2100.0);
    }



}
